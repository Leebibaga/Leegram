package com.example.leegram.model;

import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PhotoItem extends RealmObject {

    @PrimaryKey
    private String pictureURL;
    private String date;
    private String api;
    private String category;
    private String picture;
    @Index
    private int position;

    public String getPictureURL() {
        return pictureURL;
    }

    public String getDate() {
        return date;
    }

    public String getPicture() {
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

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
