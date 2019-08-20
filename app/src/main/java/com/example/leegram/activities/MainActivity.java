package com.example.leegram.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.leegram.R;
import com.example.leegram.fragments.FavoritePhotosFragment;
import com.example.leegram.fragments.NoDataFragment;
import com.example.leegram.fragments.SearchPhotosFragment;

import io.realm.Realm;


public class MainActivity extends AppCompatActivity implements NoDataFragment.OnAddPhotoButtonClickListener,
        SearchPhotosFragment.OnClickAddButtonListener, FavoritePhotosFragment.OnButtonsListener {
    private NoDataFragment noDataFragment;
    private SearchPhotosFragment searchPhotosFragment;
    private  FavoritePhotosFragment favoritePhotosFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        searchPhotosFragment = new SearchPhotosFragment();
        favoritePhotosFragment = new FavoritePhotosFragment();
        noDataFragment = new NoDataFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.main_activity_container, noDataFragment)
                .commit();
    }

    public void onAddPhotoButtonClicked() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
    }


    @Override
    public void onClickAddButton() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, favoritePhotosFragment)
                .commit();
    }

    @Override
    public void onNoPictureListener() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, noDataFragment)
                .commit();
    }

    @Override
    public void onAddButtonListener() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
    }
}
