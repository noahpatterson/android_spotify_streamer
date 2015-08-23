package com.example.noahpatterson.spotifystreamer.view_holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.R;

/**
 * Created by noahpatterson on 8/23/15.
 */
public class TopTracksAdapterViewHolder {
    public ImageView albumImage;
    public TextView trackName;
    public TextView albumName;

    public TopTracksAdapterViewHolder(View view) {
        albumImage = (ImageView) view.findViewById(R.id.top_tracks_album_image);
        trackName = (TextView) view.findViewById(R.id.top_tracks_song_name);
        albumName = (TextView) view.findViewById(R.id.top_tracks_album_name);
    }
}
