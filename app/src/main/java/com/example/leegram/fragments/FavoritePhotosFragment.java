package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.leegram.R;
import com.example.leegram.activities.ImageScreenActivity;
import com.example.leegram.model.PhotoItem;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import io.realm.Realm;
import io.realm.RealmResults;

public class FavoritePhotosFragment extends Fragment {

    public interface OnButtonsListener {
        void onNoPictureListener();
        void onAddButtonListener();
    }

    //view
    private RecyclerView favoritePhotos;
    private FloatingActionButton addPhoto;
    private View rootView;

    //data
    private RealmResults<PhotoItem> photoItems;
    private OnButtonsListener onButtonsListener;
    private ShimmerFrameLayout skeletonLayout;
    private List<String> photosURLs = new LinkedList<>();

    //adapter
    private FavoritePhotosAdapter favoritePhotosAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onButtonsListener = (OnButtonsListener) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_favorite_photos, container, false);
            findViews();
            favoritePhotosAdapter = new FavoritePhotosAdapter();
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
            favoritePhotos.setAdapter(favoritePhotosAdapter);
            addPhoto.setOnClickListener(v -> {
                if (photoItems.isEmpty()) {
                    onButtonsListener.onNoPictureListener();
                } else {
                    onButtonsListener.onAddButtonListener();
                }
            });
            updateData();
        }

        skeletonLayout.setVisibility(View.GONE);
        return rootView;
    }

    private void findViews() {
        favoritePhotos = rootView.findViewById(R.id.favorite_photos);
        skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
        addPhoto = rootView.findViewById(R.id.fab);
    }

    private List<Bitmap> convertBitmap() {
        List<Bitmap> downloadedPhotos = new LinkedList<>();
        int size = photoItems.size();
        for (int index = 0; index < size; index++) {
            downloadedPhotos.add(BitmapFactory.decodeByteArray
                    (photoItems.get(index).getPicture(), 0, (photoItems.get(index).getPicture().length)));
        }
        return downloadedPhotos;
    }

    private void setPhotosURLs() {
        int size = photoItems.size();
        photosURLs.clear();
        for (int index = 0; index < size; index++) {
            photosURLs.add(photoItems.get(index).getPictureURL());
        }
    }

    private void updateData() {
        Realm realm = Realm.getDefaultInstance();
        photoItems = realm
                .where(PhotoItem.class)
                .sort("position")
                .findAll();
        setPhotosURLs();
        favoritePhotosAdapter.setPhotos();
        photoItems.addChangeListener((photoItems1, changeSet) -> {
            photoItems.sort("position");
            setPhotosURLs();
            favoritePhotosAdapter.setPhotos();
        });

    }

    public class FavoritePhotosAdapter extends RecyclerView.Adapter<FavoritePhotosAdapter.PhotoHolder> {

        private List<Bitmap> photos;
        private LayoutInflater inflater;

        public FavoritePhotosAdapter() {
            photos = new LinkedList<>();
            inflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new PhotoHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
        }

        @SuppressLint("NewApi")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull final PhotoHolder viewHolder, int position) {
            viewHolder.photo.setImageBitmap(photos.get(position));
            viewHolder.photo.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), ImageScreenActivity.class);
                intent.putExtra("image", photosURLs.get(position));
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(Objects.requireNonNull(getActivity()),
                                viewHolder.photo,
                                getString(R.string.image_transition));
                startActivity(intent, options.toBundle());
                getActivity().overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
            });

        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setPhotos() {
            this.photos.clear();
            this.photos.addAll(convertBitmap());
            notifyDataSetChanged();
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
