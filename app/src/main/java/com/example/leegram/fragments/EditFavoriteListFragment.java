package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.leegram.R;
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

public class EditFavoriteListFragment extends Fragment implements OnStartDragListener {

    public interface OnClickEditItemListener {
        void onClickItem();
        void noItemSelected();
        void onSwipeAction();
    }

    //view
    private RecyclerView favoritePhotos;
    private View rootView;

    //data
    private List<PhotoItem> photoItems = new LinkedList<>();
    private RealmList<PhotoItem> deletedPhotoItems = new RealmList<>();
    private List<String> photosURLs = new LinkedList<>();
    private ItemTouchHelper mItemTouchHelper;
    private OnClickEditItemListener mClickEditItemListener;
    private boolean clearChangesIsShown = false;

    //adapter
    private EditFavoriteListPhotosAdapter editFavoriteListPhotosAdapter;


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
        updateData();
        return rootView;
    }

    private void findViews() {
        favoritePhotos = rootView.findViewById(R.id.edit_favorite_photos);
    }

    private List<Bitmap> convertBitmap() {
        List<Bitmap> downloadedPhotos = new LinkedList<>();
        for (PhotoItem photoItem : photoItems) {
            downloadedPhotos.add(BitmapFactory.decodeByteArray
                    (photoItem.getPicture(), 0, photoItem.getPicture().length));
        }
        return downloadedPhotos;
    }

    private void setPhotosData() {
        int size = photoItems.size();
        photosURLs.clear();
        for (int index = 0; index < size; index++) {
            photosURLs.add(photoItems.get(index).getPictureURL());
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
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

    public void deleteSelectedItems() {
        List<String> selectedPhotos = editFavoriteListPhotosAdapter.getSelected();
        for (int index = 0; index < selectedPhotos.size(); index++) {
            int position = photosURLs.indexOf(selectedPhotos.get(index));
            photosURLs.remove(selectedPhotos.get(index));
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
                            .equalTo("pictureURL", deletedPhotoItem.getPictureURL())
                            .findAll();
                    results.deleteAllFromRealm();
                });
            }
        }
    }

    public void updateRealmWhenSaved() {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(photoItems));
        }
    }

    public void clearChanges(){
        photoItems.clear();
        updateData();
    }

    public void setClearChangesButtonGone(){
        clearChangesIsShown = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mClickEditItemListener = (OnClickEditItemListener) context;
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
            final String itemTouched = photosURLs.get(position);
            viewHolder.photo.setImageBitmap(photos.get(position));
            String SelectedPhoto = Objects.requireNonNull(getActivity()).getIntent().getStringExtra("image");
            if(itemTouched.equals(SelectedPhoto)){
                highlightView(viewHolder);
                selected.add(SelectedPhoto);
                deletedPhotoItems.add(photoItems.get(position));
            }
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
                    mClickEditItemListener.onClickItem();
                } else {
                    mClickEditItemListener.noItemSelected();
                }
            });
        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void highlightView(EditFavoriteListPhotosAdapter.PictureHolder holder) {
            holder.photo.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent));
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
            photos.addAll(convertBitmap());
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
            if(!clearChangesIsShown) {
                clearChangesIsShown = true;
                mClickEditItemListener.onSwipeAction();
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
