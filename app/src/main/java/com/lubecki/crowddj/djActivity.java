package com.lubecki.crowddj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.lubecki.crowddj.spotify.models.Tracks;
import com.lubecki.crowddj.spotify.models.TracksPager;
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
    private ImageView imageView;
    private TextView songData;
    private ImageButton playPauseButton;

    private static final int SPOTIFY_REQUEST_CODE = 1337;

    private HashSet<String> oldTweets;

    private boolean allowRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dj);
        instance = this;
        oldTweets = new HashSet<>();

        TextView toolbar = (TextView) findViewById(R.id.title);

        Typeface font = Typeface.createFromAsset(getAssets(),"DIN Condensed Bold.ttf");
        toolbar.setTypeface(font);

        listView = (ListView) findViewById(R.id.listView);
        imageView = (ImageView) findViewById(R.id.album_art);
        songData = (TextView) findViewById(R.id.song_title);
        playPauseButton = (ImageButton) findViewById(R.id.play_pause);

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

    private void refreshList() {
        EditText editText = (EditText) findViewById(R.id.edit_query);

        if(!editText.getText().toString().trim().equals("")) {
            TwitterAPI api = TwitterAPI.getInstance();
            TwitterService service = api.getService();
            service.getTweets(editText.getText().toString(), new Callback<SearchList>() {
                @Override
                public void success(SearchList searchList, Response response) {

                    //trackids search -------------------------
                    SpotifyApi spotifyApi = new SpotifyApi();
                    SpotifyService spotifyService = spotifyApi.getService();

                    String tracksString = "";

                    ArrayList<String> trackIds = getSpotifyUrls(searchList);
                    for (int i = 0; i < trackIds.size(); i++) {
                        if(i > 0) {
                            tracksString += "," + trackIds.get(i);
                        }
                        else {
                            tracksString += trackIds.get(i);
                        }
                    }

                    spotifyService.getTracks(tracksString, new Callback<Tracks>() {
                        @Override
                        public void success(Tracks tracks, Response response) {
                            for(Track track : tracks.tracks) {
                                manager.addTrack(track);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });

                    //trackids search ------------------------- END

                    //query search ----------------------------

                    ArrayList<String> queries = getSpotifyQueries(searchList);

                    for(String query : queries) {
                        spotifyService.searchTracks(query, new Callback<TracksPager>() {
                            @Override
                            public void success(TracksPager pager, Response response) {
                                if(pager.tracks.items.size() > 0) {
                                    manager.addTrack(pager.tracks.items.get(0));
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


    public void refresh(View view) {
        refreshList();

        Timber.v("LIST REFRESHED" + manager.getTracks());
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
                        }
                    }
                }
                oldTweets.add(list.tweets[i].tweetId);
            }
        }

        return spotifyTracks;
    }

    private ArrayList<String> getSpotifyQueries(SearchList list) {
        ArrayList<String> queries = new ArrayList<>();

        for(int i = 0; i < list.tweets.length; i++) {
            if(!oldTweets.contains(list.tweets[i].tweetId)) {
                String[] split = list.tweets[i].tweetText.split("\\s+");
                String song = "";
                String artist = "";
                boolean foundBy = false;

                for(int j = 0; j < split.length - 1; j++) {
                    if(!foundBy) {
                        if(!split[j].equals("by")) {
                            song+=split[j] + " ";
                        } else {
                            foundBy = true;
                        }
                    }
                    else {
                        if(i < split.length) {
                            artist += split[j] + " ";
                        }
                    }
                }

                queries.add(song + " " + artist);
                oldTweets.add(list.tweets[i].tweetId);
            }
        }

        return queries;
    }

    public void PausePlay(View view) {
        if(manager.isPlaying()) {
            manager.pause();

            Bitmap play = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
            playPauseButton.setImageBitmap(play);

        } else {
            manager.play();

            Bitmap pause = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause);
            playPauseButton.setImageBitmap(pause);
        }
    }

    @Override
    public void requestLogin() {
        SpotifyAuthenticator.authenticate(this, SPOTIFY_REQUEST_CODE);
    }

    @Override
    public void trackStarted(Track track) {

        Timber.v("TRACK STARTED" + track.name);

        Picasso.with(this).load(track.album.images.get(0).url).into(imageView);
        songData.setText(track.name + "\n" + track.artists.get(0).name);

        Typeface font = Typeface.createFromAsset(getAssets(),"DIN Condensed Bold.ttf");
        songData.setTypeface(font);

        manager.getTracks().remove(0);
        updateList();

        if(allowRefresh) {

            Timber.v("TRACK REFRESHED" + track.name);
            refreshList();
        }

        allowRefresh = true;
    }

    @Override
    public void trackAdded() {
        updateList();
    }

    @Override
    public void errorCallback() {
        Toast.makeText(this, "Spotify Experienced a Fatal Error", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, djActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Track> tracks = manager.getTracks();

                listAdapter = new ListAdapter(getBaseContext(), R.layout.track_list_item, tracks);
                listView.setAdapter(listAdapter);
            }
        });
    }
}
