package com.example.larla.larla.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.R;
import com.example.larla.larla.models.Chat;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.CreateRoomParams;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.util.JsonUtils;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CHANNEL_ID = "com.example.larla.MESSAGES";
    public static final String CHANNEL_NAME = "Mensajes";
    private MXSession session;


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("LarLa");

        session = Matrix.getInstance(getApplicationContext()).getSession();

        final String userName = this.getIntent().getStringExtra("userName");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle("New chat");
                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(96, 96, 96, 96);

                final EditText chatNameInput = new EditText(MainActivity.this);
                chatNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                chatNameInput.setHint("Chat name");
                layout.addView(chatNameInput);

                dialogBuilder.setView(layout);

                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String chatName = chatNameInput.getText().toString();
                        session.createRoom(chatName, null,  null, new SimpleApiCallback<String>());
                    }
                });

                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                dialogBuilder.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headView = navigationView.getHeaderView(0);
        TextView textNavBar = (TextView) headView.findViewById(R.id.textViewNavBarUser);
        textNavBar.setText(userName);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        final DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Log.d("Image", "loadImage: loading... " + url);
                if (imageView != null) {
                    if(url != null) {
                        session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(), imageView, url, 10);
                    } else {
                        imageView.setImageResource(R.drawable.man_96);
                    }
                }
            }
        });
        DialogsList listView = (DialogsList) findViewById(R.id.chatListView);
        listView.setAdapter(dialogsListAdapter);


        session.getDataHandler().addListener(new MXEventListener() {
            @Override
            public void onInitialSyncComplete(String toToken) {

                for (Room room : session.getDataHandler().getStore().getRooms()) {
                    dialogsListAdapter.addItem(new Chat(session, room.getState()));
                }
            }

            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                if (session.getDataHandler().isInitialSyncComplete()) {

                    if (event.type.equals(Event.EVENT_TYPE_MESSAGE) && !event.getSender().equals("@" + userName + ":matrix.org")) {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setStyle(new NotificationCompat.MessagingStyle(session.getMyUser().displayname)
                                                .setConversationTitle(roomState.name)
                                                .addMessage(JsonUtils.toMessage(event.getContent()).body, event.getOriginServerTs(), session.getDataHandler().getUser(event.getSender()).displayname)).setSmallIcon(R.drawable.ic_stat_name).setColor(Color.argb(0, 75, 143, 255)).setColorized(true);

                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        mNotificationManager.notify(event.hashCode(), mBuilder.build());
                    }
                }
            }
        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener() {
            @Override
            public void onDialogClick(IDialog dialog) {
                Intent intent = new Intent(MainActivity.this, ChatKitActivity.class);
                String roomId = dialog.getId();
                String roomName = dialog.getDialogName();
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_user) {
            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
            startActivity(userInfoIntent);
        } else if (id == R.id.nav_settings) {
            // ToDo
            Intent callIntent = new Intent(MainActivity.this, SipSettingsActivity.class);
            startActivity(callIntent);
        } else if (id == R.id.nav_share) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle("About Larla");
            alertBuilder.setMessage("An awesome chat Android app for the people that are ready to be unplugged.\n\n" +
                                    "@adrymyry & @jorgec10\n\n" +
                                    "University of Murcia\n" +
                                    "2018");
            alertBuilder.setPositiveButton("OK", null);
            AlertDialog dialog = alertBuilder.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            dialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        item.setChecked(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_call) {
            Intent callIntent = new Intent(MainActivity.this, SipCallActivity.class);
            startActivity(callIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }
}
