package com.example.leegram.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.leegram.R;

public class NoDataFragment extends Fragment {
    private OnAddPhotoButtonClickListener mAddPhotos;

    public NoDataFragment(){

    }

    public interface OnAddPhotoButtonClickListener {
        void onAddPhotoButtonClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_no_data, container, false);
        Button addPhotosToList = rootView.findViewById(R.id.add_to_list);
        addPhotosToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddPhotos.onAddPhotoButtonClicked();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAddPhotos = (OnAddPhotoButtonClickListener) context;
    }

}
