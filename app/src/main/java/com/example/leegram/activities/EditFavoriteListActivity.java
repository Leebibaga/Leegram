package com.example.leegram.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.leegram.R;
import com.example.leegram.fragments.EditFavoriteListFragment;


public class EditFavoriteListActivity extends AppCompatActivity {

    private EditFavoriteListFragment editFavoriteListFragment;

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
}
