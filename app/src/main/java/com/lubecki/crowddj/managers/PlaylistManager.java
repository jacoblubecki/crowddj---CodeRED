package com.lubecki.crowddj.managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubecki.crowddj.R;
import com.lubecki.crowddj.djActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jacob on 4/4/15.
 */
public class PlaylistManager {

    private static PlaylistManager manager;

    private SharedPreferences preferences;
    private ArrayList tracks;

    private PlaylistManager() {
        tracks = new ArrayList();
    }

    public static PlaylistManager getInstance() {
        if(manager != null) {
            return manager;
        }
        return new PlaylistManager();
    }

    public void addTrack() {

    }

    public void serializeList(){
        Activity activity = djActivity.getInstance();
        SharedPreferences prefs = activity.getSharedPreferences("tommista.com.harmony", Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        String json = gson.toJson(tracks);

        prefs.edit().putString("jsonData", json).apply();
    }

    public void loadList() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        preferences = djActivity.getInstance().getSharedPreferences(djActivity.getInstance().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String trackString = preferences.getString("jsonData", "");

        if(trackString.isEmpty()){
            tracks = new ArrayList<>();
        }else{
            tracks = new ArrayList<>(Arrays.asList(gson.fromJson(trackString, Object.class))); //// FIX THIS DUMBASS
        }
    }
}
