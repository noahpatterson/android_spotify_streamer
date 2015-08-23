package com.example.noahpatterson.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noahpatterson.spotifystreamer.service.PlayerService;
import com.example.noahpatterson.spotifystreamer.view_holder.PlayerViewHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    private ParcelableTrack parcelableTrack = null;
    private Boolean playing = false;
    private String playingURL;
    private int seek = 0;
    private View fragmentView;
    private ArrayList<ParcelableTrack> mParcelableTrackArrayList;
    private int mCurrentTrackPosition;
    private boolean isLargeLayout;
    private PlayerViewHolder mPlayerViewHolder;

    public PlayerFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("PlayerFragment", "in onCreate");
        super.onCreate(savedInstanceState);
//
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("PlayerFragment", "in onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_player, container, false);
        mPlayerViewHolder = new PlayerViewHolder(fragmentView);

        isLargeLayout = getResources().getBoolean(R.bool.large_layout);

        if (isLargeLayout) {
        mParcelableTrackArrayList = getArguments().getParcelableArrayList("allTracks");

        if (savedInstanceState != null) {
            playing = savedInstanceState.getBoolean("playing", false);
            playingURL = savedInstanceState.getString("playingURL", null);
            parcelableTrack = savedInstanceState.getParcelable("parcelableTrack");
            mCurrentTrackPosition = savedInstanceState.getInt("currentPosition");
        } else {
            parcelableTrack = getArguments().getParcelable("track");
            mCurrentTrackPosition = getArguments().getInt("currentTrackPosition", 0);
        }
        } else {
            mParcelableTrackArrayList = getActivity().getIntent().getParcelableArrayListExtra("allTracks");

            if (savedInstanceState != null) {
                playing = savedInstanceState.getBoolean("playing", false);
                playingURL = savedInstanceState.getString("playingURL", null);
                parcelableTrack = savedInstanceState.getParcelable("parcelableTrack");
                mCurrentTrackPosition = savedInstanceState.getInt("currentPosition");
            } else {
                parcelableTrack = getActivity().getIntent().getParcelableExtra("track");
                mCurrentTrackPosition = getActivity().getIntent().getIntExtra("currentTrackPosition", 0);
            }
        }

        // add data to view
        populateView();

        //assign trackDurationc
        TextView trackNameTextView = (TextView) fragmentView.findViewById(R.id.playerTrackName);
        trackNameTextView.setText(parcelableTrack.name);

        //assign trackDuration
        //TODO: this should really be the preview track length, possibly obtained from mediaPlayer

        // set playbutton click listener
        if (playing) {
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }
        mPlayerViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrack(fragmentView.getContext());
            }
        });

        //next track
        mPlayerViewHolder.nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentTrackPosition == mParcelableTrackArrayList.size() -1) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.end_of_top_tracks_list, Toast.LENGTH_LONG).show();
                } else {
                    parcelableTrack = mParcelableTrackArrayList.get(mCurrentTrackPosition + 1);
                    mCurrentTrackPosition += 1;

                    // add data to view
                    populateView();

                    //play track
                    seek = 0;
                    playTrack(fragmentView.getContext());
                }
            }
        });

        //previous track
        mPlayerViewHolder.prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentTrackPosition == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.end_of_top_tracks_list, Toast.LENGTH_LONG).show();
                } else {
                    parcelableTrack = mParcelableTrackArrayList.get(mCurrentTrackPosition - 1);
                    mCurrentTrackPosition -= 1;

                    // add data to view
                    populateView();

                    //play track
                    seek = 0;
                    playTrack(fragmentView.getContext());
                }
            }
        });

        //set seekBar change listener
        //TODO: sync MediaPlayer position to seekbar
        mPlayerViewHolder.seekBar.setMax(30 * 1000);
        mPlayerViewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(progress));
                mPlayerViewHolder.trackTimeTextView.setText(formattedDuration);

                    if (playing) {
                        Intent intent = new Intent(fragmentView.getContext(), PlayerService.class);
                        intent.putExtra("seek_position", progress);
                        intent.setAction("com.example.action.SEEK");
                        getActivity().startService(intent);
                    }
                    seek = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return fragmentView;
    }

    @Override
    public void onStart() {
        Log.d("PlayerFragment", "in onStart");
        super.onStart();

    }

    @Override
    public void onResume() {
        Log.d("PlayerFragment", "in onResume");
        super.onResume();
        IntentFilter currPositionFilter = new IntentFilter(PlayerService.ACTION_CURR_POSITION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(currPositionReciever, currPositionFilter);

        // play track when view loads if no track is playing
        // this feature is per the rubric, though personally not auto-playing is better UX
//        if (!playing) {
//            playTrack(fragmentView.getContext());
//        }

        IntentFilter playerCompleteFilter = new IntentFilter(PlayerService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playerCompleteReciever, playerCompleteFilter);
    }

    @Override
    public void onPause() {
        Log.d("PlayerFragment", "in onPause");
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(currPositionReciever);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(playerCompleteReciever);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("PlayerFragment", "in onSaveInstanceState");
//        outState.putBoolean("isPlaying", musicSrv.getMediaPlayer().isPlaying());
//        Boolean statePressed = false;
//
//        int[] states = fragmentView.findViewById(R.id.playerPlayButton).getDrawableState();
//        for (int state : states)
//        {
//            if (state == android.R.attr.state_pressed) {
//                statePressed = true;
//            }
//        }

        outState.putBoolean("playing", playing);
        if (parcelableTrack != null) {
            outState.putString("playingURL", parcelableTrack.previewURL);
            outState.putParcelable("parcelableTrack", parcelableTrack);
            outState.putInt("currentPosition", mCurrentTrackPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        Log.d("PlayerFragment", "in onStop");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("PlayerFragment", "in onDestroy");
//        getActivity().unbindService(musicConnection);
        super.onDestroy();
    }



    public void playTrack(Context context) {
        Log.d("PlayerFragment", "in playTrack");
        Intent intent = new Intent(context, PlayerService.class);

        if (playing && parcelableTrack.previewURL.equals(playingURL)) {
            //pause command
            intent.setAction("com.example.action.PAUSE");
            getActivity().startService(intent);
            playing = false;
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);
            seek = mPlayerViewHolder.seekBar.getProgress();
        }
        else {
            intent.putExtra("previewURL", parcelableTrack.previewURL);
            intent.putExtra("seek", seek);
            intent.setAction("com.example.action.PLAY");
            getActivity().startService(intent);
            playingURL = parcelableTrack.previewURL;
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private BroadcastReceiver currPositionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (playingURL == null) {
                playingURL = intent.getStringExtra("playingURL");
            }
            if (parcelableTrack.previewURL.equals(playingURL)) {
                int currPosition = intent.getIntExtra("currPosition", 0);

                mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
                mPlayerViewHolder.seekBar.setProgress(currPosition);

                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(currPosition));
                mPlayerViewHolder.trackTimeTextView.setText(formattedDuration);
                playing = true;

            }
        }
    };

    private BroadcastReceiver playerCompleteReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayerViewHolder.seekBar.setProgress(0);
            mPlayerViewHolder.trackTimeTextView.setText("00:00");
            playing = false;
            seek = 0;
        }
    };

    private void populateView() {
        //assign artistNames
        String allArtistNames = "";
        for (String artistName : parcelableTrack.artistNames) {
            allArtistNames += artistName + " ";
        }
        mPlayerViewHolder.artistNameTextView.setText(allArtistNames);

        //assign albumName
        mPlayerViewHolder.albumNameTextView.setText(parcelableTrack.albumName);

        //assign albumImage
        if (TextUtils.isEmpty(parcelableTrack.albumImage)) {
            Picasso.with(fragmentView.getContext()).load(R.drawable.noalbum).into(mPlayerViewHolder.albumImageImageView);
        } else {
            Picasso.with(fragmentView.getContext()).load(parcelableTrack.albumImage).into(mPlayerViewHolder.albumImageImageView);
        }

        //assign trackName
        mPlayerViewHolder.trackNameTextView.setText(parcelableTrack.name);
    }
}
