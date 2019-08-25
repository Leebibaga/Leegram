package com.example.leegram.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PhotoItem extends RealmObject {

    @PrimaryKey
    private String pictureURL;
    private String date;
    private String api;
    private String category;
    private byte[] picture;

    public String getPictureURL() {
        return pictureURL;
    }

    public String getDate() {
        return date;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
}
