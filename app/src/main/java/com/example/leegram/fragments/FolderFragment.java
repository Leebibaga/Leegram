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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.leegram.App_Configurations;
import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.Folder;
import com.example.leegram.model.PhotoItem;
import com.example.leegram.others.FavoriteItemTouchCallBack;
import com.example.leegram.others.OnStartDragListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class FolderFragment extends Fragment implements OnStartDragListener {

    enum ActionBarMode {
        EDIT, CLICKED_ITEM, NOT_EDIT_MODE;
    }

    //view
    private View rootView;
    private RecyclerView favoritePhotos;
    private TextView noDataText;
    private ImageView enlargedPhoto;

    //data
    private List<PhotoItem> photoItems = new LinkedList<>();
    private List<String> photoDir = new LinkedList<>();
    private RealmList<PhotoItem> deletedPhotoItems = new RealmList<>();
    private String folderName;
    private ItemTouchHelper mItemTouchHelper;
    private ActionBarMode actionBarMode;
    private boolean isSaved = false;

    //adapter
    private FavoritePhotosAdapter favoritePhotosAdapter;
    private EditFavoriteListPhotosAdapter editFavoriteListPhotosAdapter;


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

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
        switch (actionBarMode) {
            case EDIT:
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.clear_edit).setVisible(true);
                menu.findItem(R.id.add).setVisible(false);
                break;
            case CLICKED_ITEM:
                menu.findItem(R.id.trash).setVisible(true);
                menu.findItem(R.id.clear_edit).setVisible(true);
                menu.findItem(R.id.add).setVisible(false);
                break;
            case NOT_EDIT_MODE:
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.add).setVisible(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.add:
                navigateToSearchScreen();
                break;
            case R.id.trash:
                deleteSelectedItems();
                break;
            case R.id.clear_edit:
                editFavoriteListPhotosAdapter.clearData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void saveData() {
        if (isSaved) {
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        } else {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(photoItems));
            }
            actionBarMode = ActionBarMode.NOT_EDIT_MODE;
            getActivity().invalidateOptionsMenu();
            updateData();
            favoritePhotos.setAdapter(favoritePhotosAdapter);
        }
        isSaved = true;
    }

    public void deleteSelectedItems() {
        List<String> selectedPhotos = editFavoriteListPhotosAdapter.getSelected();
        for (int index = 0; index < selectedPhotos.size(); index++) {
            int position = photoDir.indexOf(selectedPhotos.get(index));
            photoDir.remove(selectedPhotos.get(index));
            PhotoItem photoItemToDelete = photoItems.get(position);
            updatePositionAfterRemove(position + 1);
            photoItems.remove(photoItemToDelete);
            editFavoriteListPhotosAdapter.notifyItemRemoved(position);
        }
        deleteFromRealm();
        editFavoriteListPhotosAdapter.setPhotos();
        editFavoriteListPhotosAdapter.clearData();
    }


    public void updatePositionAfterRemove(int position) {
        for (int index = position; index < photoItems.size(); index++) {
            int oldPosition = photoItems.get(index).getPosition();
            photoItems.get(index).setPosition(oldPosition - 1);
        }
    }

    public void deleteFromRealm() {
        try (Realm realm = Realm.getDefaultInstance()) {
            for (PhotoItem deletedPhotoItem : deletedPhotoItems) {
                realm.executeTransaction(realm1 -> {
                    RealmResults<PhotoItem> results = realm1
                            .where(PhotoItem.class)
                            .equalTo("picture", deletedPhotoItem.getPicture())
                            .findAll();
                    results.deleteAllFromRealm();
                });
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_folder, container, false);
            findViews();
            favoritePhotosAdapter = new FavoritePhotosAdapter();
            editFavoriteListPhotosAdapter = new EditFavoriteListPhotosAdapter(this);
            ItemTouchHelper.Callback callback = new FavoriteItemTouchCallBack(editFavoriteListPhotosAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(favoritePhotos);
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
            favoritePhotos.setAdapter(favoritePhotosAdapter);
        }
        enlargedPhoto.setOnClickListener(v -> {
            enlargedPhoto.setVisibility(View.GONE);
            favoritePhotos.setVisibility(View.VISIBLE);
        });

        actionBarMode = ActionBarMode.NOT_EDIT_MODE;
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
                    .equalTo("id", getArguments().getString(App_Configurations.FOLDER_ID))
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
        bundle.putString(App_Configurations.FOLDER_ID, getArguments().getString(App_Configurations.FOLDER_ID));
        SearchPhotosFragment searchPhotosFragment = new SearchPhotosFragment();
        searchPhotosFragment.setArguments(bundle);
        ((MainActivity) getActivity()).showOtherFragment(searchPhotosFragment);
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
                editFavoriteListPhotosAdapter.setPhotos();
                editFavoriteListPhotosAdapter.getSelected().add(photoDir.get(position));
                deletedPhotoItems.add(photoItems.get(position));
                favoritePhotos.setAdapter(editFavoriteListPhotosAdapter);
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


    public class EditFavoriteListPhotosAdapter extends RecyclerView.Adapter<EditFavoriteListPhotosAdapter.PictureHolder> {
        private List<Bitmap> photos;
        private LayoutInflater inflater;
        private List<String> selected;
        private final OnStartDragListener mDragStartListener;


        public EditFavoriteListPhotosAdapter(OnStartDragListener mDragStartListener) {
            photos = new LinkedList<>();
            selected = new LinkedList<>();
            inflater = LayoutInflater.from(getContext());
            this.mDragStartListener = mDragStartListener;
        }

        @NonNull
        @Override
        public EditFavoriteListPhotosAdapter.PictureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new EditFavoriteListPhotosAdapter.PictureHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
        }


        @SuppressLint("ClickableViewAccessibility")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull final EditFavoriteListPhotosAdapter.PictureHolder viewHolder, int position) {
            final String itemTouched = photoDir.get(position);
            viewHolder.photo.setImageBitmap(photos.get(position));
            if (selected.contains(itemTouched)) {
                highlightView(viewHolder);
            } else {
                unhighlightView(viewHolder);
            }
            if (selected.size() > 0) {
                actionBarMode = ActionBarMode.CLICKED_ITEM;
            } else {
                actionBarMode = ActionBarMode.EDIT;
            }
            getActivity().invalidateOptionsMenu();

            viewHolder.photo.setOnClickListener(V -> {
                viewHolder.photo.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(viewHolder);
                    }
                    return true;
                });
            });
            viewHolder.photo.setOnClickListener(view -> {
                PhotoItem photoItemToDelete = photoItems.get(position);
                if (selected.contains(itemTouched)) {
                    selected.remove(itemTouched);
                    deletedPhotoItems.remove(photoItemToDelete);
                    unhighlightView(viewHolder);
                } else {
                    selected.add(itemTouched);
                    deletedPhotoItems.add(photoItemToDelete);
                    highlightView(viewHolder);
                }

                if (selected.size() > 0) {
                    actionBarMode = ActionBarMode.CLICKED_ITEM;
                } else {
                    actionBarMode = ActionBarMode.EDIT;
                }
                getActivity().invalidateOptionsMenu();
            });
        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(EditFavoriteListPhotosAdapter.PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark));
            holder.photo.setPadding(10, 10, 10, 10);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void unhighlightView(EditFavoriteListPhotosAdapter.PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), android.R.color.transparent));
            holder.photo.setPadding(0, 0, 0, 0);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setPhotos() {
            photos.clear();
            photos.addAll(getPhotosFromPhone());
            notifyDataSetChanged();
        }

        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(photos, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(photos, i, i - 1);
                }
            }
            updateItemsPositions(fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        private void updateItemsPositions(int startPosition, int targetPosition) {
            photoItems.get(startPosition).setPosition(targetPosition);
            photoItems.get(targetPosition).setPosition(startPosition);
            PhotoItem photoItem = photoItems.get(startPosition);
            photoItems.set(startPosition, photoItems.get(targetPosition));
            photoItems.set(targetPosition, photoItem);
        }

        private List<String> getSelected() {
            return selected;
        }

        void clearData() {
            selected.clear();
            deletedPhotoItems.clear();
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
