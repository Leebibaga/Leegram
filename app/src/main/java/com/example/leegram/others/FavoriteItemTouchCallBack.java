package com.example.leegram.others;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.leegram.fragments.EditFavoriteListFragment;

public class FavoriteItemTouchCallBack extends ItemTouchHelper.Callback {

    EditFavoriteListFragment.EditFavoriteListPhotosAdapter editFavoriteListPhotosAdapter;

    public FavoriteItemTouchCallBack(EditFavoriteListFragment.EditFavoriteListPhotosAdapter editFavoriteListPhotosAdapter){
        this.editFavoriteListPhotosAdapter = editFavoriteListPhotosAdapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder start, @NonNull RecyclerView.ViewHolder target) {
        editFavoriteListPhotosAdapter.onItemMove(start.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        editFavoriteListPhotosAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    public interface ItemTouchHelperAdapter {
        boolean  onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
    }
}
