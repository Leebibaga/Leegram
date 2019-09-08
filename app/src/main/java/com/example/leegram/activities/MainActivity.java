package com.example.leegram.activities;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.leegram.R;
import com.example.leegram.fragments.CreateNewFolderFragment;
import com.example.leegram.fragments.EditFavoriteListFragment;
import com.example.leegram.fragments.FolderFragment;
import com.example.leegram.fragments.FolderListFragment;
import com.example.leegram.fragments.SearchPhotosFragment;
import com.example.leegram.others.OnItemClickedListener;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements FolderFragment.OnLongClickPhotoListener,
        OnItemClickedListener, SearchPhotosFragment.OnFinishedSearchListener {

    private boolean enteredNewFolder = false;

    public enum ActionBarMode {
        FOLDER_SCREEN, SEARCH, SEARCH_ITEM_CLICKED, EDIT, EDIT_ITEM_CLICKED, FOLDER_LIST_SCREEN, CREATE_FOLDER_SCREEN
    }

    private final String SEARCH_TAG = "search_fragment";
    private final String EDIT_TAG = "edit_fragment";
    private final String FAVORITE_TAG = "favorite_fragment";
    private final String FOLDER_LIST = "folder_list";
    private final String CREATE_FOLDER = "create_folder";


    //view
    private SearchPhotosFragment searchPhotosFragment;
    private CreateNewFolderFragment createNewFolderFragment;
    private EditFavoriteListFragment editFavoriteListFragment;
    private FolderListFragment folderListFragment;
    private ActionBarMode actionBarMode;
    private EditText searchPhoto;
    //data
    private Runnable delayCounter;
    private Handler handleSpinner = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            createNewFolderFragment = new CreateNewFolderFragment();
            searchPhotosFragment = new SearchPhotosFragment();
            editFavoriteListFragment = new EditFavoriteListFragment();
            folderListFragment = new FolderListFragment();
            actionBarMode = ActionBarMode.FOLDER_LIST_SCREEN;
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_activity_container, folderListFragment)
                .commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        switch (actionBarMode) {
            case EDIT:
                Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Leegram");
                menu.findItem(R.id.clear_edit).setVisible(true);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_photos).setVisible(false);
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.add_chosen_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
            case FOLDER_SCREEN:
                Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayShowCustomEnabled(false);
                getSupportActionBar().setTitle("Leegram");
                menu.findItem(R.id.add_photos).setVisible(true);
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_chosen_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
            case SEARCH:
                getSupportActionBar().setDisplayShowCustomEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_photos).setVisible(false);
                menu.findItem(R.id.add_chosen_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
            case EDIT_ITEM_CLICKED:
                getSupportActionBar().setTitle("Leegram");
                menu.findItem(R.id.trash).setVisible(true);
                menu.findItem(R.id.clear_edit).setVisible(true);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
            case SEARCH_ITEM_CLICKED:
                getSupportActionBar().setTitle("Leegram");
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.clear_search).setVisible(true);
                menu.findItem(R.id.add_chosen_photos).setVisible(true);
                menu.findItem(R.id.add_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
            case FOLDER_LIST_SCREEN:
                getSupportActionBar().setTitle("Leegram");
                menu.findItem(R.id.add_folders).setVisible(true);
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_chosen_photos).setVisible(false);
                menu.findItem(R.id.add_photos).setVisible(false);
                break;
            case CREATE_FOLDER_SCREEN:
                Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayShowCustomEnabled(false);
                getSupportActionBar().setTitle("Create New Folder");
                menu.findItem(R.id.clear_edit).setVisible(false);
                menu.findItem(R.id.clear_search).setVisible(false);
                menu.findItem(R.id.add_chosen_photos).setVisible(false);
                menu.findItem(R.id.add_photos).setVisible(false);
                menu.findItem(R.id.add_folders).setVisible(false);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_action_bar, menu);
        getSupportActionBar().setCustomView(R.layout.search_bar);
        searchPhoto = getSupportActionBar().getCustomView().findViewById(R.id.search_photos_bar);
        searchBarAction();
        return super.onCreateOptionsMenu(menu);
    }


    private void searchBarAction() {
        searchPhoto.requestFocus();
        searchPhoto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (delayCounter == null) {
                    delayCounter = () -> {
                        if (!searchPhoto.getText().toString().isEmpty()) {
                            searchPhotosFragment.downloadPhotos(searchPhoto.getText().toString());
                        }
                    };
                }
                handleSpinner.removeCallbacks(delayCounter);
                handleSpinner.postDelayed(delayCounter, 600);
            }
        });
    }

    public void toggleSoftKeyboard(boolean openKeyBoard) {
        InputMethodManager imm = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (openKeyBoard) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(searchPhoto.getWindowToken(), 0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.add_photos:
                showSearchBar();
                break;
            case R.id.add_chosen_photos:
                addPhotos();
                break;
            case R.id.clear_search:
                clearSearchChoices();
                break;
            case R.id.clear_edit:
                clearEditChoices();
                break;
            case R.id.trash:
                deleteItems();
                break;
            case R.id.add_folders:
                navigateToCreateNewFolder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchBar() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .addToBackStack(SEARCH_TAG)
                .commit();
        toggleSoftKeyboard(true);
    }

    private void addPhotos() {
        searchPhotosFragment.savePhotos();
    }

    private void clearSearchChoices() {
        searchPhotosFragment.clearChoices();
    }

    private void clearEditChoices() {
        editFavoriteListFragment.clearChanges();
    }

    private void deleteItems() {
        editFavoriteListFragment.deleteSelectedItems();
    }

    public void navigateToCreateNewFolder(){
        enteredNewFolder = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, createNewFolderFragment)
                .addToBackStack(CREATE_FOLDER)
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
    public void setActionBarMode(ActionBarMode actionBarMode) {
        this.actionBarMode = actionBarMode;
    }

    @Override
    public void onDownloadFinished() {
        toggleSoftKeyboard(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
