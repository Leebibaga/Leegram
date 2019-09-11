package com.example.leegram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.leegram.Const;
import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.Folder;

import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class FolderListFragment extends Fragment {

    private final String CREATE_FOLDER = "create_folder";

    //view
    private TextView noFolderMessage;
    private RecyclerView folderList;
    private View rootView;

    //data
    private String defaultFolder;
    private List<Folder> folderItems = new LinkedList<>();

    //Adapter
    private FolderListFolderAdapter folderListFolderAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folder, menu);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(Const.APP_NAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.add:
                navigateToCreateNewFolderScreen();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.folder_list_fragment, container, false);
            folderListFolderAdapter = new FolderListFolderAdapter();
            setUI();
            getFoldersNames();
            if (folderItems.isEmpty()) {
                noFolderMessage.setVisibility(View.VISIBLE);
                folderList.setVisibility(View.GONE);
            } else if (getDefaultFolder()) {
                navigateToFolder(defaultFolder);
            } else {
                noFolderMessage.setVisibility(View.GONE);
                folderList.setVisibility(View.VISIBLE);
            }
        }
        return rootView;
    }

    public void setUI() {
        noFolderMessage = rootView.findViewById(R.id.no_folder_text);
        folderList = rootView.findViewById(R.id.folder_list);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        folderList.setLayoutManager(linearLayoutManager);
        folderList.setAdapter(folderListFolderAdapter);
    }

    private void getFoldersNames() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Folder> realmResults = realm.where(Folder.class)
                    .findAll();
            folderItems.clear();
            folderItems.addAll(realm.copyFromRealm(realmResults));
        }
    }

//    private void getFoldersNames() {
//        RealmConfiguration config = new RealmConfiguration
//                .Builder()
//                .deleteRealmIfMigrationNeeded()
//                .build();
//        Realm realm = Realm.getInstance(config);
//            RealmResults<Folder> realmResults = realm.where(Folder.class)
//                    .findAll();
//            folderItems.clear();
//            folderItems.addAll(realm.copyFromRealm(realmResults));
//
//    }
    private boolean getDefaultFolder() {
        try (Realm realm = Realm.getDefaultInstance()) {
            Folder folderItem = realm.where(Folder.class)
                    .equalTo("isDefault", true)
                    .findFirst();
            if (folderItem != null) {
                defaultFolder = folderItem.getId();
                return true;
            } else {
                return false;
            }
        }
    }

    private void navigateToFolder(String folderId) {
        Bundle bundle = new Bundle();
        bundle.putString(Const.FOLDER_ID, folderId);
        FolderFragment folderFragment = new FolderFragment();
        folderFragment.setArguments(bundle);
        ((MainActivity) getActivity()).showOtherFragment(folderFragment);
    }

    private void navigateToCreateNewFolderScreen(){
        ((MainActivity) getActivity()).showOtherFragment(new CreateNewFolderFragment());
    }

    public class FolderListFolderAdapter extends RecyclerView.Adapter<FolderListFolderAdapter.FolderListHolder> {

        private LayoutInflater inflater;

        public FolderListFolderAdapter() {
            inflater = getLayoutInflater();
        }

        @NonNull
        @Override
        public FolderListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new FolderListHolder(inflater.inflate(R.layout.folder_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FolderListHolder viewHolder, int position) {
            viewHolder.folderName.setText(folderItems.get(position).getFolderName());
            viewHolder.folderName.setOnClickListener(v -> {
                navigateToFolder(folderItems.get(position).getId());
            });
        }

        @Override
        public int getItemCount() {
            return folderItems.size();
        }


        public class FolderListHolder extends RecyclerView.ViewHolder {
            TextView folderName;

            public FolderListHolder(@NonNull View itemView) {
                super(itemView);
                folderName = itemView.findViewById(R.id.folder_name);
            }
        }

    }
}
