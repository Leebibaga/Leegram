package com.example.leegram.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;
import com.example.leegram.fragments.FavoritePhotosFragment;
import com.example.leegram.fragments.NoDataFragment;
import com.example.leegram.fragments.SearchPhotosFragment;


public class MainActivity extends AppCompatActivity implements NoDataFragment.OnAddPhotoButtonClickListener,
        SearchPhotosFragment.OnClickAddButtonListener, FavoritePhotosFragment.OnButtonsListener {
    private NoDataFragment noDataFragment;
    private SearchPhotosFragment searchPhotosFragment;
    private FavoritePhotosFragment favoritePhotosFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CommunicateWithRealm communicateWithRealm = new CommunicateWithRealm();
        searchPhotosFragment = new SearchPhotosFragment();
        favoritePhotosFragment = new FavoritePhotosFragment();
        noDataFragment = new NoDataFragment();
        fragmentManager = getSupportFragmentManager();
        if(communicateWithRealm.getPhotoItems().isEmpty()){
            fragmentManager.beginTransaction()
                    .add(R.id.main_activity_container, noDataFragment)
                    .commit();
        }else{
            fragmentManager.beginTransaction()
                    .add(R.id.main_activity_container, favoritePhotosFragment)
                    .commit();
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
}
