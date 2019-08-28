package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.leegram.others.PhotosDownloader;
import com.example.leegram.R;
import com.example.leegram.model.PhotoItem;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class SearchPhotosFragment extends Fragment implements PhotosDownloader.PhotoDownloadCallback {

    public interface OnClickAddButtonListener {
        void onClickAddButton();
    }

    // view
    private View rootView;
    private RecyclerView listOfPhotos;
    private Button addToFavorites;
    private EditText searchPhotoBar;
    private View skeletonLayout;

    // data
    private Runnable delayCounter;
    private Handler handleSpinner = new Handler();
    private OnClickAddButtonListener mClickListener;

    // adapters
    private SearchPhotosListAdapter searchPhotosListAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_search_photos, container, false);
            searchPhotoBar = rootView.findViewById(R.id.search_photos_bar);
            listOfPhotos = rootView.findViewById(R.id.photos_list);
            addToFavorites = rootView.findViewById(R.id.add_to_favorites);
            skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
        }

        initUI();

        return rootView;
    }

    @SuppressLint("NewApi")
    private void initUI() {
        searchPhotosListAdapter = new SearchPhotosListAdapter();
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        listOfPhotos.setLayoutManager(staggeredGridLayoutManager);
        listOfPhotos.setAdapter(searchPhotosListAdapter);
        addToFavorites.setOnClickListener(v -> {
            setPhotoInRealm(searchPhotosListAdapter.getSelectedPhotos());
            addToFavorites.setVisibility(View.GONE);
            searchPhotoBar.getText().clear();
            mClickListener.onClickAddButton();
        });
        showSoftKeyboard(searchPhotoBar);
        searchPhotoBar.addTextChangedListener(new TextWatcher() {
            String textBeforeChanged;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textBeforeChanged = searchPhotoBar.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (delayCounter == null) {
                    delayCounter = () -> {
                        if (!searchPhotoBar.getText().toString().isEmpty()) {
                            skeletonLayout.setVisibility(View.VISIBLE);
                            new PhotosDownloader(SearchPhotosFragment.this, searchPhotoBar.getText().toString());
                        }
                    };
                }
                handleSpinner.removeCallbacks(delayCounter);
                handleSpinner.postDelayed(delayCounter, 600);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setPhotoInRealm(List<String> selectedPhotos) {
        updatePositionInRealm(selectedPhotos.size());
        long position = getMaxPosition();
        try (Realm realm = Realm.getDefaultInstance()) {
            for (String photoURL : selectedPhotos) {
                final PhotoItem photoItem = new PhotoItem();
                photoItem.setPictureURL(photoURL);
                photoItem.setApi("unsplashed");
                int index = searchPhotosListAdapter.getPhotosURLs().indexOf(photoURL);
                photoItem.setPicture(photoToByte(searchPhotosListAdapter.getPhotos().get(index)));
                photoItem.setDate(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").toString());
                photoItem.setPosition((int) position);
                realm.executeTransaction(realm1 -> realm1.insertOrUpdate(photoItem));
            }
        }
    }

    private long getMaxPosition() {
        long maxPosition;
        try (Realm realm = Realm.getDefaultInstance()) {
            Number results = realm
                    .where(PhotoItem.class)
                    .max("position");
            if (results == null){
                maxPosition = 0;
            }else {
                maxPosition = (long) results;
            }
        }
        return maxPosition;
    }

    private void updatePositionInRealm(int numberOfNewItems) {
        List<PhotoItem> photoItems = new LinkedList<>();
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<PhotoItem> results = realm
                    .where(PhotoItem.class)
                    .findAll();
            photoItems.addAll(realm.copyFromRealm(results));
            for (PhotoItem photoItem : photoItems) {
                int oldPosition = photoItem.getPosition();
                photoItem.setPosition(oldPosition + numberOfNewItems);
            }
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(photoItems));
        }
    }

    public byte[] photoToByte(Bitmap photo) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClickListener = (OnClickAddButtonListener) context;
    }

    @Override
    public void setImages(List<Bitmap> downloadedPhotos) {
        skeletonLayout.setVisibility(View.GONE);
        searchPhotosListAdapter.setPhotos(downloadedPhotos);
        searchPhotosListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setURLs(List<String> urls) {
        searchPhotosListAdapter.setPhotosURLs(urls);
    }

    class SearchPhotosListAdapter extends RecyclerView.Adapter<SearchPhotosListAdapter.PictureHolder> {
        private List<Bitmap> photos;
        private List<String> photosURLs, selected;
        private LayoutInflater inflater;

        SearchPhotosListAdapter() {
            photos = new LinkedList<>();
            photosURLs = new LinkedList<>();
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
            final String itemTouched = photosURLs.get(position);
            pictureHolder.photo.setImageBitmap(photos.get(position));
            pictureHolder.photo.setOnClickListener(view -> {
                if (selected.contains(itemTouched)) {
                    selected.remove(itemTouched);
                    unhighlightView(pictureHolder);
                } else {
                    selected.add(itemTouched);
                    highlightView(pictureHolder);
                }

                if (selected.size() > 0) {
                    addToFavorites.setVisibility(View.VISIBLE);
                } else {
                    addToFavorites.setVisibility(View.GONE);
                }
            });

            if (selected.contains(itemTouched)) {
                highlightView(pictureHolder);
            } else {
                unhighlightView(pictureHolder);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
            holder.photo.setPadding(10, 10, 10, 10);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void unhighlightView(PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), android.R.color.transparent));
            holder.photo.setPadding(-10, -10, -10, -10);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setPhotos(List<Bitmap> photos) {
            this.photos = photos;
        }

        public List<Bitmap> getPhotos() {
            return photos;
        }

        public List<String> getSelectedPhotos() {
            return selected;
        }

        public void setPhotosURLs(List<String> urls) {
            this.photosURLs = urls;
        }

        public List<String> getPhotosURLs() {
            return photosURLs;
        }

        private class PictureHolder extends RecyclerView.ViewHolder {
            ImageView photo;

            PictureHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo_item);
            }
        }


    }
}

