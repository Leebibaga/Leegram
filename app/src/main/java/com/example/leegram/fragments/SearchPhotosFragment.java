package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.leegram.activities.MainActivity;
import com.example.leegram.others.OnItemClickedListener;
import com.example.leegram.others.PhotosDownloader;
import com.example.leegram.R;
import com.example.leegram.model.PhotoItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

public class SearchPhotosFragment extends Fragment implements PhotosDownloader.PhotoDownloadCallback {

    public interface OnFinishedSearchListener {
        void onDownloadFinished();
    }

    // view
    private View rootView;
    private RecyclerView listOfPhotos;
    private View skeletonLayout;
    private ProgressBar spinner;

    // data
    private OnItemClickedListener mClickListener;
    private OnFinishedSearchListener mFinishedSearchListener;
    private PhotosDownloader photosDownloader;
    private List<String> photoIDs = new LinkedList<>();
    private OnItemClickedListener mOnItemClickedListener;

    // adapters
    private SearchPhotosListAdapter searchPhotosListAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            Log.e("alex", "onCreateView: ");
            rootView = inflater.inflate(R.layout.fragment_search_photos, container, false);
            spinner = rootView.findViewById(R.id.spinner);
            listOfPhotos = rootView.findViewById(R.id.photos_list);
            skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
            initUI();
        }
        mOnItemClickedListener.setActionBarMode(MainActivity.ActionBarMode.SEARCH);
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
        return rootView;
    }

    @SuppressLint("NewApi")
    private void initUI() {
        searchPhotosListAdapter = new SearchPhotosListAdapter();
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        listOfPhotos.setLayoutManager(staggeredGridLayoutManager);
        listOfPhotos.setAdapter(searchPhotosListAdapter);
    }

    public void downloadPhotos(String text) {
        listOfPhotos.setVisibility(View.INVISIBLE);
        skeletonLayout.setVisibility(View.VISIBLE);
        if (photosDownloader != null) {
            photosDownloader.stop();
        }
        photosDownloader = new PhotosDownloader(this);
        photosDownloader.start(text);

    }

    public void clearChoices() {
        searchPhotosListAdapter.getSelectedPhotos().clear();
        searchPhotosListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClickListener = (OnItemClickedListener) context;
        mFinishedSearchListener = (OnFinishedSearchListener) context;
        mOnItemClickedListener = (OnItemClickedListener) context;
    }

    @Override
    public void setImages(List<Bitmap> downloadedPhotos) {
        mFinishedSearchListener.onDownloadFinished();
        skeletonLayout.setVisibility(View.GONE);
        listOfPhotos.setVisibility(View.VISIBLE);
        searchPhotosListAdapter.setPhotos(downloadedPhotos);
        searchPhotosListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setIDs(List<String> ids) {
        searchPhotosListAdapter.setPhotosIDs(ids);
    }

    public void savePhotos() {
        spinner.setVisibility(View.VISIBLE);
        new PhotoDataSaving().execute();
    }

    public void doneSavingPhotos(){
        spinner.setVisibility(View.GONE);
        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    class SearchPhotosListAdapter extends RecyclerView.Adapter<SearchPhotosListAdapter.PictureHolder> {

        private List<Bitmap> photos;
        private List<String> selected;
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
            final String itemTouched = photoIDs.get(position);
            pictureHolder.photo.setImageBitmap(photos.get(position));
            if (selected.contains(itemTouched)) {
                highlightView(pictureHolder);
            } else {
                unhighlightView(pictureHolder);
            }

            pictureHolder.photo.setOnClickListener(view -> {
                if (selected.contains(itemTouched)) {
                    unhighlightView(pictureHolder);
                    selected.remove(itemTouched);
                } else {
                    highlightView(pictureHolder);
                    selected.add(itemTouched);
                }

                if (selected.size() > 0) {
                    mClickListener.setActionBarMode(MainActivity.ActionBarMode.SEARCH_ITEM_CLICKED);
                    Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                } else {
                    mClickListener.setActionBarMode(MainActivity.ActionBarMode.SEARCH);
                    Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark));
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

        List<Bitmap> getPhotos() {
            return photos;
        }

        public List<String> getSelectedPhotos() {
            return selected;
        }

        void setPhotosIDs(List<String> ids) {
            photoIDs = ids;
        }

        private class PictureHolder extends RecyclerView.ViewHolder {

            ImageView photo;

            PictureHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo_item);
            }
        }
    }

    public class PhotoDataSaving extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            int maxPosition = (int) getMaxPosition() + 1;
            List<PhotoItem> photoItems = new LinkedList<>();
            List<String> selectedPhotos = searchPhotosListAdapter.getSelectedPhotos();
            for (String string : selectedPhotos) {
                final PhotoItem item = new PhotoItem();
                item.setPictureURL(string);
                int index = photoIDs.indexOf(string);
                item.setPicture(savePhoto(searchPhotosListAdapter.getPhotos().get(index), photoIDs.get(index)));
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
                item.setDate(dateFormat.format(new Date(System.currentTimeMillis())));
                item.setPosition(maxPosition);
                photoItems.add(item);
            }
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> realm1.insertOrUpdate(photoItems));
            }
            return null;
        }

        private long getMaxPosition() {
            long maxPosition;
            try (Realm realm = Realm.getDefaultInstance()) {
                Number results = realm
                        .where(PhotoItem.class)
                        .max("position");
                if (results == null) {
                    maxPosition = 0;
                } else {
                    maxPosition = (long) results;
                }
            }
            return maxPosition;
        }

        private String savePhoto(Bitmap photo, String photoName) {
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directoryToInternalStorage = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File directoryToPic = new File(directoryToInternalStorage, photoName);
            FileOutputStream saveToFile = null;
            try {
                saveToFile = new FileOutputStream(directoryToPic);
                if (!photo.compress(Bitmap.CompressFormat.PNG, 100, saveToFile)) {
                    Toast.makeText(getContext(), "Can't save photo", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    saveToFile.flush();
                    saveToFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directoryToPic.getAbsolutePath();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            doneSavingPhotos();
        }
    }

}
