package com.example.leegram.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;
import com.example.leegram.fragments.FavoritePhotosFragment;
import com.example.leegram.fragments.NoDataFragment;
import com.example.leegram.fragments.SearchPhotosFragment;


public class MainActivity extends AppCompatActivity implements NoDataFragment.OnAddPhotoButtonClickListener,
        SearchPhotosFragment.OnClickAddButtonListener, FavoritePhotosFragment.OnButtonsListener {
    private static int EDIT_REQUEST_CODE = 9999;

    private NoDataFragment noDataFragment;
    private SearchPhotosFragment searchPhotosFragment;
    private FavoritePhotosFragment favoritePhotosFragment;
    private FragmentManager fragmentManager;
    private  CommunicateWithRealm communicateWithRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        communicateWithRealm = new CommunicateWithRealm();
        if (savedInstanceState == null) {
            searchPhotosFragment = new SearchPhotosFragment();
            favoritePhotosFragment = new FavoritePhotosFragment();
            noDataFragment = new NoDataFragment();
            fragmentManager = getSupportFragmentManager();
        }
    }

    public void onAddPhotoButtonClicked() {
        fragmentManager.beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
    }

    @Override
    public void onClickAddButton() {
        fragmentManager.beginTransaction()
                .replace(R.id.main_activity_container, favoritePhotosFragment)
                .commit();
    }

    @Override
    public void onNoPictureListener() {
        fragmentManager.beginTransaction()
                .replace(R.id.main_activity_container, noDataFragment)
                .commit();
    }

    @Override
    public void onAddButtonListener() {
        fragmentManager.beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_action_bar, menu);
        menu.findItem(R.id.action_save).setVisible(false);
        if(communicateWithRealm.getPhotoItems().isEmpty()){
            fragmentManager.beginTransaction()
                    .add(R.id.main_activity_container, noDataFragment)
                    .commit();
            menu.findItem(R.id.action_edit).setVisible(false);
        }else{
            fragmentManager.beginTransaction()
                    .add(R.id.main_activity_container, favoritePhotosFragment)
                    .commit();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            Intent intent = new Intent(this, EditFavoriteListActivity.class);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
        }

        return super.onOptionsItemSelected(item);
    }
}
