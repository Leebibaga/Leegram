package com.example.leegram.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;
import com.example.leegram.model.PhotoItem;
import com.example.leegram.others.FavoriteItemTouchCallBack;
import com.example.leegram.others.OnStartDragListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class EditFavoriteListFragment extends Fragment implements OnStartDragListener {

    //view
    private RecyclerView favoritePhotos;
    private View rootView;

    //data
    private RealmResults<PhotoItem> photoItems;
    private List<String> photosURLs = new LinkedList<>();
    private List<Integer> positions = new LinkedList<>();
    private ItemTouchHelper mItemTouchHelper;

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
            if (photoItems.get(index) != null) {
                photosURLs.add(photoItems.get(index).getPictureURL());
                positions.add(photoItems.get(index).getPosition());
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void updateData() {
        Realm realm = Realm.getDefaultInstance();
        photoItems = realm
                .where(PhotoItem.class)
                .findAll();
        setPhotosData();
        editFavoriteListPhotosAdapter.setPhotos();
    }

    public void updateRealmWhenSaved() {
        try(Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(photoItems));
        }
    }

    public class EditFavoriteListPhotosAdapter extends RecyclerView.Adapter<EditFavoriteListPhotosAdapter.PictureHolder> {
        private List<Bitmap> photos;
        private LayoutInflater inflater;
        private final OnStartDragListener mDragStartListener;


        public EditFavoriteListPhotosAdapter(OnStartDragListener mDragStartListener) {
            photos = new LinkedList<>();
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
            viewHolder.photo.setImageBitmap(photos.get(position));
            viewHolder.photo.setOnClickListener(V -> {
                viewHolder.photo.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(viewHolder);
                    }
                    return false;
                });
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

        public void onItemDismiss(int position) {
            photos.remove(position);
            photoItems.get(position).setPictureURL("removed");
            updatePositionAfterRemove(position + 1);
            notifyItemRemoved(position);
        }

        public void updatePositionAfterRemove(int position) {
            for (int index = position; index < photoItems.size(); index++) {
                Objects.requireNonNull(photoItems.where().equalTo("position", index).findFirst()).setPosition(index);
            }
        }

        private void updateItemsPositions(int startPosition, int targetPosition) {
            Objects.requireNonNull(photoItems.where().equalTo("position", startPosition).findFirst()).setPosition(-1);
            if(targetPosition > startPosition) {
                for (int index = startPosition + 1; index < targetPosition; index++) {
                    Objects.requireNonNull(photoItems.where().equalTo("position", index).findFirst()).setPosition(index - 1);
                }
            }else {
                for (int index = startPosition - 1; index > targetPosition; index--) {
                    Objects.requireNonNull(photoItems.where().equalTo("position", index).findFirst()).setPosition(index + 1);
                }
            }
            Objects.requireNonNull(photoItems.where().equalTo("position", -1).findFirst()).setPosition(targetPosition);
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
