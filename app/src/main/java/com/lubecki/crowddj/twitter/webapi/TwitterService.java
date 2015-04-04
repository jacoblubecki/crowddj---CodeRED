package com.lubecki.crowddj.twitter.webapi;

import com.lubecki.crowddj.twitter.model.SearchList;
import com.lubecki.crowddj.twitter.model.Tweet;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Jacob on 4/3/15.
 */
public interface TwitterService {

    @GET("/search/tweets.json")
    void getTweets(@Query("q") String hashtag, Callback<SearchList> tweets);
}
