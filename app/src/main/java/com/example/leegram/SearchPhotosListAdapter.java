package com.example.leegram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class SearchPhotosListAdapter extends BaseAdapter {
    private List<String> photos;
    private Context context;

    public SearchPhotosListAdapter(Context context, LinkedList<String> photos) {
        this.photos = photos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.photo_fragment, parent, false);
            holder.contactPic = convertView.findViewById(R.id.photo_item);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        //get pictur from list
        return null;
    }

    private class ViewHolder {
        ImageView contactPic;
    }
}
