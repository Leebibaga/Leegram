package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.leegram.Const;
import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.Folder;
import com.example.leegram.model.PhotoItem;

import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;

public class FolderFragment extends Fragment {

    public interface OnLongClickPhotoListener {
        void onPhotoClicked(String itemClicked);
    }

    //view
    private View rootView;
    private RecyclerView favoritePhotos;
    private TextView noDataText;
    private ImageView enlargedPhoto;

    //data
    private List<PhotoItem> photoItems = new LinkedList<>();
    private List<String> photoDir = new LinkedList<>();
    private String folderName;

    //adapter
    private FavoritePhotosAdapter favoritePhotosAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folder, menu);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(folderName);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.add:
                navigateToSearchScreen();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_folder, container, false);
            findViews();
            favoritePhotosAdapter = new FavoritePhotosAdapter();
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
            favoritePhotos.setAdapter(favoritePhotosAdapter);
        }
        enlargedPhoto.setOnClickListener(v -> {
            enlargedPhoto.setVisibility(View.GONE);
            favoritePhotos.setVisibility(View.VISIBLE);
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
        if (photoItems.isEmpty()) {
            noDataText.setVisibility(View.VISIBLE);
            favoritePhotos.setVisibility(View.GONE);
        } else {
            noDataText.setVisibility(View.GONE);
            favoritePhotos.setVisibility(View.VISIBLE);
        }
    }

    private void findViews() {
        favoritePhotos = rootView.findViewById(R.id.favorite_photos);
        noDataText = rootView.findViewById(R.id.add_photos_text);
        enlargedPhoto = rootView.findViewById(R.id.enlarged_photo);
    }

    private List<Bitmap> getPhotosFromPhone() {
        List<Bitmap> downloadedPhotos = new LinkedList<>();
        int size = photoItems.size();
        for (int index = 0; index < size; index++) {
            String pathToPicture = photoItems.get(index).getPicture();
            downloadedPhotos.add(BitmapFactory.decodeFile(pathToPicture));
        }
        return downloadedPhotos;
    }

    private void setPhotosURLs() {
        int size = photoItems.size();
        photoDir.clear();
        for (int index = 0; index < size; index++) {
            photoDir.add(photoItems.get(index).getPicture());
        }
    }

    private void updateData() {
        try (Realm realm = Realm.getDefaultInstance()) {
            Folder folderItem = realm
                    .where(Folder.class)
                    .equalTo("id", getArguments().getString(Const.FOLDER_ID))
                    .findFirst();
            folderName = folderItem.getFolderName();
            photoItems.clear();
            photoItems.addAll(realm.copyFromRealm(folderItem.getPhotoItems().sort("position")));
            setPhotosURLs();
            favoritePhotosAdapter.setPhotos();
        }
    }

    private void navigateToSearchScreen() {
        Bundle bundle = new Bundle();
        bundle.putString(Const.FOLDER_ID, getArguments().getString(Const.FOLDER_ID));
        SearchPhotosFragment searchPhotosFragment = new SearchPhotosFragment();
        searchPhotosFragment.setArguments(bundle);
        ((MainActivity) getActivity()).showOtherFragment(searchPhotosFragment);
    }

    private void navigateToEditMode(String photoDir) {
        Bundle bundle = new Bundle();
        bundle.putString(Const.FOLDER_ID, getArguments().getString(Const.FOLDER_ID));
        bundle.putString(Const.PHOTO_DIR, photoDir);
        EditFavoriteListFragment editFavoriteListFragment = new EditFavoriteListFragment();
        editFavoriteListFragment.setArguments(bundle);
        ((MainActivity) getActivity()).showOtherFragment(editFavoriteListFragment);
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
                enlargedPhoto.setImageBitmap(photos.get(position));
                enlargedPhoto.setVisibility(View.VISIBLE);
                favoritePhotos.setVisibility(View.GONE);
            });
            viewHolder.photo.setOnLongClickListener(view -> {
                navigateToEditMode(photoDir.get(position));
                return true;
            });

        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setPhotos() {
            this.photos.clear();
            this.photos.addAll(getPhotosFromPhone());
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
