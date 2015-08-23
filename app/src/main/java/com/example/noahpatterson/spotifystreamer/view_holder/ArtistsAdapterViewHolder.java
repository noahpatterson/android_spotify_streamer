package com.example.noahpatterson.spotifystreamer.view_holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.R;

/**
 * Created by noahpatterson on 8/23/15.
 */
public class ArtistsAdapterViewHolder {
    public ImageView artistImage;
    public TextView artistName;

    public ArtistsAdapterViewHolder(View view) {
        artistImage = (ImageView) view.findViewById(R.id.artist_search_artist_image);
        artistName = (TextView) view.findViewById(R.id.artist_search_artist_name);
    }
}
