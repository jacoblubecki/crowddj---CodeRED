package com.lubecki.crowddj.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.lubecki.crowddj.R;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import timber.log.Timber;

/**
 * Created by JoshBeridon on 4/3/15.
 */
public class SpotifyPlayer {
    private Context context;
    private Player player;
    private String songID;
    private EndTrackCallBack callback;

    private ConnectionStateCallback connectionStateCallback;
    private PlayerNotificationCallback playerNotificationCallback;

    public SpotifyPlayer(Context context, String songID, EndTrackCallBack callback) {
        this.context = context;
        this.songID = songID;
        this.callback = callback;
        makeListeners();
        init();
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
                Timber.i(throwable, "Log In Failed");
            }

            @Override
            public void onTemporaryError() {
                Timber.i("Temporary Error");
            }

            @Override
            public void onConnectionMessage(String s) {
                Timber.i("Connection Message: " + s);
            }
        };
        playerNotificationCallback = new PlayerNotificationCallback() {
            @Override
            public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
                if (eventType == EventType.TRACK_END && playerState.positionInMs > 100) {
                    callback.trackEnded();
                }
            Timber.i("Playback Event" + eventType.name());
            }

            @Override
            public void onPlaybackError(ErrorType errorType, String s) {
                Timber.i("Playback Error:\n" + errorType.name()+": "+ s);

            }
        };

    }

    private void init() {
        Resources res = context.getResources();
        String prefsName = res.getString(R.string.shared_prefs_name);
        String key = res.getString(R.string.spotify_token_key);
        String defValue = res.getString(R.string.shared_prefs_def_string);
        String clientID = res.getString(R.string.spotify_client_id);

        SharedPreferences preferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        String authToken = preferences.getString(key, defValue);

        Config playerConfig = new Config(context, authToken,clientID);
        player = Spotify.getPlayer(playerConfig,this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                player.addConnectionStateCallback(connectionStateCallback);
                player.addPlayerNotificationCallback(playerNotificationCallback);
                //player.play("spotify:track:" + songID); 2TpxZ7JUBn3uw46aR7qd6V
                player.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                Timber.i("Should be playing");
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }
    public void pause(){ player.pause();}
    public void resume(){
        player.resume();

    Timber.i("Should be playing2");
    }
    public void play(){player.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");}

}
