package com.lubecki.crowddj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lubecki.crowddj.R;
import com.lubecki.crowddj.spotify.models.Track;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Track> {

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<Track> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.track_list_item, null);

        }

        Track track = getItem(position);

        if (track != null) {

            TextView songTitle = (TextView) v.findViewById(R.id.song_title);
            TextView artistName = (TextView) v.findViewById(R.id.artist_name);

            if (songTitle != null) {
                songTitle.setText(track.name);
            }

            if (artistName != null) {
                artistName.setText(track.artists.get(0).name);
            }
        }

        return v;
    }


}