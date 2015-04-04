package com.lubecki.crowddj;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lubecki.crowddj.adapter.ListAdapter;
import com.lubecki.crowddj.managers.PlaylistManager;
import com.lubecki.crowddj.spotify.LoginRequester;
import com.lubecki.crowddj.spotify.SpotifyAuthenticator;
import com.lubecki.crowddj.spotify.TrackCallBack;
import com.lubecki.crowddj.spotify.models.Track;
import com.lubecki.crowddj.spotify.webapi.SpotifyApi;
import com.lubecki.crowddj.spotify.webapi.SpotifyService;
import com.lubecki.crowddj.twitter.model.SearchList;
import com.lubecki.crowddj.twitter.model.TweetUrls;
import com.lubecki.crowddj.twitter.webapi.TwitterAPI;
import com.lubecki.crowddj.twitter.webapi.TwitterService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class djActivity extends ActionBarActivity implements LoginRequester, TrackCallBack {

    private static djActivity instance;
    private PlaylistManager manager;

    private ListAdapter listAdapter;
    private ListView listView;

    private static final int SPOTIFY_REQUEST_CODE = 1337;

    private HashSet<String> oldTweets;

    private int toBeQueued = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dj);
        instance = this;
        oldTweets = new HashSet<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);




        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO eventually put remote logging into a tree and put here.R
        }

        manager = PlaylistManager.getInstance();
        manager.setLoginRequestListener(this);
        manager.setTrackChangeListener(this);

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
        EditText editText = (EditText) findViewById(R.id.edit_query);

        if(!editText.getText().toString().trim().equals("")) {
            TwitterAPI api = TwitterAPI.getInstance();
            TwitterService service = api.getService();
            service.getTweets(editText.getText().toString(), new Callback<SearchList>() {
                @Override
                public void success(SearchList searchList, Response response) {
                    SpotifyApi spotifyApi = new SpotifyApi();
                    SpotifyService spotifyService = spotifyApi.getService();

                    for (String trackID : getSpotifyUrls(searchList)) {
                        spotifyService.getTrack(trackID, new Callback<Track>() {
                            @Override
                            public void success(Track track, Response response) {
                                manager.addTrack(track);
                                toBeQueued--;

                                if (toBeQueued == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateList();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }
                }



                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
        else {
            Toast.makeText(this, "Please Enter a Hashtag.", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateList() {
        ArrayList<Track> tracks = manager.getTracks();

        listAdapter = new ListAdapter(this, R.layout.track_list_item, tracks);
        listView.setAdapter(listAdapter);
    }

    private ArrayList<String> getSpotifyUrls(SearchList list) {
        ArrayList<String> spotifyTracks = new ArrayList<>();

        for(int i = 0; i < list.tweets.length; i++) {
            if(!oldTweets.contains(list.tweets[i].tweetId)) {
                TweetUrls[] urls = list.tweets[i].tweetEntities.urlList;

                for(TweetUrls tweetUrls : urls) {
                    if(tweetUrls.expandedUrl.contains("open.spotify.com/track")) {
                        Pattern pattern = Pattern.compile("/track.*/([^/]+)/?$");
                        Matcher matcher = pattern.matcher(tweetUrls.expandedUrl);
                        if(matcher.find()) {
                            Timber.v(matcher.group(1));
                            spotifyTracks.add(matcher.group(1));
                            toBeQueued++;
                        }
                    }
                }
            }
            oldTweets.add(list.tweets[i].tweetId);
        }

        return spotifyTracks;
    }

    private ArrayList<String> getSpotifyQueries(SearchList list) {
        ArrayList<String> spotifyTracks = new ArrayList<>();

        for(int i = 0; i < list.tweets.length; i++) {
            if(!oldTweets.contains(list.tweets[i].tweetId)) {
                spotifyTracks.add(list.tweets[i].tweetText);
                toBeQueued++;
            }
            oldTweets.add(list.tweets[i].tweetId);
        }

        return spotifyTracks;
    }

    public void PausePlay(View view) {
        if(manager.isPlaying()) {
            manager.pause();
        } else {
            manager.play();
        }
    }

    @Override
    public void requestLogin() {
        SpotifyAuthenticator.authenticate(this, 1337);
    }

    @Override
    public void trackStarted(Track track) {
        ImageView imageView = (ImageView) findViewById(R.id.album_art);
        Picasso.with(this).load(track.album.images.get(0).url).into(imageView);

        TextView songData = (TextView) findViewById(R.id.song_title);
        songData.setText(track.name + "\n" + track.artists.get(0).name);
        Typeface font = Typeface.createFromAsset(getAssets(),"DIN Condensed Bold.ttf");
        songData.setTypeface(font);
    }

    @Override
    public void trackAdded() {

    }
}
