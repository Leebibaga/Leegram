package com.example.leegram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.leegram.R;

import java.util.LinkedList;
import java.util.List;

public class FolderListFragment extends Fragment {

    //view
    private ImageView noFolderMessage;
    private RecyclerView folderList;
    private View rootView;

    //Adapter
    private FolderListFolderAdapter folderListFolderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.folder_list_fragment, container, false);
            folderListFolderAdapter = new FolderListFolderAdapter();
            setUI();
        }
       return rootView;
    }

    public void setUI(){
        noFolderMessage = rootView.findViewById(R.id.no_folder_text);
        folderList = rootView.findViewById(R.id.folder_list);
        folderList.setAdapter(folderListFolderAdapter);
    }

    public class FolderListFolderAdapter extends RecyclerView.Adapter<FolderListFolderAdapter.FolderListHolder> {

        private LayoutInflater inflater;
        private List<String> foldersName;

        public FolderListFolderAdapter(){
            inflater = getLayoutInflater();
            foldersName = new LinkedList<>();
        }

        @NonNull
        @Override
        public FolderListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new FolderListHolder (inflater.inflate(R.layout.folder_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FolderListHolder viewHolder, int i) {
            viewHolder.folderName.setText("folderName");
        }

        @Override
        public int getItemCount() {
            return foldersName.size();
        }

        public class FolderListHolder extends RecyclerView.ViewHolder{

            TextView folderName;

            public FolderListHolder(@NonNull View itemView) {
                super(itemView);
                folderName = itemView.findViewById(R.id.folder_name);
            }
        }

    }
}
