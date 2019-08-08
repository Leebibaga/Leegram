package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class User {

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
    private UserLinks userLinks;

    public String getId() {
        return id;
    }

    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getTwitterUsername() {
        return twitterUsername;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserLinks getUserLinks() {
        return userLinks;
    }
}
