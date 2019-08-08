package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

public class URLs {

    @SerializedName("raw")
    private String raw;
    @SerializedName("full")
    private String full;
    @SerializedName("regular")
    private String regular;
    @SerializedName("small")
    private String small;
    @SerializedName("thumb")
    private String thumb;

    public String getSmall() {
        return small;
    }

    public String getRaw() {
        return raw;
    }

    public String getRegular() {
        return regular;
    }

    public String getThumb() {
        return thumb;
    }

    public String getFull() {
        return full;
    }
}
