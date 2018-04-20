package com.example.larla.larla.activity;

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

public class ChatKitActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 100;
    private static final int REQUEST_VIDEO = 101;
    File destination;
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_kit_activity);

        // Get session, room and user info
        final String roomId = this.getIntent().getStringExtra("roomId");
        final MXSession session = Matrix.getInstance(getApplicationContext()).getSession();
        String matrixUserId = session.getDataHandler().getUserId();
        String matrixUser = session.getDataHandler().getUser(matrixUserId).displayname;
        final Author userAuthor = new Author(matrixUser, matrixUser, "avatar");

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
                Message message = new Message("1", input.toString(), userAuthor, new Date(System.currentTimeMillis()));
                session.getDataHandler().getRoom(roomId).sendTextMessage(message.getText(), null, null, null);
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

                                break;
                            case 3: // Location
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
    };

}