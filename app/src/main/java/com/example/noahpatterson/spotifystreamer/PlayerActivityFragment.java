package com.example.noahpatterson.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_player, container, false);

        // populate player layout
        //pull track parceable
        ParcelableTrack parcelableTrack = getActivity().getIntent().getParcelableExtra("track");

        //assign artistNames
        TextView artistNameTextView = (TextView) fragmentView.findViewById(R.id.playerArtistName);
        String allArtistNames = "";
        for (String artistName : parcelableTrack.artistNames) {
            allArtistNames += artistName + " ";
        }
        artistNameTextView.setText(allArtistNames);

        //assign albumName
        TextView albumNameTextView = (TextView) fragmentView.findViewById(R.id.playerAlbumName);
        albumNameTextView.setText(parcelableTrack.albumName);

        //assign albumImage
        ImageView albumImageImageView = (ImageView) fragmentView.findViewById(R.id.playerAlbumImage);
        if (TextUtils.isEmpty(parcelableTrack.albumImage)) {
            Picasso.with(fragmentView.getContext()).load(R.drawable.noalbum).into(albumImageImageView);
        } else {
            Picasso.with(fragmentView.getContext()).load(parcelableTrack.albumImage).into(albumImageImageView);
        }

        //assign trackDuration
        TextView trackNameTextView = (TextView) fragmentView.findViewById(R.id.playerTrackName);
        trackNameTextView.setText(parcelableTrack.name);

        //assign trackDuration
        TextView trackDurationTextView = (TextView) fragmentView.findViewById(R.id.playerTotalTrackTime);
        String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(parcelableTrack.duration));
        trackDurationTextView.setText(formattedDuration);

        return fragmentView;
    }
}
