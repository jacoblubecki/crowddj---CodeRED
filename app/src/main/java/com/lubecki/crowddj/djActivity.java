package com.lubecki.crowddj;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lubecki.crowddj.managers.PlaylistManager;
import com.lubecki.crowddj.spotify.models.SpotifyTrack;
import com.lubecki.crowddj.spotify.webapi.SpotifyApi;
import com.lubecki.crowddj.spotify.webapi.SpotifyService;
import com.lubecki.crowddj.twitter.model.SearchList;
import com.lubecki.crowddj.twitter.model.Tweet;
import com.lubecki.crowddj.twitter.model.TweetUrls;
import com.lubecki.crowddj.twitter.webapi.TwitterAPI;
import com.lubecki.crowddj.twitter.webapi.TwitterService;

import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.lubecki.crowddj.spotify.EndTrackCallBack;
import com.lubecki.crowddj.spotify.SpotifyAuthenticator;
import com.lubecki.crowddj.spotify.SpotifyPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class djActivity extends ActionBarActivity {

    private static djActivity instance;
    private PlaylistManager manager;

    private static final int SPOTIFY_REQUEST_CODE = 1337;

    private HashSet<String> oldTweets;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dj, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh(View view) {
        TwitterAPI api = TwitterAPI.getInstance();
        TwitterService service = api.getService();
        service.getTweets("#crowddj", new Callback<SearchList>() {
            @Override
            public void success(SearchList searchList, Response response) {

            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getSpotifyUrls(SearchList list) {
        ArrayList<String> updatedIds = new ArrayList<>();

        for(int i = 0; i < list.tweets.length; i++) {
            if(!oldTweets.contains(list.tweets[i].tweetId)) {
                TweetUrls[] urls = list.tweets[i].tweetEntities.urlList;

                for(TweetUrls tweetUrls : urls) {

                }
            }
        }
    }
}
