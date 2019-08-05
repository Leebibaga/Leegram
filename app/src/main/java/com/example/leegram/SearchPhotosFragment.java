package com.example.leegram;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class SearchPhotosFragment extends Fragment implements SearchView.OnQueryTextListener {
    private CommunicateWithApi communicateWithApi;
    private GridView listOfPhotos;
    public SearchPhotosFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search_photos, container, false);
        SearchView searchPhotoBar = rootView.findViewById(R.id.search_photos_bar);
        listOfPhotos = rootView.findViewById(R.id.photos_list);
        searchPhotoBar.setIconifiedByDefault(false);
        searchPhotoBar.setSubmitButtonEnabled(true);
        searchPhotoBar.setOnQueryTextListener(this);
        return rootView;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("query", query);
        parameters.put("page", "1");
        parameters.put("per_page", "10");
        try {
            communicateWithApi = new CommunicateWithApi(
                    "https://api.unsplash.com/search/collections", "GET", parameters);
            communicateWithApi.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
