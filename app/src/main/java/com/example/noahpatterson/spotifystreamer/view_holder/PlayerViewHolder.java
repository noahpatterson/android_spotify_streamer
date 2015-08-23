package com.example.noahpatterson.spotifystreamer.view_holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.R;

/**
 * Created by noahpatterson on 8/23/15.
 */
public class PlayerViewHolder {
    public TextView trackTimeTextView;
    public SeekBar seekBar;
    public ImageButton playButton;
    public TextView trackNameTextView;
    public TextView albumNameTextView;
    public ImageView albumImageImageView;
    public TextView artistNameTextView;
    public ImageButton prevTrackButton;
    public ImageButton nextTrackButton;

    public PlayerViewHolder(View view) {
        trackTimeTextView   = (TextView) view.findViewById(R.id.playerCurrentTrackPosition);
        seekBar             = (SeekBar) view.findViewById(R.id.playerSeekBar);
        playButton          = (ImageButton)view.findViewById(R.id.playerPlayButton);
        trackNameTextView   = (TextView) view.findViewById(R.id.playerTrackName);
        albumNameTextView   = (TextView) view.findViewById(R.id.playerAlbumName);
        albumImageImageView = (ImageView) view.findViewById(R.id.playerAlbumImage);
        artistNameTextView  = (TextView) view.findViewById(R.id.playerArtistName);
        prevTrackButton     = (ImageButton) view.findViewById(R.id.playerPreviousButton);
        nextTrackButton     = (ImageButton) view.findViewById(R.id.playerNextButton);

    }
}