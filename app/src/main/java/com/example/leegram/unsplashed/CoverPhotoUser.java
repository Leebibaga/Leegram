package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class CoverPhotoUser {

    @SerializedName("id")
    private String id;
    @SerializedName("username")
    private String username;
    @SerializedName("name")
    private String name;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("instagram_username")
    private String instagramUsername;
    @SerializedName("twitter_username")
    private String twitterUsername;
    @SerializedName("portfolio_url")
    private String portfolioUrl;
    @SerializedName("profile_image")
    private ProfileImage profileImage;
    @SerializedName("links")
    private UserLinks links;
}
