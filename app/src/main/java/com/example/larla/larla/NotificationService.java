package com.example.larla.larla;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.util.JsonUtils;

public class NotificationService extends Service {

    public static final String CHANNEL_ID = "com.example.larla.MESSAGES";
    public static final String CHANNEL_NAME = "Messages";

    private MXSession session;
    private MXEventListener eventListener;

    public NotificationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        eventListener = new MXEventListener() {
            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                if (session.getDataHandler().isInitialSyncComplete()) {

                    if(event.type.equals(Event.EVENT_TYPE_MESSAGE)) {
                    //if (event.type.equals(Event.EVENT_TYPE_MESSAGE) && !event.getSender().equals(session.getMyUserId())) {
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


        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        session = Matrix.getInstance(this).getSession();
        session.getDataHandler().addListener(eventListener);

        startForeground(startId,
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentText("Running in background").build());

        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
