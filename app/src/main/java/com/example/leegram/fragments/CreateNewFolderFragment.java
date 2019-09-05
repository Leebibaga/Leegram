package com.example.leegram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.example.leegram.R;
import com.example.leegram.model.FolderItem;
import java.util.UUID;

import io.realm.Realm;

public class CreateNewFolderFragment extends Fragment {

    //view
    private View rootView;
    private EditText enterNewFolderName;
    private Button createFolder;
    private LinearLayout makeDefault;
    private RadioButton defaultToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_create_folder, container, false);
            setUI();
        }
        createFolder.setOnClickListener(v -> {

        });
        return rootView;
    }

    public void setUI(){
        enterNewFolderName = rootView.findViewById(R.id.enter_folder_name);
        createFolder = rootView.findViewById(R.id.create_folder);
        makeDefault = rootView.findViewById(R.id.make_default);
        defaultToggle = rootView.findViewById(R.id.make_default_toggle);
    }

    public void setNewFolder(){
        FolderItem folderItem = new FolderItem();
        folderItem.setFolderName(enterNewFolderName.getText().toString());
        folderItem.setDefault(defaultToggle.isSelected());
        folderItem.setId(UUID.randomUUID().toString());
        try (Realm realm = Realm.getDefaultInstance()){

        }
    }
}
