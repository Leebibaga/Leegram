package com.example.leegram.unsplashed;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnsplashedPhotos {

    @SerializedName("total")
    private int totalPhotosFound;
    @SerializedName("total_pages")
    private int totalPages;
    @SerializedName("results")
    List<Photo> results;
}
