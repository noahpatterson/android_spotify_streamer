package com.example.noahpatterson.spotifystreamer.view_holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.R;

/**
 * Created by noahpatterson on 8/23/15.
 */
public class TopTracksViewHolder {
    public ListView top_tracks_list_view;
    public TextView artist_hero_name;
    public ImageView artist_hero_image;


    public TopTracksViewHolder(View view) {
        top_tracks_list_view = (ListView) view.findViewById(R.id.listview_top_tracks);
        artist_hero_name = (TextView) view.findViewById(R.id.artist_hero_name);
        artist_hero_image = (ImageView) view.findViewById(R.id.artist_hero_image);
    }
}
