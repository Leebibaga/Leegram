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
        SearchPhotosFragment.OnClickAddButtonListener {
    private NoDataFragment noDataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        noDataFragment = new NoDataFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.main_activity_container, noDataFragment)
                .commit();
    }

    public void onAddPhotoButtonClicked() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        SearchPhotosFragment searchPhotosFragment = new SearchPhotosFragment();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
    }


    @Override
    public void onClickAddButton() {
        FragmentManager goToSearchPhotoScreen = getSupportFragmentManager();
        FavoritePhotosFragment favoritePhotosFragment = new FavoritePhotosFragment();
        goToSearchPhotoScreen.beginTransaction()
                .replace(R.id.main_activity_container, favoritePhotosFragment)
                .commit();
    }
}
