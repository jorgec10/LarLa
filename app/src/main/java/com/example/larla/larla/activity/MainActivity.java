package com.example.larla.larla.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.larla.larla.Matrix;
import com.example.larla.larla.notifications.NotificationAlarm;
import com.example.larla.larla.R;
import com.example.larla.larla.models.Chat;
import com.example.larla.larla.sip.IncomingCallReceiver;
import com.example.larla.larla.sip.LarlaSipManager;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MXSession session;
    private MXEventListener sessionListener;
    private DialogsListAdapter dialogsListAdapter;


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

        Intent alarm = new Intent(this, NotificationAlarm.class);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(this, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringAlarm);


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

        dialogsListAdapter = new DialogsListAdapter<>(new ImageLoader() {
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


        sessionListener = new MXEventListener() {
            @Override
            public void onInitialSyncComplete(String toToken) {

                for (Room room : session.getDataHandler().getStore().getRooms()) {
                    dialogsListAdapter.addItem(new Chat(session, room.getState()));
                }
            }
        };

        session.getDataHandler().addListener(sessionListener);

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener() {
            @Override
            public void onDialogClick(IDialog dialog) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                String roomId = dialog.getId();
                String roomName = dialog.getDialogName();
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);

                session.getDataHandler().getRoom(roomId).markAllAsRead(new SimpleApiCallback<Void>());

                startActivity(intent);
            }
        });

        SharedPreferences preferences = getBaseContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String sipUsername = preferences.getString("sipUsername", null);
        String sipPassword = preferences.getString("sipPassword", null);
        String sipDomain = preferences.getString("sipDomain", null);

        LarlaSipManager.getInstance(this).initializeManager(sipUsername, sipDomain, sipPassword);

    }

    @Override
    protected void onPause() {
        super.onPause();
        dialogsListAdapter.clear();
        session.getDataHandler().removeListener(sessionListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dialogsListAdapter.clear();
        session.getDataHandler().addListener(sessionListener);

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
            userInfoIntent.putExtra("userId", session.getDataHandler().getUserId());
            startActivity(userInfoIntent);
        } else if (id == R.id.nav_settings) {
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
