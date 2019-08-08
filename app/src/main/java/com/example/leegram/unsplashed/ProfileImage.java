package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class ProfileImage {

    @SerializedName("small")
    private String small;
    @SerializedName("medium")
    private String medium;
    @SerializedName("large")
    private String large;

    public String getLarge() {
        return large;
    }

    public String getMedium() {
        return medium;
    }

    public String getSmall() {
        return small;
    }
}
