package com.example.leegram.others;

import com.example.leegram.model.PhotoItem;


import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class CommunicateWithRealm {

    public CommunicateWithRealm(){

    }


    public void removeFromRealm(final String key, String value) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(realm1 -> {
                RealmResults<PhotoItem> results = realm1
                        .where(PhotoItem.class)
                        .equalTo(key, value)
                        .findAll();
                results.deleteAllFromRealm();
            });

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public List<PhotoItem> getPhotoItems() {
        List<PhotoItem> photoItems = new LinkedList<>();
        Realm realm = null;
        try {
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            realm = Realm.getInstance(config);
            RealmResults<PhotoItem> results = realm
                    .where(PhotoItem.class)
                    .findAll();
            photoItems.addAll(realm.copyFromRealm(results));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return photoItems;
    }

    public byte[] getPhoto(String value) {
        byte[] photoItem;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            PhotoItem results = realm
                    .where(PhotoItem.class)
                    .equalTo("pictureURL", value)
                    .findAll()
                    .first();
            photoItem = results.getPicture();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return photoItem;
    }

}
