package com.example.leegram.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.leegram.R;
import com.example.leegram.activities.MainActivity;
import com.example.leegram.model.FolderItem;
import com.example.leegram.others.OnItemClickedListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class FolderListFragment extends Fragment {

    private final String DEFAULT_FOLDER = "default_folder";

    //view
    private TextView noFolderMessage;
    private RecyclerView folderList;
    private View rootView;

    //data
    private String defaultFolder;
    private List<FolderItem> folderItems = new LinkedList<>();
    private OnItemClickedListener mOnItemClickedListener;

    //Adapter
    private FolderListFolderAdapter folderListFolderAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnItemClickedListener = (OnItemClickedListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.folder_list_fragment, container, false);
            folderListFolderAdapter = new FolderListFolderAdapter();
            setUI();
        }
        mOnItemClickedListener.setActionBarMode(MainActivity.ActionBarMode.FOLDER_LIST_SCREEN);
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
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
            RealmResults<FolderItem> realmResults = realm.where(FolderItem.class)
                    .findAll();
            folderItems.clear();
            folderItems.addAll(realm.copyFromRealm(realmResults));
        }
    }

    private boolean getDefaultFolder() {
        try (Realm realm = Realm.getDefaultInstance()) {
            FolderItem folderItem = realm.where(FolderItem.class)
                    .equalTo("isDefault", true)
                    .findFirst();
            if (folderItem != null) {
                defaultFolder = folderItem.getId();
                return true;
            }
        }
        return false;
    }

    private void navigateToFolder(String folderId) {
        Bundle bundle = new Bundle();
        bundle.putString("folderId", folderId);
        FolderFragment folderFragment = new FolderFragment();
        folderFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_container, folderFragment)
                .addToBackStack(folderId)
                .commit();
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
                mOnItemClickedListener.setActionBarMode(MainActivity.ActionBarMode.FOLDER_SCREEN);
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
