package com.example.leegram.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.leegram.R;
import com.example.leegram.fragments.CreateNewFolderFragment;
import com.example.leegram.fragments.EditFavoriteListFragment;
import com.example.leegram.fragments.FolderFragment;
import com.example.leegram.fragments.FolderListFragment;
import com.example.leegram.fragments.SearchPhotosFragment;


public class MainActivity extends AppCompatActivity implements FolderFragment.OnLongClickPhotoListener {

    private final String EDIT_TAG = "edit_fragment";
    private final String FAVORITE_TAG = "favorite_fragment";
    private final String FOLDER_LIST = "folder_list";
    private final String CREATE_FOLDER = "create_folder";


    //view
    private SearchPhotosFragment searchPhotosFragment;
    private CreateNewFolderFragment createNewFolderFragment;
    private EditFavoriteListFragment editFavoriteListFragment;
    private FolderListFragment folderListFragment;

    //data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            createNewFolderFragment = new CreateNewFolderFragment();
            searchPhotosFragment = new SearchPhotosFragment();
            editFavoriteListFragment = new EditFavoriteListFragment();
            folderListFragment = new FolderListFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_activity_container, folderListFragment)
                .commit();
    }

    @Override
    public void onPhotoClicked(String photoClicked) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, editFavoriteListFragment)
                .addToBackStack(EDIT_TAG)
                .commit();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack();
    }
}
