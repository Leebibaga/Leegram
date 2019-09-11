package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.leegram.Const;
import com.example.leegram.model.Folder;
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
import io.realm.RealmList;

public class SearchPhotosFragment extends Fragment implements PhotosDownloader.PhotoDownloadCallback {

    // view
    private View rootView;
    private RecyclerView listOfPhotos;
    private View skeletonLayout;
    private View spinner;
    private EditText searchPhoto;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    // data
    private PhotosDownloader photosDownloader;
    private List<String> photoIDs = new LinkedList<>();
    private Runnable delayCounter;
    private Handler handleSpinner = new Handler();
    private boolean isItemClicked = false;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // adapters
    private PhotoSearchListAdapter searchPhotosListAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_search_photos, container, false);
            spinner = rootView.findViewById(R.id.spinner);
            listOfPhotos = rootView.findViewById(R.id.photos_list);
            skeletonLayout = rootView.findViewById(R.id.parentShimmerLayout);
            initUI();
        }
        toggleSoftKeyboard(true);
        initScrollListener();
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
        return rootView;
    }

    private void initScrollListener() {
        listOfPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int[] lastPlaceShown = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
                if (!isLoading && !photoIDs.isEmpty() && !isLastPage) {
                    if (staggeredGridLayoutManager != null && lastPlaceShown[lastPlaceShown.length -1] >=  photoIDs.size() - 1) {
                        photosDownloader.start(searchPhoto.getText().toString());
                        isLoading = true;
                    }
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void initUI() {
        listOfPhotos.setVisibility(View.GONE);
        searchPhotosListAdapter = new PhotoSearchListAdapter();
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        listOfPhotos.setLayoutManager(staggeredGridLayoutManager);
        listOfPhotos.setAdapter(searchPhotosListAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.search_bar);
        searchPhoto = actionBar.getCustomView().findViewById(R.id.search_photos_bar);
        searchBarAction();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isItemClicked) {
            menu.findItem(R.id.clear_search).setVisible(true);
            menu.findItem(R.id.add_chosen_photos).setVisible(true);
        } else {
            menu.findItem(R.id.clear_search).setVisible(false);
            menu.findItem(R.id.add_chosen_photos).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.add_chosen_photos:
                savePhotos();
                break;
            case R.id.clear_search:
                clearChoices();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchBarAction() {
        searchPhoto.requestFocus();
        searchPhoto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (delayCounter == null) {
                    delayCounter = () -> {
                        if (!searchPhoto.getText().toString().isEmpty()) {
                            downloadPhotos(searchPhoto.getText().toString());
                        }
                    };
                }
                handleSpinner.removeCallbacks(delayCounter);
                handleSpinner.postDelayed(delayCounter, 600);
            }
        });
    }

    public void toggleSoftKeyboard(boolean openKeyBoard) {
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (openKeyBoard) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(searchPhoto.getWindowToken(), 0);
        }
    }

    public void downloadPhotos(String text) {
        skeletonLayout.setVisibility(View.VISIBLE);
        if (photosDownloader != null) {
            photosDownloader.stop();
        }
        photosDownloader = new PhotosDownloader(this);
        isLastPage = photosDownloader.start(text);
    }

    public void clearChoices() {
        searchPhotosListAdapter.getSelectedPhotos().clear();
        searchPhotosListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void setImages(List<Bitmap> downloadedPhotos) {
        toggleSoftKeyboard(false);
        skeletonLayout.setVisibility(View.GONE);
        listOfPhotos.setVisibility(View.VISIBLE);
        searchPhotosListAdapter.setPhotos(downloadedPhotos);
        searchPhotosListAdapter.notifyDataSetChanged();
        isLoading = false;
    }

    @Override
    public void setIDs(List<String> ids) {
        photoIDs.addAll(ids);
    }

    public void savePhotos() {
        spinner.setVisibility(View.VISIBLE);
        new PhotoDataSaving().execute();
    }

    public void doneSavingPhotos() {
        spinner.setVisibility(View.GONE);
        getActivity().onBackPressed();
    }

    class PhotoSearchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_IMAGE = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private List<Bitmap> photos;
        private List<String> selected;
        private LayoutInflater inflater;

        PhotoSearchListAdapter() {
            photos = new LinkedList<>();
            selected = new LinkedList<>();
            inflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            if(viewType == VIEW_TYPE_IMAGE) {
                return new PictureHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
            } else {
                return new LoadingViewHolder(inflater.inflate(R.layout.lazy_loading_progress_bar, viewGroup, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position + 1 == getItemCount() ? VIEW_TYPE_LOADING : VIEW_TYPE_IMAGE;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
            if(viewHolder instanceof PictureHolder) {
                populateItemRows((PictureHolder) viewHolder, position);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void populateItemRows(PictureHolder viewHolder, int position) {
            final String itemTouched = photoIDs.get(position);
            viewHolder.photo.setImageBitmap(photos.get(position));
            if (selected.contains(itemTouched)) {
                highlightView(viewHolder);
            } else {
                unhighlightView(viewHolder);
            }
            viewHolder.photo.setOnClickListener(view -> {
                if (selected.contains(itemTouched)) {
                    unhighlightView(viewHolder);
                    selected.remove(itemTouched);
                } else {
                    highlightView(viewHolder);
                    selected.add(itemTouched);
                }

                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                isItemClicked = selected.size() > 0;
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
            holder.photo.setPadding(0, 0, 0, 0);
        }

        @Override
        public int getItemCount() {
            if (isLastPage) {
                return photos.size();
            }
            return photos.size() + 1;
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

        private class PictureHolder extends RecyclerView.ViewHolder {
            ImageView photo;
            PictureHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo_item);
            }
        }

        private class LoadingViewHolder extends RecyclerView.ViewHolder {
            View progressBar;

            LoadingViewHolder(@NonNull View itemView) {
                super(itemView);
                progressBar = itemView.findViewById(R.id.lazy_progress_bar);
            }
        }
    }

    public class PhotoDataSaving extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try (Realm realm = Realm.getDefaultInstance()) {
                Folder result = realm.where(Folder.class).equalTo("id", getArguments().getString(Const.FOLDER_ID)).findFirst();
                long maxPosition;
                if (result.getPhotoItems().isEmpty()) {
                    maxPosition = 0;
                } else {
                    maxPosition = (long) result.getPhotoItems().max("position");
                }
                RealmList<PhotoItem> photoItems = new RealmList<>();
                List<String> selectedPhotos = searchPhotosListAdapter.getSelectedPhotos();
                for (String string : selectedPhotos) {
                    maxPosition ++;
                    final PhotoItem item = new PhotoItem();
                    item.setPictureURL(string);
                    int index = photoIDs.indexOf(string);
                    item.setPicture(savePhoto(searchPhotosListAdapter.getPhotos().get(index), photoIDs.get(index)));
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
                    item.setDate(dateFormat.format(new Date(System.currentTimeMillis())));
                    item.setPosition((int) maxPosition);
                    photoItems.add(item);
                }
                realm.executeTransaction(realm1 -> {
                    result.getPhotoItems().addAll(photoItems);
                    realm1.copyToRealmOrUpdate(result);
                });
            }
            return null;
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
