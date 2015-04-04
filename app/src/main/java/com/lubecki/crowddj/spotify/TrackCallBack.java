package com.lubecki.crowddj.spotify;

import com.lubecki.crowddj.spotify.models.Track;

/**
 * Created by JoshBeridon on 4/3/15.
 */
public interface TrackCallBack {
    public void trackStarted(Track track);
    public void trackAdded();
    public void errorCallback();
}
