package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class CoverPhoto {
    @SerializedName("id")
    private String id;
    @SerializedName("created_at")
    private String createId;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("color")
    private String color;
    @SerializedName("likes")
    private int likes;
    @SerializedName("liked_by_user")
    private Boolean likedByUser;
    @SerializedName("description")
    private String description;
    @SerializedName("user")
    private CoverPhotoUser user;
    @SerializedName("urls")
    private URLs urls;
    @SerializedName("links")
    private CoverPhotoLinks coverPhotoLinks;

}
