package com.example.leegram;

import android.nfc.Tag;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements NoDataFragment.OnAddPhotoButtonClickListener {
    private NoDataFragment noDataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
