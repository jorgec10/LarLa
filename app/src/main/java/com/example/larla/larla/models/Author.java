package com.example.larla.larla.models;

import com.stfalcon.chatkit.commons.models.IUser;

import org.matrix.androidsdk.rest.model.RoomMember;

public class Author implements IUser {

    private RoomMember roomMember;

    public Author(RoomMember roomMember) {
        this.roomMember = roomMember;
    }

    @Override
    public String getId() {
        return roomMember.getUserId();
    }

    @Override
    public String getName() {
        return roomMember.getName();
    }

    @Override
    public String getAvatar() {
        return roomMember.getAvatarUrl();
    }
}
