package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class PhotoUser {

    @SerializedName("id")
    private String id;
    @SerializedName("username")
    private String username;
    @SerializedName("name")
    private String name;
    @SerializedName("portfolio_url")
    private String portfolioURL;
    @SerializedName("bio")
    private String bio;
    @SerializedName("profile_image")
    private ProfileImage profileImage;
    @SerializedName("links")
    private PhotoUserLinks photoUserLinks;
}
