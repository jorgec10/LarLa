package com.example.larla.larla.models;


import com.stfalcon.chatkit.commons.models.IMessage;

import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.util.JsonUtils;

import java.util.Date;

public class Message implements IMessage {

    private Event event;

    public Message(Event event) {
        this.event = event;
    }

    @Override
    public String getId() {
        return event.eventId;
    }

    @Override
    public String getText() {
        if (event.type.equals(Event.EVENT_TYPE_MESSAGE)) {
            org.matrix.androidsdk.rest.model.message.Message message = JsonUtils.toMessage(event.getContent());
            return message.body;
        }
        return "No message event";
    }

    @Override
    public Author getUser() {
        RoomMember member = JsonUtils.toRoomMember(event.getContent());
        return new Author(member);
    }

    @Override
    public Date getCreatedAt() {
        return new Date(event.getOriginServerTs());
    }
}

