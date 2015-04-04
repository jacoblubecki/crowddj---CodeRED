package com.lubecki.crowddj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lubecki.crowddj.managers.PlaylistManager;
import com.lubecki.crowddj.spotify.SpotifyAuthenticator;
import com.lubecki.crowddj.twitter.model.SearchList;
import com.lubecki.crowddj.twitter.model.Tweet;
import com.lubecki.crowddj.twitter.webapi.TwitterAPI;
import com.lubecki.crowddj.twitter.webapi.TwitterService;

import java.util.HashSet;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class djActivity extends Activity {

    private static djActivity instance;
    private PlaylistManager manager;

    private static final int SPOTIFY_REQUEST_CODE = 1337;

    private HashSet<Tweet> oldTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dj);
        instance = this;
        oldTweets = new HashSet<>();


        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO eventually put remote logging into a tree and put here.R
        }

        manager = PlaylistManager.getInstance();

        if(getSharedPreferences(getString(R.string.shared_prefs_name), MODE_PRIVATE).getString(getString(R.string.spotify_token_key), null) == null) {
            SpotifyAuthenticator.authenticate(this, 1337);
        }
    }

    public static djActivity getInstance() {
        return instance;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SPOTIFY_REQUEST_CODE) {
            Timber.i("Hit activity result");
            SpotifyAuthenticator.handleResponse(this, resultCode, intent);
        }
    }


    public void refresh(View view) {
        TwitterAPI api = TwitterAPI.getInstance();
        TwitterService service = api.getService();
        service.getTweets("#crowddj", new Callback<SearchList>() {
            @Override
            public void success(SearchList searchList, Response response) {
                for(Tweet tweet : searchList.tweets) {

                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}
