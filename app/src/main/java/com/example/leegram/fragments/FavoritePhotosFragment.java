package com.example.leegram.fragments;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.leegram.PhotosDownloader;
import com.example.leegram.R;
import com.example.leegram.model.PhotoItem;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavoritePhotosFragment extends Fragment implements PhotosDownloader.PhotoDownloadCallback {

    public interface OnButtonsListener {
        void onNoPictureListener();
        void onAddButtonListener();
    }

    private RecyclerView favoritePhotos;
    private List<PhotoItem> photoItems = new LinkedList<>();
    private List<String> photosURLs = new LinkedList<>();
    private FavoritePhotosAdapter favoritePhotosAdapter;
    private Button removePhotos;
    private OnButtonsListener onButtonsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_favorite_photos, container, false);
        favoritePhotosAdapter = new FavoritePhotosAdapter();
        favoritePhotos = rootView.findViewById(R.id.favorite_photos);
        removePhotos = rootView.findViewById(R.id.remove_photos);
        Button addPhotos = rootView.findViewById(R.id.add_more_photos);
        ShimmerFrameLayout skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
        removePhotos.setVisibility(View.GONE);
        onButtonsListener = (OnButtonsListener) rootView.getContext();
        removePhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePhotosFromList();
                removePhotos.setVisibility(View.GONE);
                favoritePhotosAdapter.setSelected();
                if(photoItems.isEmpty()) {
                    onButtonsListener.onNoPictureListener();
                }
            }
        });
        addPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonsListener.onAddButtonListener();
            }
        });
        getResultFromRealm();
        setPhotosURLs();
        new PhotosDownloader( this).execute(photosURLs.toArray(new String[photosURLs.size()]));
        return rootView;
    }

    private void removePhotosFromList() {
        List<String> selected = favoritePhotosAdapter.getSelectedPhotos();
        for (String item : selected) {
            removeFromRealm(item);
            favoritePhotosAdapter.removeItem(photosURLs.indexOf(item));
            photoItems.remove(photosURLs.indexOf(item));
            photosURLs.remove(item);
        }
    }

    private void removeFromRealm(final String url) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    RealmResults<PhotoItem> results = realm
                            .where(PhotoItem.class)
                            .equalTo("picture", url)
                            .findAll();
                    results.deleteAllFromRealm();
                }
            });

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void getResultFromRealm() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            RealmResults<PhotoItem> results = realm
                    .where(PhotoItem.class)
                    .findAll();
            photoItems.addAll(realm.copyFromRealm(results));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void setPhotosURLs() {
        int size = photoItems.size();
        for (int index = 0; index < size; index++) {
            photosURLs.add(photoItems.get(index).getPicture());
        }
    }

    @Override
    public void setImages(List<Bitmap> downloadedPhotos) {
        favoritePhotosAdapter.setPhotos(downloadedPhotos);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
        favoritePhotos.setAdapter(favoritePhotosAdapter);
    }

    @Override
    public void setURLs(List<String> urls) {
    }


    public class FavoritePhotosAdapter extends RecyclerView.Adapter<FavoritePhotosAdapter.PhotoHolder> {

        private List<Bitmap> photos;
        private List<String> selected;
        private LayoutInflater inflater;

        public FavoritePhotosAdapter() {
            photos = new LinkedList<>();
            selected = new LinkedList<>();
            inflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new PhotoHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PhotoHolder viewHolder, int position) {
            final String itemTouched = photoItems.get(position).getPicture();
            viewHolder.photo.setImageBitmap(photos.get(position));
            viewHolder.photo.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View view) {
                    if (selected.contains(itemTouched)) {
                        selected.remove(itemTouched);
                        unhighlightView(viewHolder);
                    } else {
                        selected.add(itemTouched);
                        highlightView(viewHolder);
                    }

                    if (selected.size() > 0) {
                        removePhotos.setVisibility(View.VISIBLE);
                    } else {
                        removePhotos.setVisibility(View.GONE);
                    }
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(PhotoHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
            holder.photo.setPadding(10, 10, 10, 10);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void unhighlightView(PhotoHolder holder) {
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

        List<String> getSelectedPhotos() {
            return selected;
        }

        public void setSelected(){
            selected = null;
            selected = new LinkedList<>();
        }

        public void removeItem(int position) {
            photos.remove(position);
            notifyItemRemoved(position);
        }

        private class PhotoHolder extends RecyclerView.ViewHolder {

            ImageView photo;

            PhotoHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo_item);
            }
        }
    }
}
