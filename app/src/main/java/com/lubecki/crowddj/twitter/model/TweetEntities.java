package com.lubecki.crowddj.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tbrown on 1/17/15.
 */
public class TweetEntities {

    @SerializedName("urls")
    public TweetUrls urlList[];

    @SerializedName("hashtags")
    public HashTag[] hashTags;

}