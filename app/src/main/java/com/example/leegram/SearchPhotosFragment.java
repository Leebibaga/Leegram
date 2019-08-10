package com.example.leegram;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SearchPhotosFragment extends Fragment implements SearchView.OnQueryTextListener, PhotosDownloader.FinishDownloadingPhotos {

    private RecyclerView listOfPhotos;
    private SearchPhotosListAdapter searchPhotosListAdapter;
    private Handler handleSpiiner = new Handler();
    private Runnable delayCounter;
    private SearchView searchPhotoBar;

    public SearchPhotosFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_search_photos, container, false);
        searchPhotoBar = rootView.findViewById(R.id.search_photos_bar);
        listOfPhotos = rootView.findViewById(R.id.photos_list);
        searchPhotosListAdapter = new SearchPhotosListAdapter();
        searchPhotoBar.setIconifiedByDefault(false);
        searchPhotoBar.setSubmitButtonEnabled(true);
        searchPhotoBar.setOnQueryTextListener(this);
        return rootView;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (delayCounter == null) {
            delayCounter = new Runnable() {
                @Override
                public void run() {
                    if(!TextUtils.isEmpty(searchPhotoBar.getQuery())) {
                        new PhotosDownloader(getContext(), SearchPhotosFragment.this, newText);
                    }
                }
            };
        }
        
        handleSpiiner.removeCallbacks(delayCounter);
        handleSpiiner.postDelayed(delayCounter, 500);
        return true;
    }

    @Override
    public void setImages(List<Bitmap> downloadedPhotos) {
        searchPhotosListAdapter.setPhotos(downloadedPhotos);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        listOfPhotos.setLayoutManager(staggeredGridLayoutManager);
        listOfPhotos.setAdapter(searchPhotosListAdapter);
    }

    class SearchPhotosListAdapter extends RecyclerView.Adapter<SearchPhotosListAdapter.PictureHolder> {
        private List<Bitmap> photos, selected;
        private LayoutInflater inflater;

        SearchPhotosListAdapter() {
          photos = new LinkedList<>();
          selected = new LinkedList<>();
          inflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public PictureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new PictureHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull final PictureHolder pictureHolder, int position) {
            final Bitmap itemTouched = photos.get(position);
            pictureHolder.photo.setImageBitmap(itemTouched);
            pictureHolder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selected.contains(itemTouched)) {
                        selected.remove(itemTouched);
                        unhighlightView(pictureHolder);
                    } else {
                        selected.add(itemTouched);
                        highlightView(pictureHolder);
                    }
                }
            });

            if (selected.contains(itemTouched))
                highlightView(pictureHolder);
            else
                unhighlightView(pictureHolder);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
            holder.photo.setPadding(10,10,10,10);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void unhighlightView(PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), android.R.color.transparent));
            holder.photo.setPadding(-10,-10,-10,-10);
        }


        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setPhotos(List<Bitmap> photos) {
            this.photos = photos;
        }

        private class PictureHolder extends RecyclerView.ViewHolder {
            ImageView photo;

            public PictureHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo_item);
            }
        }
    }

}

