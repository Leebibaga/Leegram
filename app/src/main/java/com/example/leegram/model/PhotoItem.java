package com.example.leegram.model;

import java.time.format.DateTimeFormatter;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PhotoItem extends RealmObject {

    @PrimaryKey
    private String picture;
    private String date;
    private String api;
    private String category;

    public String getPicture() {
        return picture;
    }

    public String getDate() {
        return date;
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

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
