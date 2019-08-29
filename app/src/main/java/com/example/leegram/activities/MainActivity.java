package com.example.leegram.activities;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.leegram.R;
import com.example.leegram.fragments.FavoritePhotosFragment;
import com.example.leegram.fragments.SearchPhotosFragment;


public class MainActivity extends AppCompatActivity implements SearchPhotosFragment.OnClickItemListener{

    //view
    private SearchPhotosFragment searchPhotosFragment;
    private FavoritePhotosFragment favoritePhotosFragment;
    private ActionBar actionBar;
    private EditText searchPhoto;
    private MenuItem addSelectedPhoto;
    private MenuItem plusButton;

    //data
    private Runnable delayCounter;
    private Handler handleSpinner = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            searchPhotosFragment = new SearchPhotosFragment();
            favoritePhotosFragment = new FavoritePhotosFragment();
            actionBar = getSupportActionBar();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_activity_container, favoritePhotosFragment)
                .commit();
        actionBar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_action_bar, menu);
        actionBar.setCustomView(R.layout.search_bar);
        View view = actionBar.getCustomView();
        searchPhoto = view.findViewById(R.id.search_photos_bar);
        addSelectedPhoto = menu.findItem(R.id.add_chosen_photos);
        plusButton = menu.findItem(R.id.add_photos);
        searchBarAction();
        return super.onCreateOptionsMenu(menu);
    }

    private void searchBarAction(){
        searchPhoto.addTextChangedListener(new TextWatcher() {
            String textBeforeChanged;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textBeforeChanged = searchPhoto.getText().toString();
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
        if (openKeyBoard){
            InputMethodManager imm = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else{
            // TODO: add close keyboard
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.add_photos:
                showSearchBar();
                break;
            case R.id.add_chosen_photos:
                addPhotos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchBar(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, searchPhotosFragment)
                .commit();
        plusButton.setVisible(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        toggleSoftKeyboard(true);
    }

    public void addPhotos() {
        searchPhotosFragment.savePhotosToRealm();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, favoritePhotosFragment)
                .commit();
        addSelectedPhoto.setVisible(false);
        plusButton.setVisible(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    // TODO: 29/08/2019 remove !~!! make actionbar change method smarter and use invalidate 
    @Override
    public void onClickItem() {
        addSelectedPhoto.setVisible(true);
    }

    @Override
    public void noItemSelected() {
        addSelectedPhoto.setVisible(false);
    }
}
