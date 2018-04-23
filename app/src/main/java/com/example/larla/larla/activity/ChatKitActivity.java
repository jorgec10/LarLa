package com.example.larla.larla.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;
import com.example.larla.larla.fragments.LarlaMessageListFragment;
import com.example.larla.larla.models.Author;
import com.example.larla.larla.models.Message;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.callback.ToastErrorHandler;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.sync.RoomResponse;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.example.larla.larla.activity.MainActivity.hasPermissions;

public class ChatKitActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 100;
    private static final int REQUEST_VIDEO = 101;
    private static final int REQUEST_LOCATION = 102;
    File destination;
    ImageView imageView;
    MXSession session;
    String roomId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_kit_activity);

        // Check permissions
        int PERMISSION_ALL = 1;
        String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }

        // Get session, room and user info
        roomId = this.getIntent().getStringExtra("roomId");
        session = Matrix.getInstance(getApplicationContext()).getSession();
        String matrixUserId = session.getDataHandler().getUserId();
        String matrixUser = session.getDataHandler().getUser(matrixUserId).displayname;

        setTitle(this.getIntent().getStringExtra("roomName"));

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        LarlaMessageListFragment fragment = (LarlaMessageListFragment) fm.findFragmentByTag("FRAGMENT");

        if (fragment == null) {
            // this fragment displays messages and handles all message logic
            fragment = LarlaMessageListFragment.newInstance(session.getMyUserId(), roomId, org.matrix.androidsdk.R.layout.fragment_matrix_message_list_fragment);
            fm.beginTransaction().add(R.id.chat_view, fragment, "FRAGMENT").commit();
        }

        // User messages input
        MessageInput messageInput = findViewById(R.id.input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                session.getDataHandler().getRoom(roomId).sendTextMessage(input.toString(), null, null, null);
                return true;
            }
        });

        // If SDK > 24 we have to use FileProvider class to give access to the particular file or folder to make them accessible for other apps.
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        // Attachment listener
        messageInput.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {

                final CharSequence options[] = new CharSequence[] {"Photo", "Video", "Audio", "Location"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatKitActivity.this);
                builder.setTitle("Select attachment");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Photo
                                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                //Add extra to save full-image somewhere
                                destination = new File(Environment.getExternalStorageDirectory(),"image.jpg");
                                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                                //REQUEST_IMAGE defines a request code in order to identify it on the onActivityResult
                                startActivityForResult(imageIntent, REQUEST_IMAGE);
                                break;
                            case 1: // Video
                                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                //Add extra to save video to our file
                                destination = new File(Environment.getExternalStorageDirectory(),"myVideo");
                                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                                //Optional extra to set video quality
                                videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                startActivityForResult(videoIntent, REQUEST_VIDEO);
                                break;
                            case 2: // Audio
                                Intent audioIntent = new Intent(ChatKitActivity.this, AudioActivity.class);
                                startActivity(audioIntent);
                                break;
                            case 3: // Location
                                Intent mapsIntent = new Intent(ChatKitActivity.this, MapsActivity.class);
                                startActivityForResult(mapsIntent, REQUEST_LOCATION);
                                break;
                        }
                    }
                });
                builder.show();

            }
        });
    }

    @Override
    //Print the image in the Imageview when the Intent to the camera has finished
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                FileInputStream in = new FileInputStream(destination);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; //Downsample 2x
                Bitmap userImage = BitmapFactory.decodeStream(in, null, options);
                imageView.setImageBitmap(userImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        else if(requestCode == REQUEST_VIDEO && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();
        }

        else if(requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK) {
            String location = data.getStringExtra("lat") + " : " + data.getStringExtra("long");
            //Toast.makeText(this, data.getStringExtra("lat") + " : " + data.getStringExtra("long"), Toast.LENGTH_SHORT).show();
            session.getDataHandler().getRoom(roomId).sendTextMessage(location, null, null, null);
        }
    };

}