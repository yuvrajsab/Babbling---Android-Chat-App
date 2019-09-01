package io.github.yuvrajsab.babblin;

public class Users {

    public String image;
    public String thumbnail;
    public String name;
    public String status;
    public boolean online;
    public String phone;

    public Users() {
    }

    public Users(String image, String thumbnail, String name, String status, boolean online, String phone) {
        this.image = image;
        this.thumbnail = thumbnail;
        this.name = name;
        this.status = status;
        this.online = online;
        this.phone = phone;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
