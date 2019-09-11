package com.example.leegram.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// todo: change name to Folder
public class Folder extends RealmObject {

    @PrimaryKey
    private String id;
    private String folderName;
    private boolean isDefault;
    private RealmList<PhotoItem> photoItems;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public RealmList<PhotoItem> getPhotoItems() {
        return photoItems;
    }

    public void setPhotoItems(RealmList<PhotoItem> photoItems) {
        this.photoItems = photoItems;
    }
}
