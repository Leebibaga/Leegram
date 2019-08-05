package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("published_at")
    private String publishedAt;
    @SerializedName("curated")
    private Boolean curated;
    @SerializedName("featured")
    private Boolean featured;
    @SerializedName("total_photos")
    private int totalPhotos;
    @SerializedName("private")
    private Boolean isPrivate;
    @SerializedName("share_key")
    private String shareKey;
    @SerializedName("cover_photo")
    private CoverPhoto coverPhoto;
    @SerializedName("user")
    private CoverPhotoUser user;
}
