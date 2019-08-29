package com.example.leegram.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.leegram.R;
import com.example.leegram.fragments.EditFavoriteListFragment;

// TODO: 29/08/2019 cant really undersatand why an activity is needed here
public class EditFavoriteListActivity extends AppCompatActivity implements
        EditFavoriteListFragment.OnClickEditItemListener {

    private EditFavoriteListFragment editFavoriteListFragment;
    private MenuItem removePhotos;
    private MenuItem clearChanges;

    @SuppressLint("ResourceType")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            setContentView(R.layout.activity_main);
            editFavoriteListFragment = new EditFavoriteListFragment();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.main_activity_container, editFavoriteListFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_activity_action_bar, menu);
        removePhotos = menu.findItem(R.id.trash);
        removePhotos.setVisible(true);
        clearChanges = menu.findItem(R.id.clear);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.trash:
                editFavoriteListFragment.deleteSelectedItems();
                removePhotos.setVisible(false);
                break;
            case R.id.clear:
                clearChanges.setVisible(false);
                editFavoriteListFragment.setClearChangesButtonGone();
                editFavoriteListFragment.clearChanges();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItem() {
        removePhotos.setVisible(true);
    }

    @Override
    public void noItemSelected() {
        removePhotos.setVisible(false);
    }

    @Override
    public void onSwipeAction() {
        clearChanges.setVisible(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            editFavoriteListFragment.updateRealmWhenSaved();
            finish();
            this.overridePendingTransition(R.anim.exit_animation, R.anim.exit_animation);
        }
        return super.onKeyDown(keyCode, event);
    }

}
