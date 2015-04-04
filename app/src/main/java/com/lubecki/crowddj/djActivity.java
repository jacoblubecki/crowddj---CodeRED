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
import com.lubecki.crowddj.twitter.model.SearchList;
import com.lubecki.crowddj.twitter.model.Tweet;
import com.lubecki.crowddj.twitter.webapi.TwitterAPI;
import com.lubecki.crowddj.twitter.webapi.TwitterService;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.lubecki.crowddj.spotify.EndTrackCallBack;
import com.lubecki.crowddj.spotify.SpotifyAuthenticator;
import com.lubecki.crowddj.spotify.SpotifyPlayer;

import timber.log.Timber;

public class djActivity extends ActionBarActivity {

    private static djActivity instance;
    private PlaylistManager manager;

    private SpotifyPlayer spotifyPlayer;
    private static final int SPOTIFY_REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SpotifyAuthenticator.authenticate(this, 1337);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO eventually put remote logging into a tree and put here.R
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dj);
        instance = this;

        manager = PlaylistManager.getInstance();

        TwitterAPI api = TwitterAPI.getInstance();
        TwitterService service = api.getService();
        service.getTweets("#crowddj", new Callback<SearchList>() {
            @Override
            public void success(SearchList list, Response response) {
                for(Tweet tweet : list.tweets) {
                    Log.v("TEST.JPG", tweet.tweetEntities.hashTags[0].text);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("TEST.PNG", error.getMessage());
            }
        });

        spotifyPlayer = new SpotifyPlayer(this, "2TpxZ7JUBn3uw46aR7qd6V" , new EndTrackCallBack() {
            @Override
            public void trackEnded() {

            }
        });

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
    }
}
