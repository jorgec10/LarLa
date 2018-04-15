package com.example.larla.larla.models;

public class Chat {

    private String name;
    private String info;
    private Integer image;

    public Chat(String username, String info, Integer image) {
        this.name = username;
        this.info = info;
        this.image = image;
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
}
