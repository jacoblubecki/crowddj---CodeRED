package com.lubecki.crowddj.managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubecki.crowddj.R;
import com.lubecki.crowddj.Secrets;
import com.lubecki.crowddj.djActivity;
import com.lubecki.crowddj.spotify.SpotifyPlayer;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;

import java.util.ArrayList;
import java.util.Arrays;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Jacob on 4/4/15.
 */
public class PlaylistManager {

    private Context context;
    private SharedPreferences preferences;

    private static PlaylistManager manager;
    private Player spotifyPlayer;
    private ArrayList<Track> tracks;

    private PlaylistManager() {
        tracks = new ArrayList<>();
        context = djActivity.getInstance();
        preferences = context.getSharedPreferences("com.lubecki.crowddj", Context.MODE_PRIVATE);

        String authToken = context.getSharedPreferences("com.lubecki.crowddj", Context.MODE_PRIVATE)
                .getString(context.getString(R.string.spotify_token_key), "");
        String clientID = context.getString(R.string.spotify_client_id);
        spotifyPlayer = Player.create(new Config(context, authToken, clientID));
    }

    public static PlaylistManager getInstance() {
        if(manager != null) {
            return manager;
        }
        return new PlaylistManager();
    }

    public void addTrack(Track track) {
        tracks.add(track);
        spotifyPlayer.queue(track.uri);
        serializeList();
    }

    public void serializeList(){
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        String json = gson.toJson(tracks);

        preferences.edit().putString("jsonData", json).apply();
    }

    public void loadList() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        String trackString = preferences.getString("jsonData", "");

        if(trackString.isEmpty()){
            tracks = new ArrayList<>();
        }else{
            tracks = new ArrayList<>(Arrays.asList(gson.fromJson(trackString, Track.class)));
        }
    }
}
