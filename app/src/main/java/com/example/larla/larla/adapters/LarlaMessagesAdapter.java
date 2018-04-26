package com.example.larla.larla.adapters;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larla.larla.R;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.MXMediaDownloadListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.message.AudioMessage;
import org.matrix.androidsdk.rest.model.message.ImageMessage;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.util.JsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LarlaMessagesAdapter extends AbstractMessagesAdapter {

    private final HashMap<String, MessageRow> eventos = new HashMap<>();

    final MXSession session;

    public LarlaMessagesAdapter(MXSession session, Context context) {
        super(context, 0);
        this.session = session;
    }

    @Override
    public void add(MessageRow messageRow, boolean b) {
        if (isSupportedRow(messageRow)) {
            if (eventos.containsKey(messageRow.getEvent().eventId)) {
                eventos.put(messageRow.getEvent().eventId, messageRow);
            } else {
                super.add(messageRow);
                if (messageRow.getEvent().eventId != null) {
                    eventos.put(messageRow.getEvent().eventId, messageRow);
                }
            }
        }
    }

    @Override
    public void addToFront(MessageRow messageRow) {
        if (isSupportedRow(messageRow)) {
            insert(messageRow,0);
            if (messageRow.getEvent().eventId != null) {
                eventos.put(messageRow.getEvent().eventId, messageRow);
            }
        }
    }

    @Override
    public MessageRow getMessageRow(String s) {
        if (s != null) {
            return eventos.get(s);
        } else {
            return null;
        }
    }

    @Override
    public MessageRow getClosestRow(Event event) {
        if (event == null) {
            return null;
        } else {
            return getClosestRowFromTs(event.eventId, event.getOriginServerTs());
        }
    }

    @Override
    public MessageRow getClosestRowFromTs(String s, long l) {
        MessageRow messageRow = getMessageRow(s);

        if (messageRow == null) {
            List<MessageRow> rows = new ArrayList<>(eventos.values());

            // loop because the list is not sorted
            for (MessageRow row : rows) {

                // check if the row event has been received after eventTs (from)
                if (row.getEvent().getOriginServerTs() > l) {
                    // not yet initialised
                    if (messageRow == null) {
                        messageRow = row;
                    }
                    // keep the closest row
                    else if (row.getEvent().getOriginServerTs() < messageRow.getEvent().getOriginServerTs()) {
                        messageRow = row;
                        Log.d("Debug", "## getClosestRowFromTs() " + row.getEvent().eventId);
                    }
                }
            }
        }

        return messageRow;
    }

    @Override
    public MessageRow getClosestRowBeforeTs(String s, long l) {

        MessageRow messageRow = getMessageRow(s);

        if (messageRow == null) {
            List<MessageRow> rows = new ArrayList<>(eventos.values());

            // loop because the list is not sorted
            for (MessageRow row : rows) {
                    long rowTs = row.getEvent().getOriginServerTs();

                    // check if the row event has been received before eventTs (from)
                    if (rowTs < l) {
                        // not yet initialised
                        if (messageRow == null) {
                            messageRow = row;
                        }
                        // keep the closest row
                        else if (rowTs > messageRow.getEvent().getOriginServerTs()) {
                            messageRow = row;
                            Log.d("Debug", "## getClosestRowBeforeTs() " + row.getEvent().eventId);
                        }
                    }
            }
        }

        return messageRow;
    }

    @Override
    public void updateEventById(Event event, String s) {
        MessageRow row = eventos.get(event.eventId);

        // the event is not yet defined
        if (null == row) {
            MessageRow oldRow = eventos.get(s);

            if (oldRow != null) {
                eventos.remove(s);
                eventos.put(event.eventId, oldRow);
            }
        } else {
            // the eventId already exists
            // remove the old display
            removeEventById(s);
        }

        notifyDataSetChanged();
    }

    @Override
    public void removeEventById(String s) {
        MessageRow row = eventos.get(s);

        if (row != null) {
            remove(row);
            eventos.remove(s);
        }

        notifyDataSetChanged();
    }

    @Override
    public void setIsPreviewMode(boolean b) {

    }

    @Override
    public void setIsUnreadViewMode(boolean b) {

    }

    @Override
    public boolean isUnreadViewMode() {
        return false;
    }

    @Override
    public void setSearchPattern(String s) {

    }

    @Override
    public void resetReadMarker() {

    }

    @Override
    public void updateReadMarker(String s, String s1) {

    }

    @Override
    public int getMaxThumbnailWidth() {
        return 0;
    }

    @Override
    public int getMaxThumbnailHeight() {
        return 0;
    }

    @Override
    public void onBingRulesUpdate() {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_text, parent, false);
        }
        // GA Crash
        if (position >= getCount()) {
            return convertView;
        }

        MessageRow row = getItem(position);
        Event event = row.getEvent();

        // sanity check
        if (event.eventId != null) {

            synchronized (this) {
                switch (event.type) {
                    case Event.EVENT_TYPE_MESSAGE:
                        Message message = JsonUtils.toMessage(event.getContent());
                        switch (message.msgtype) {
                            case Message.MSGTYPE_TEXT:
                                return getTextView(position,convertView,parent);
                            case Message.MSGTYPE_IMAGE:
                                return getImageView(position,convertView,parent);
                            case Message.MSGTYPE_AUDIO:
                                return getAudioView(position,convertView,parent);
                            default:
                                return convertView;
                        }
                    default:
                        return convertView;
                }
            }
        }
        return convertView;
    }

    private View getAudioView(int position, View convertView, ViewGroup parent) {
        MessageRow row = getItem(position);
        final Event event = row.getEvent();

        if (event.getSender() != null) {

            if (event.getSender().equals(session.getMyUserId())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_audio_send, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_audio, parent, false);
            }

            TextView nameTextField = (TextView) convertView.findViewById(R.id.text_message_name);
            final Button playButton = (Button) convertView.findViewById(R.id.play_message_body);
            TextView timeTextField = (TextView) convertView.findViewById(R.id.text_message_time);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image_message_profile);

            final String url = session.getDataHandler().getUser(event.getSender()).avatar_url;
            if (url != null && imageView != null) {
                session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(), imageView, url,32);
            }

            if (nameTextField != null) {
                nameTextField.setText(session.getDataHandler().getUser(event.getSender()).displayname != null ? session.getDataHandler().getUser(event.getSender()).displayname : event.getSender());
            }

            final AudioMessage message = JsonUtils.toAudioMessage(event.getContent());
            final String urlAudio = message.getUrl();

            if (urlAudio != null && playButton != null) {
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final File file = new File(Environment.getExternalStoragePublicDirectory("Larla/Audios"), "audio"+event.getOriginServerTs());
                        if (file.exists()) {
                            playAudio(file, message);
                        } else {

                            getContext().registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    String action = intent.getAction();

                                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                        playAudio(file, message);
                                    }

                                }
                            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                            String uri = session.getContentManager().getDownloadableUrl(urlAudio);
                            DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                            request.setDestinationInExternalPublicDir("Larla/Audios", "audio"+event.getOriginServerTs());
                            request.setMimeType(message.getMimeType());
                            request.setVisibleInDownloadsUi(false);
                            manager.enqueue(request);

                        }
                    }
                });
            }

            if (timeTextField != null) {
                Date date = new Date(event.getOriginServerTs());
                timeTextField.setText(new SimpleDateFormat("HH:mm").format(date));
            }
        }

        return convertView;
    }

    private void playAudio(File file, AudioMessage message) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            //Toast.makeText((), "Playing Audio", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Intent playIntent = new Intent();
