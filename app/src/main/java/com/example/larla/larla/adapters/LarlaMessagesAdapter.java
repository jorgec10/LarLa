package com.example.larla.larla.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.larla.larla.R;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
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
        super.add(messageRow);
        eventos.put(messageRow.getEvent().eventId, messageRow);
    }

    @Override
    public void addToFront(MessageRow messageRow) {
        Log.d("Debug", messageRow.getEvent().getContentAsJsonObject().get("body").getAsString());
        insert(messageRow,0);
    }

    @Override
    public MessageRow getMessageRow(String s) {
        return eventos.get(s);
    }

    @Override
    public MessageRow getClosestRow(Event event) {
        return getClosestRowFromTs(event.eventId, event.getOriginServerTs());
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
        //eventos.put(eve);
    }

    @Override
    public void removeEventById(String s) {
        eventos.remove(s);
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
        return getTextView(position, convertView, parent);
    }

    protected View getTextView(final int position, View convertView, ViewGroup parent) {
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
        if (null != event.eventId) {
            synchronized (this) {
                //this code gets references to objects in the chat_listview_row.xml file
                TextView nameTextField = (TextView) convertView.findViewById(R.id.txt_sender);
                TextView infoTextField = (TextView) convertView.findViewById(R.id.txt_msg);

                if (event.type.equals(Event.EVENT_TYPE_MESSAGE)) {
                    nameTextField.setText(session.getDataHandler().getUser(event.getSender()).displayname);
                    infoTextField.setText(event.getContentAsJsonObject().get("body").getAsString());
                } else {
                    nameTextField.setText(event.type);
                    infoTextField.setText("no text");
                }
            }
        }


        return convertView;
    }

}