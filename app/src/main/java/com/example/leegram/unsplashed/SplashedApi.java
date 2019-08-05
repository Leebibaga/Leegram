package com.example.leegram.unsplashed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SplashedApi {

    @GET("/search/collections")
    Call<Photo> getPhotos(@Query("query") String query, @Query("page") int numberOfPages, @Query("per_page") int NumberOfItemsPerPage);
}