//        playIntent.setAction(android.content.Intent.ACTION_VIEW);
//        playIntent.setDataAndType(Uri.fromFile(file), message.getMimeType());
//        getContext().startActivity(playIntent);
    }

    private View getImageView(int position, View convertView, ViewGroup parent) {

        MessageRow row = getItem(position);
        Event event = row.getEvent();

        if (event.getSender() != null) {

            if (event.getSender().equals(session.getMyUserId())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_image_send, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_image, parent, false);
            }

            TextView nameTextField = (TextView) convertView.findViewById(R.id.text_message_name);
            final ImageView imageMessageBody = (ImageView) convertView.findViewById(R.id.image_message_body);
            TextView timeTextField = (TextView) convertView.findViewById(R.id.text_message_time);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image_message_profile);

            String url = session.getDataHandler().getUser(event.getSender()).avatar_url;
            if (url != null && imageView != null) {
                session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(), imageView, url,32);
            }

            if (nameTextField != null) {
                nameTextField.setText(session.getDataHandler().getUser(event.getSender()).displayname != null ? session.getDataHandler().getUser(event.getSender()).displayname : event.getSender());
            }

            final ImageMessage message = JsonUtils.toImageMessage(event.getContent());
            final String urlImageBody = message.getThumbnailUrl();
            if (urlImageBody != null && imageMessageBody != null) {
                Log.d("image", "printing image");
                if (!session.getMediasCache().isMediaCached(urlImageBody, message.getMimeType())) {
                    session.getMediasCache().downloadMedia(getContext(), session.getHomeServerConfig(), urlImageBody, message.getMimeType(), message.info.thumbnail_file, new MXMediaDownloadListener() {
                        @Override
                        public void onDownloadComplete(String downloadId) {
                            session.getMediasCache().loadBitmap(session.getHomeServerConfig(), imageMessageBody, urlImageBody, 100,100, message.getRotation(), 0, message.info.thumbnailInfo.mimetype, message.info.thumbnail_file);
                        }
                    });
                } else {
                    session.getMediasCache().loadBitmap(session.getHomeServerConfig(), imageMessageBody, urlImageBody, 100, 100, message.getRotation(), 0, message.info.thumbnailInfo.mimetype, message.info.thumbnail_file);
                }
            }

            if (timeTextField != null) {
                Date date = new Date(event.getOriginServerTs());
                timeTextField.setText(new SimpleDateFormat("HH:mm").format(date));
            }
        }

        return convertView;
    }

    protected View getTextView(final int position, View convertView, ViewGroup parent) {
        MessageRow row = getItem(position);
        Event event = row.getEvent();

        if (event.getSender() != null) {

            if (event.getSender().equals(session.getMyUserId())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_text_send, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_message_text, parent, false);
            }

            TextView nameTextField = (TextView) convertView.findViewById(R.id.text_message_name);
            TextView infoTextField = (TextView) convertView.findViewById(R.id.text_message_body);
            TextView timeTextField = (TextView) convertView.findViewById(R.id.text_message_time);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image_message_profile);

            String url = session.getDataHandler().getUser(event.getSender()).avatar_url;
            if (url != null && imageView != null) {
                session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(), imageView, url,32);
            }

            if (nameTextField != null) {
                nameTextField.setText(session.getDataHandler().getUser(event.getSender()).displayname != null ? session.getDataHandler().getUser(event.getSender()).displayname : event.getSender());
            }
            if (infoTextField != null) {
                infoTextField.setText(JsonUtils.toMessage(event.getContent()).body);
            }
            if (timeTextField != null) {
                Date date = new Date(event.getOriginServerTs());
                timeTextField.setText(new SimpleDateFormat("HH:mm").format(date));
            }
        }

        return convertView;
    }


    private boolean isSupportedRow(MessageRow row) {
        RoomState roomState = row.getRoomState();
        Event event = row.getEvent();

        // sanity checks
        if ((null == event) || (null == event.eventId) || (null == roomState)) {
            Log.e("Matrix", "## isSupportedRow() : invalid row");
            return false;
        }

        String eventId = event.eventId;
        MessageRow currentRow = eventos.get(eventId);

        if (null != currentRow) {
            // waiting for echo
            // the message is displayed as sent event if the echo has not been received
            // it avoids displaying a pending message whereas the message has been sent
            if (event.getAge() == Event.DUMMY_EVENT_AGE) {
                currentRow.updateEvent(event);
                Log.d("Matrix", "## isSupportedRow() : update the timestamp of " + eventId);
            } else {
                Log.e("Matrix", "## isSupportedRow() : the event " + eventId + " has already been received");
            }

            return true;
        }

        String eventType = event.getType();

        if (Event.EVENT_TYPE_MESSAGE.equals(eventType)) {
            // A message is displayable as long as it has a body
            // Redacted messages should not be displayed
            Message message = JsonUtils.toMessage(event.getContent());
            return !TextUtils.isEmpty(message.body);
        }

        return false;
    }

}