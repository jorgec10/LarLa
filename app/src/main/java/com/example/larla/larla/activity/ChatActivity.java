package com.example.larla.larla.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;
import com.example.larla.larla.fragments.LarlaMessageListFragment;
import com.stfalcon.chatkit.messages.MessageInput;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.message.LocationMessage;

import java.io.File;
import java.lang.reflect.Method;

import static com.example.larla.larla.activity.MainActivity.hasPermissions;

public class ChatActivity extends BaseActivity {

    private static final int REQUEST_IMAGE = 100;
    private static final int REQUEST_VIDEO = 101;
    private static final int REQUEST_LOCATION = 102;
    public static final int REQUEST_AUDIO = 103;
    public static final int REQUEST_IMAGE_GALLERY = 104;
    public static final int REQUEST_VIDEO_GALLERY = 105;

    public static final String larlaMessagesFragmentTAG = "com.example.larla.MESSAGESFRAGMENT";

    MXSession session;
    String roomId;
    private LarlaMessageListFragment fragment;
    private File destination;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Check permissions
        int PERMISSION_ALL = 1;
        String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.USE_SIP};

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
        fragment = (LarlaMessageListFragment) fm.findFragmentByTag(larlaMessagesFragmentTAG);

        if (fragment == null) {
            // this fragment displays messages and handles all message logic
            fragment = LarlaMessageListFragment.newInstance(session.getMyUserId(), roomId, org.matrix.androidsdk.R.layout.fragment_matrix_message_list_fragment);
            fm.beginTransaction().add(R.id.chat_view, fragment, larlaMessagesFragmentTAG).commit();
        }

        // User messages input
        MessageInput messageInput = findViewById(R.id.input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                session.getDataHandler().getRoom(roomId).sendTextMessage(input.toString(), null, null, null);
                //fragment.sendTextMessage(input.toString(), null, null);
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
                
                final CharSequence options[] = new CharSequence[] {"Take a photo",
                                                                    "Photo from gallery",
                                                                    "Record a video",
                                                                    "Video from gallery",
                                                                    "Record an audio",
                                                                    "Select location",};
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select attachment");
                AlertDialog.Builder builder1 = builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String url = null;

                        switch (which) {
                            case 0: // Photo
                                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                //Add extra to save full-image somewhere
                                destination = new File(getExternalCacheDir(),"image.jpg");
                                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                                //REQUEST_IMAGE defines a request code in order to identify it on the onActivityResult
                                startActivityForResult(imageIntent, REQUEST_IMAGE);
                                break;
                            case 1: // Image from gallery
                                Intent galleryIntent = new Intent();
                                galleryIntent.setType("image/*");
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(galleryIntent, "Select picture"), REQUEST_IMAGE_GALLERY);
                                break;
                            case 2: // Video
                                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                //Add extra to save video to our file
                                destination = new File(getExternalCacheDir(),"video.mp4");
                                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                                //Optional extra to set video quality
                                videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                startActivityForResult(videoIntent, REQUEST_VIDEO);
                                break;
                            case 3: // Video from gallery
                                Intent galleryVideoIntent = new Intent();
                                galleryVideoIntent.setType("video/*");
                                galleryVideoIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(galleryVideoIntent, "Select picture"), REQUEST_VIDEO_GALLERY);
                                break;
                            case 4: // Audio
                                Intent audioIntent = new Intent(ChatActivity.this, AudioActivity.class);
                                startActivityForResult(audioIntent, REQUEST_AUDIO);
                            break;
                            case 5: // Location
                                Intent mapsIntent = new Intent(ChatActivity.this, MapsActivity.class);
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
            RoomMediaMessage message = new RoomMediaMessage(Uri.fromFile(destination));
            Log.d("Image", "Message created, sending with type " + message.getMessageType() + " mime " + message.getMimeType(getApplicationContext()));
            fragment.sendMediaMessage(message);

        }

        else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {

            Uri selectedImageUri = data.getData();
            RoomMediaMessage message = new RoomMediaMessage(selectedImageUri);
            Log.d("Image from gallery", "Message created, sending with type " + message.getMessageType() + " mime " + message.getMimeType(getApplicationContext()));
            fragment.sendMediaMessage(message);

        }

        else if(requestCode == REQUEST_VIDEO && resultCode == Activity.RESULT_OK) {
            RoomMediaMessage message = new RoomMediaMessage(Uri.fromFile(destination));
            fragment.sendMediaMessage(message);        }

        else if(requestCode == REQUEST_VIDEO_GALLERY && resultCode == Activity.RESULT_OK) {

            Uri selectedVideoUri = data.getData();
            RoomMediaMessage message = new RoomMediaMessage(selectedVideoUri);
            fragment.sendMediaMessage(message);
        }

        else if(requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK) {
            String location = "geo:" + data.getStringExtra("lat") + "," + data.getStringExtra("long");
            LocationMessage locationMessage = new LocationMessage();
            locationMessage.geo_uri = location;
            locationMessage.body = "Shared location";
            locationMessage.msgtype = org.matrix.androidsdk.rest.model.message.Message.MSGTYPE_LOCATION;
            session.getRoomsApiClient().sendMessage("algo", roomId, locationMessage, new SimpleApiCallback<Event>());
            //session.getDataHandler().getRoom(roomId).sendTextMessage(location, null, null, null);
        }

        else if (requestCode == REQUEST_AUDIO && resultCode == Activity.RESULT_OK) {
            String url = data.getStringExtra("url");
            String filename = data.getStringExtra("filename");
            RoomMediaMessage message = new RoomMediaMessage(Uri.parse(url), filename);
            Log.d("Audio", "Message created, sending with type " + message.getMessageType() + " mime " + message.getMimeType(getApplicationContext()));
            fragment.sendMediaMessage(message);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_room_info) {
            Intent roomInfoIntent = new Intent(ChatActivity.this, RoomInfoActivity.class);
            roomInfoIntent.putExtra("roomId", roomId);
            startActivity(roomInfoIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}