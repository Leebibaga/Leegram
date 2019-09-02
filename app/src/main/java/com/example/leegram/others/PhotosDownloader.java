package com.example.leegram.others;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

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

    private String TOKEN = "210590bd55fa23badf70b162392aa62e3bc62b7029e01eb0f9db858abc0c7cc6";
    Call<UnsplashedPhotos> getResults;

    public interface PhotoDownloadCallback {
        void setImages(List<Bitmap> downloadedPhotos);

        void setURLs(List<String> urls);

    }

    private LinkedList<Bitmap> downloadedPhotos = new LinkedList<>();
    private LinkedList<String> photosUrls = new LinkedList<>();
    private PhotoDownloadCallback finishDownloadingPhotos;
    private int pageNumber = 1;


    public PhotosDownloader(PhotoDownloadCallback callback) {
        this.finishDownloadingPhotos = callback;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<Bitmap> doInBackground(String... strings) {
        for (String photoUrl : strings) {
            downloadedPhotos.add(downloadPhoto(photoUrl));
            if (isCancelled()) {
                break;
            }
        }
        return downloadedPhotos;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {

    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmap) {
        finishDownloadingPhotos.setImages(bitmap);
        finishDownloadingPhotos.setURLs(photosUrls);
        finishDownloadingPhotos = null;
        photosUrls = null;
    }

    public void start(String query) {
        SplashedApi apiService = new ApiClient().getClient().create(SplashedApi.class);
        getResults = apiService.getPhotos(TOKEN, query, pageNumber, 20);
        getResults.enqueue(new Callback<UnsplashedPhotos>() {
            @Override
            public void onResponse(@NonNull Call<UnsplashedPhotos> call, @NonNull Response<UnsplashedPhotos> response) {
                List<Photo> photos = response.body().getResults();
                if (photos != null && !photos.isEmpty()) {
                    for (Photo photo : photos) {
                        photosUrls.add(photo.getURLs().getRegular());
                    }
                    execute(photosUrls.toArray(new String[photos.size()]));
                    pageNumber++;
                }
            }

            @Override
            public void onFailure(Call<UnsplashedPhotos> call, Throwable t) {
                if (getResults.isCanceled()) {
                    photosUrls.clear();
                }
            }
        });
    }

    public void stop() {
        getResults.cancel();
        cancel(true);
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



