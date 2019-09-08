package com.example.leegram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.FolderItem;

import java.util.Objects;
import java.util.UUID;

import io.realm.Realm;

public class CreateNewFolderFragment extends Fragment {

    //data
    private String folderId;

    //view
    private View rootView;
    private EditText enterNewFolderName;
    private Button createFolder;
    private RadioButton defaultToggle;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        getActivity().getActionBar().setTitle("Create New Folder");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_create_folder, container, false);
            setUI();
        }
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
        createFolder.setOnClickListener(v -> {
            setNewFolder();
            navigateToFolder();
        });
        return rootView;
    }

    public void setUI(){
        enterNewFolderName = rootView.findViewById(R.id.enter_folder_name);
        createFolder = rootView.findViewById(R.id.create_folder);
        defaultToggle = rootView.findViewById(R.id.make_default_toggle);
    }

    public void setNewFolder(){
        FolderItem folderItem = new FolderItem();
        folderId = UUID.randomUUID().toString();
        folderItem.setFolderName(enterNewFolderName.getText().toString());
        folderItem.setDefault(defaultToggle.isSelected());
        folderItem.setId(folderId);
        try (Realm realm = Realm.getDefaultInstance()){
            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(folderItem));
        }
    }

    private void navigateToFolder() {
        Bundle bundle = new Bundle();
        bundle.putString("folderId", folderId);
        FolderFragment folderFragment = new FolderFragment();
        folderFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, folderFragment)
                .addToBackStack(null)
                .commit();
    }
}
