package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Photo {

    @SerializedName("id")
    private String id;
    @SerializedName("created_at")
    private String createdAt;
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
    private User user;
    @SerializedName("current_user_collections")
    private List<CurrentUserCollections> currentUserCollections;
    @SerializedName("urls")
    private URLs URLs;
    @SerializedName("links")
    private Links links;

    public Links getLinks() {
        return links;
    }

    public String getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public int getLikes() {
        return likes;
    }

    public int getHeight() {
        return height;
    }

    public Boolean getLikedByUser() {
        return likedByUser;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public List<CurrentUserCollections> getCurrentUserCollections() {
        return currentUserCollections;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public URLs getURLs() {
        return URLs;
    }
}
