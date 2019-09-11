package com.example.leegram.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.leegram.R;
import com.example.leegram.fragments.EditFavoriteListFragment;
import com.example.leegram.fragments.FolderListFragment;


public class MainActivity extends AppCompatActivity {

    //view
    private FolderListFragment folderListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            folderListFragment = new FolderListFragment();
        }
        showOtherFragment(folderListFragment);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
        invalidateOptionsMenu();
    }

    public void showOtherFragment(Fragment enterFragment){
        showOtherFragment(enterFragment,false);
    }

    public void showOtherFragment(Fragment enterFrag, boolean neglectFromBackStack){
        if (neglectFromBackStack){
            getSupportFragmentManager().popBackStackImmediate();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_container, enterFrag)
                .addToBackStack(null).commit();
        invalidateOptionsMenu();
    }
}
