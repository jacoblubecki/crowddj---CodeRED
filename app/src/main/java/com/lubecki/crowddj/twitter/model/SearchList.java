package com.lubecki.crowddj.twitter.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jacob on 4/4/15.
 */
public class SearchList {
    @SerializedName("statuses")
    public Tweet[] tweets;
}
