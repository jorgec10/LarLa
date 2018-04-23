package com.example.larla.larla.models;

import android.util.Log;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Chat implements IDialog {

    private MXSession session;
    private RoomState roomState;

    public Chat(MXSession session, RoomState roomState) {
        this.session = session;
        this.roomState = roomState;
    }

    @Override
    public String getId() {
        return roomState.roomId;
    }

    @Override
    public String getDialogPhoto() {
        Log.d("Image", "getDialogPhoto: " + roomState.getAvatarUrl());
        return roomState.getAvatarUrl();
    }

    @Override
    public String getDialogName() {
        return roomState.getDisplayName(session.getMyUser().user_id);
    }

    @Override
    public List<? extends IUser> getUsers() {
        List<Author> users = new LinkedList<Author>();
        for (RoomMember roomMember : roomState.getMembers()) {
            users.add(new Author(roomMember));
        };
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        Event event = session.getDataHandler().getStore().getLatestEvent(roomState.roomId);
        return new Message(event);
    }

    @Override
    public void setLastMessage(IMessage message) {
    }

    @Override
    public int getUnreadCount() {
        return roomState.getNotificationCount();
    }
}
