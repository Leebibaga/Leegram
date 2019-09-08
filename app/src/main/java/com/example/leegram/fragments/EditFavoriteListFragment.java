package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.PhotoItem;
import com.example.leegram.others.FavoriteItemTouchCallBack;
import com.example.leegram.others.OnItemClickedListener;
import com.example.leegram.others.OnStartDragListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class EditFavoriteListFragment extends Fragment implements OnStartDragListener {

    //view
    private RecyclerView favoritePhotos;
    private View rootView;

    //data
    private List<PhotoItem> photoItems = new LinkedList<>();
    private RealmList<PhotoItem> deletedPhotoItems = new RealmList<>();
    private List<String> photosDir = new LinkedList<>();
    private ItemTouchHelper mItemTouchHelper;
    private OnItemClickedListener mClickListener;
    private String photoToHighlight;
    private OnItemClickedListener mOnItemClickedListener;

    //adapter
    private EditFavoriteListPhotosAdapter editFavoriteListPhotosAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClickListener = (OnItemClickedListener) context;
        mOnItemClickedListener = (OnItemClickedListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_edit_favorite_list, container, false);
            findViews();
            editFavoriteListPhotosAdapter = new EditFavoriteListPhotosAdapter(this);
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            favoritePhotos.setLayoutManager(staggeredGridLayoutManager);
            favoritePhotos.setAdapter(editFavoriteListPhotosAdapter);
            ItemTouchHelper.Callback callback = new FavoriteItemTouchCallBack(editFavoriteListPhotosAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(favoritePhotos);
        }
        mOnItemClickedListener.setActionBarMode(MainActivity.ActionBarMode.EDIT);
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
        editFavoriteListPhotosAdapter.getSelected().add(photoToHighlight);
        updateData();
        return rootView;
    }

    private void updateData() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<PhotoItem> results = realm
                    .where(PhotoItem.class)
                    .sort("position")
                    .findAll();
            photoItems.addAll(realm.copyFromRealm(results));
        }
        setPhotosData();
        editFavoriteListPhotosAdapter.setPhotos();
    }

    private void setPhotosData() {
        int size = photoItems.size();
        photosDir.clear();
        for (int index = 0; index < size; index++) {
            photosDir.add(photoItems.get(index).getPicture());
        }
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

    private void findViews() {
        favoritePhotos = rootView.findViewById(R.id.edit_favorite_photos);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    public void deleteSelectedItems() {
        List<String> selectedPhotos = editFavoriteListPhotosAdapter.getSelected();
        for (int index = 0; index < selectedPhotos.size(); index++) {
            int position = photosDir.indexOf(selectedPhotos.get(index));
            photosDir.remove(selectedPhotos.get(index));
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
                            .equalTo("picture", deletedPhotoItem.getPictureURL())
                            .findAll();
                    results.deleteAllFromRealm();
                });
            }
        }
    }

    public void clearChanges(){
        photoItems.clear();
        updateData();
    }

    public void setItemClicked(String photoClicked){
        photoToHighlight = photoClicked;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try(Realm realm = Realm.getDefaultInstance()){
            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(photoItems));
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
        public PictureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new PictureHolder(inflater.inflate(R.layout.photo_fragment, viewGroup, false));
        }


        @SuppressLint("ClickableViewAccessibility")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(@NonNull final PictureHolder viewHolder, int position) {
            final String itemTouched = photosDir.get(position);
            viewHolder.photo.setImageBitmap(photos.get(position));
            if (selected.contains(itemTouched)) {
                highlightView(viewHolder);
            } else {
                unhighlightView(viewHolder);
            }
            if (selected.size() > 0) {
                mClickListener.setActionBarMode(MainActivity.ActionBarMode.EDIT_ITEM_CLICKED);
            } else {
                mClickListener.setActionBarMode(MainActivity.ActionBarMode.EDIT);
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
                    mClickListener.setActionBarMode(MainActivity.ActionBarMode.EDIT_ITEM_CLICKED);
                } else {
                    mClickListener.setActionBarMode(MainActivity.ActionBarMode.EDIT);
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
            holder.photo.setPadding(-10, -10, -10, -10);
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

        void clearData(){
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
