package com.lubecki.crowddj.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubecki.crowddj.R;
import com.lubecki.crowddj.djActivity;
import com.lubecki.crowddj.spotify.LoginRequester;
import com.lubecki.crowddj.spotify.TrackCallBack;
import com.lubecki.crowddj.spotify.models.Track;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Jacob on 4/4/15.
 */
public class PlaylistManager {

    private Context context;
    private SharedPreferences preferences;

    private static PlaylistManager manager;
    private Player spotifyPlayer;
    private ArrayList<Track> tracks;
    private Track lastTrack;

    private ConnectionStateCallback connectionStateCallback;
    private PlayerNotificationCallback playerNotificationCallback;
    private LoginRequester requester;
    private TrackCallBack callback;

    private boolean isPlaying = false;

    private PlaylistManager() {
        tracks = new ArrayList<>();
        context = djActivity.getInstance();
        preferences = context.getSharedPreferences("com.lubecki.crowddj", Context.MODE_PRIVATE);

        String authToken = preferences.getString(context.getString(R.string.spotify_token_key), "");
        String clientID = context.getString(R.string.spotify_client_id);
        makeListeners();
        spotifyPlayer = Spotify.getPlayer(new Config(context, authToken, clientID), context, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                Log.v("", "INITIALIZED");
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
        spotifyPlayer.addConnectionStateCallback(connectionStateCallback);
        spotifyPlayer.addPlayerNotificationCallback(playerNotificationCallback);
    }

    public static PlaylistManager getInstance() {
        if(manager == null) {
            manager = new PlaylistManager();
        }
        return manager;
    }

    public void addTrack(Track track) {
        tracks.add(track);
        spotifyPlayer.queue(track.uri);
        callback.trackAdded();
        if(!isPlaying) {
            spotifyPlayer.resume();
            isPlaying = true;
        }

        Log.v("", "PLAYING");
    }

    public void play() {
        spotifyPlayer.resume();
        isPlaying = true;
    }

    public void pause() {
        spotifyPlayer.pause();
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    private void makeListeners() {
        connectionStateCallback = new ConnectionStateCallback() {
            @Override
            public void onLoggedIn() {
                Timber.i("Logged In");
            }

            @Override
            public void onLoggedOut() {
                Timber.i("Logged Out");
            }

            @Override
            public void onLoginFailed(Throwable throwable) {
                requester.requestLogin();
            }

            @Override
            public void onTemporaryError() {
                Timber.i("Temporary Error");
            }

            @Override
            public void onConnectionMessage(String s) {
                //Timber.i("Connection Message: " + s);
            }
        };

        playerNotificationCallback = new PlayerNotificationCallback() {
            @Override
            public void onPlaybackEvent(EventType eventType, PlayerState playerState) {


                if(eventType == EventType.TRACK_START && playerState.positionInMs == 0) {
                    Timber.v(playerState.positionInMs + " millis" + playerState.playing + " " + playerState.activeDevice);
                    int i = 0;
                    while(i < tracks.size() && !tracks.get(i).uri.equals(playerState.trackUri)) {
                        i++;
                    }

                    if(i < tracks.size()) {
                        callback.trackStarted(tracks.get(i));
                    }
                }
            }

            @Override
            public void onPlaybackError(ErrorType errorType, String s) {
                Timber.i("Playback Error:\n" + errorType.name() + ": " + s);
                callback.errorCallback();

            }
        };
    }

    public void setLoginRequestListener(LoginRequester requestListener) {
        requester = requestListener;
    }

    public void setTrackChangeListener(TrackCallBack callback) {
        this.callback = callback;
    }
}
