package com.example.leegram;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;


import com.example.leegram.unsplashed.Photo;
import com.example.leegram.unsplashed.SplashedApi;
import com.example.leegram.unsplashed.UnsplashedPhotos;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotosDownloader extends AsyncTask<String, Bitmap, List<Bitmap>> {

    public interface FinishDownloadingPhotos {
        void setImages(List<Bitmap> downloadedPhotos);
    }

    private String TOKEN = "210590bd55fa23badf70b162392aa62e3bc62b7029e01eb0f9db858abc0c7cc6";
    private LinkedList<Bitmap> downloadedPhotos = new LinkedList<>();
    private FinishDownloadingPhotos finishDownloadingPhotos;
    private ProgressDialog simpleWaitDialog;
    private int pageNumber = 1;


    PhotosDownloader(Context context, FinishDownloadingPhotos callback, String query) {
        simpleWaitDialog = new ProgressDialog(context);
        this.finishDownloadingPhotos = callback;
        simpleWaitDialog.show();
        getListOfPhotoURLs(query);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<Bitmap> doInBackground(String... strings) {
        for (String photoUrl : strings) {
            downloadedPhotos.add(downloadPhoto(photoUrl));
        }
        return downloadedPhotos;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        simpleWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmap) {
        finishDownloadingPhotos.setImages(bitmap);
        simpleWaitDialog.dismiss();
        finishDownloadingPhotos = null;
        simpleWaitDialog = null;
    }

    private void getListOfPhotoURLs(String query) {
        SplashedApi apiService = new ApiClient().getClient().create(SplashedApi.class);
        Call<UnsplashedPhotos> getResults = apiService.getPhotos( TOKEN, query, pageNumber, 20);
        getResults.enqueue(new Callback<UnsplashedPhotos>() {
            @Override
            public void onResponse(@NonNull Call<UnsplashedPhotos> call, @NonNull Response<UnsplashedPhotos> response) {
                List<Photo> photos = response.body().getResults();
                if (photos != null && !photos.isEmpty()) {
                    LinkedList<String> photosUrls = new LinkedList<>();
                    for (Photo photo : photos) {
                        photosUrls.add(photo.getURLs().getRegular());
                    }
                    execute(photosUrls.toArray(new String[photos.size()]));
                    pageNumber++;
                }
            }

            @Override
            public void onFailure(Call<UnsplashedPhotos> call, Throwable t) {

            }
        });
    }

    private Bitmap downloadPhoto(String url) {
        URL photoURL = null;
        try {
            photoURL = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) photoURL.openConnection();
            return BitmapFactory.decodeStream(httpURLConnection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}


