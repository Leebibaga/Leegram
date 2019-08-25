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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.leegram.activities.EditFavoriteListActivity;
import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;
import com.example.leegram.activities.ImageScreenActivity;
import com.example.leegram.model.PhotoItem;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FavoritePhotosFragment extends Fragment {

    public interface OnButtonsListener {
        void onNoPictureListener();

        void onAddButtonListener();
    }
    //view
    private RecyclerView favoritePhotos;
    private Button removePhotos;
    private Button addPhotos;
    private View rootView;

    //data
    private List<PhotoItem> photoItems = new LinkedList<>();
    private OnButtonsListener onButtonsListener;
    private ShimmerFrameLayout skeletonLayout;
    private CommunicateWithRealm communicateWithRealm;
    private List<String> photosURLs = new LinkedList<>();

    //adapter
    private FavoritePhotosAdapter favoritePhotosAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onButtonsListener = (OnButtonsListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_favorite_photos, container, false);
            findViews();
            communicateWithRealm = new CommunicateWithRealm();
            favoritePhotosAdapter = new FavoritePhotosAdapter();
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
            favoritePhotos.setAdapter(favoritePhotosAdapter);
            removePhotos.setOnClickListener(v -> {
                removePhotosFromList();
                removePhotos.setVisibility(View.GONE);
                favoritePhotosAdapter.setSelected();
                if (photoItems.isEmpty()) {
                    onButtonsListener.onNoPictureListener();
                    favoritePhotosAdapter.setSelected();
                }
            });
            addPhotos.setOnClickListener(v -> {
                onButtonsListener.onAddButtonListener();
            });
        }
        photoItems.clear();
        photoItems.addAll(communicateWithRealm.getPhotoItems());
        setPhotosURLs();
        favoritePhotosAdapter.setPhotos();
        skeletonLayout.setVisibility(View.GONE);
        return rootView;
    }

    private void findViews() {
        favoritePhotos = rootView.findViewById(R.id.favorite_photos);
        removePhotos = rootView.findViewById(R.id.remove_photos);
        addPhotos = rootView.findViewById(R.id.add_more_photos);
        skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
    }

    private List<Bitmap> convertBitmap() {
        List<Bitmap> downloadedPhotos = new LinkedList<>();
        for (PhotoItem photoItem : photoItems) {
            downloadedPhotos.add(BitmapFactory.decodeByteArray
                    (photoItem.getPicture(), 0, photoItem.getPicture().length));
        }
        return downloadedPhotos;
    }

    private void removePhotosFromList() {
        List<String> selected = favoritePhotosAdapter.getSelectedPhotos();
        for (String item : selected) {
            communicateWithRealm.removeFromRealm("pictureURL", item);
            favoritePhotosAdapter.removeItem(photosURLs.indexOf(item));
            photoItems.remove(photosURLs.indexOf(item));
            photosURLs.remove(item);
        }
    }

    private void setPhotosURLs() {
        int size = photoItems.size();
        photosURLs.clear();
        for (int index = 0; index < size; index++) {
            photosURLs.add(photoItems.get(index).getPictureURL());
        }
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

            viewHolder.photo.setOnLongClickListener(V -> {
                Intent intent = new Intent(getActivity(), EditFavoriteListActivity.class);
                startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                return true;
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

        List<String> getSelectedPhotos() {
            return selected;
        }

        public void setSelected() {
            selected.clear();
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
