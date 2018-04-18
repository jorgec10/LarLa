package com.example.larla.larla.models;

public class Chat {

    private String name;
    private String info;
    private Integer image;
    private String roomId;

    public Chat(String username, String info, Integer image, String roomId) {
        this.name = username;
        this.info = info;
        this.image = image;
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
