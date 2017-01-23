package com.example.stefi.remap.model.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Stefi on 19.01.2017.
 */

public class RealmController  {

    Realm myRealm;

    public RealmController(Context mContext) {
        myRealm = Realm.getInstance(mContext);
    }


    public void insertInDb(String desc, String data, String vreme, Double lat, Double lon, String range) {
        myRealm.beginTransaction();
        // Create an object
        Task task = myRealm.createObject(Task.class);
        // Set its fields
        task.setDescription(desc);
        task.setData(data);
        task.setVreme(vreme);
        task.setLat(lat.toString());
        task.setLon(lon.toString());
        task.setRange(range);

        myRealm.commitTransaction();
    }

    public void DeleteTask(final String desc, final String date, final String time) {
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Task> result = realm.where(Task.class)
                        .equalTo(Task.DESCRIPTION,desc)
                        .equalTo(Task.DATE, date)
                        .equalTo(Task.TIME, time)
                        .findAll();
                if(!result.isEmpty()) {
                    result.clear();
                }
            }
        });
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> taskovi = new ArrayList<>();
        RealmResults<Task> results1 =
                myRealm.where(Task.class).findAll();

        for (int i = 0; i < results1.size(); i++) {
            taskovi.add(new Task(
                    results1.get(i).getDescription(),
                    results1.get(i).getData(),
                    results1.get(i).getVreme(),
                    results1.get(i).getLat(),
                    results1.get(i).getLon(),
                    results1.get(i).getRange()
                    ));
        }
        Collections.reverse(taskovi);
        return taskovi;
    }




}
