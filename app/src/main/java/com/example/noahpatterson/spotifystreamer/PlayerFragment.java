package com.example.noahpatterson.spotifystreamer;

import android.app.ActivityManager;
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
    private PlayerViewHolder mPlayerViewHolder;
    private boolean hasService = false;
    private static final String LOG = "PlayerFragment";

    // Service broacast constants
    public static final String ACTION_PLAY_TRACK = "com.example.noahpatterson.spotifystreamer.PLAY_TRACK";
    public static final String ACTION_PAUSE_TRACK = "com.example.noahpatterson.spotifystreamer.PAUSE_TRACK";
    public static final String ACTION_SEEK_TRACK = "com.example.noahpatterson.spotifystreamer.SEEK_TRACK";
    public static final String SEEK_POSITION = "seek_position";
    public static final String TRACK_PREVIEW_URL = "previewURL";

    // onSaveInstantState constants
    public static final String PLAYING_URL = "playingURL";
    public static final String CURR_TRACK = "parcelableTrack";
    public static final String CURRENT_TRACK_LIST_POSITION = "currentPosition";
    public static final String IS_PLAYING = "playing";

    public PlayerFragment() {
    }

    // removes the titl from the dialog fragment when used as a dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "in onCreate");

        // here we make sure only to start 1 playerService at a time
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            // if the service exists, don't start another one
            if (PlayerService.class.getName().equals(service.service.getClassName())) {
                Log.d(LOG, "service is running");
                hasService = true;
            }
        }
        // otherwise start a new service
        if (!hasService) {
            Intent startPlayerService = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(startPlayerService);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "in onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_player, container, false);
        mPlayerViewHolder = new PlayerViewHolder(fragmentView);

        boolean isLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // on large layouts we get player data from the fragments arguments
        //TODO: there is duplication here
        if (isLargeLayout) {
            // this controls how big the player fragment dialog is
            fragmentView.setMinimumWidth(getArguments().getInt(TopTracksFragment.PLAYER_MIN_WIDTH));
            fragmentView.setMinimumHeight(getArguments().getInt(TopTracksFragment.PLAYER_MIN_HEIGHT));
            mParcelableTrackArrayList = getArguments().getParcelableArrayList(TopTracksFragment.ALL_TRACKS);

            // restore from saved instance if we can
            if (savedInstanceState != null) {
                playing = savedInstanceState.getBoolean(IS_PLAYING, false);
                playingURL = savedInstanceState.getString(PLAYING_URL, null);
                parcelableTrack = savedInstanceState.getParcelable(CURR_TRACK);
                mCurrentTrackPosition = savedInstanceState.getInt(CURRENT_TRACK_LIST_POSITION);
                seek = savedInstanceState.getInt(SEEK_POSITION);
            } else {
                parcelableTrack = getArguments().getParcelable(TopTracksFragment.CURR_TRACK);
                mCurrentTrackPosition = getArguments().getInt(TopTracksFragment.CURRENT_TRACK_LIST_POSITION, 0);
            }
        } else {
            // for phone views we get player data from an intent
            mParcelableTrackArrayList = getActivity().getIntent().getParcelableArrayListExtra(TopTracksFragment.ALL_TRACKS);

            if (savedInstanceState != null) {
                playing = savedInstanceState.getBoolean(IS_PLAYING, false);
                playingURL = savedInstanceState.getString(PLAYING_URL, null);
                parcelableTrack = savedInstanceState.getParcelable(CURR_TRACK);
                mCurrentTrackPosition = savedInstanceState.getInt(CURRENT_TRACK_LIST_POSITION);
                seek = savedInstanceState.getInt(SEEK_POSITION);
            } else {
                parcelableTrack = getActivity().getIntent().getParcelableExtra(TopTracksFragment.CURR_TRACK);
                mCurrentTrackPosition = getActivity().getIntent().getIntExtra(TopTracksFragment.CURRENT_TRACK_LIST_POSITION, 0);
            }
        }

        // add data to view
        populateView();

        //assign trackDuration
        //TODO: this should really be the preview track length, possibly obtained from mediaPlayer

        // make sure the play button is in the right state
        if (playing) {
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }

        // set playbutton click listener
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
        //assign trackDuration
        //TODO: this should really be the preview track length, possibly obtained from mediaPlayer
        mPlayerViewHolder.seekBar.setMax(30 * 1000);
        mPlayerViewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(progress));
                mPlayerViewHolder.trackTimeTextView.setText(formattedDuration);

                    // if the track is playing we need to tell the playerService to scrub the track
                    if (playing) {
                        Intent intent = new Intent(ACTION_SEEK_TRACK);
                        intent.putExtra(SEEK_POSITION, progress);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                    }

                    // store how far we seek
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
        Log.d(LOG, "in onStart");
        super.onStart();

    }

    @Override
    public void onResume() {
        Log.d(LOG, "in onResume");
        super.onResume();

        // start our PlayerService receivers
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
        Log.d(LOG, "in onPause");
        super.onPause();

        // trash the playerService receivers
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(currPositionReciever);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(playerCompleteReciever);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG, "in onSaveInstanceState");

        // save whether the track is playing
        outState.putBoolean(IS_PLAYING, playing);

        //if we have a track, store it's data
        if (parcelableTrack != null) {
            outState.putString(PLAYING_URL, parcelableTrack.previewURL);
            outState.putParcelable(CURR_TRACK, parcelableTrack);
            outState.putInt(CURRENT_TRACK_LIST_POSITION, mCurrentTrackPosition);
            outState.putInt(SEEK_POSITION, seek);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        Log.d(LOG, "in onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "in onDestroy");
        super.onDestroy();
    }

    public void playTrack(Context context) {
        Log.d(LOG, "in playTrack");

        // if playing and the current selected track's URL matches the PlayingURL we pause the track
        if (playing && parcelableTrack.previewURL.equals(playingURL)) {

            //pause command
            Intent intent = new Intent(ACTION_PAUSE_TRACK);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            // we are no longer playing
            playing = false;

            // swap the pause button to play
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_play);

            // store the track's playing position
            seek = mPlayerViewHolder.seekBar.getProgress();
        }

        // otherwise we start the selected track
        else {
            Intent intent = new Intent(ACTION_PLAY_TRACK);
            intent.putExtra(TRACK_PREVIEW_URL, parcelableTrack.previewURL);
            intent.putExtra(SEEK_POSITION, seek);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            playingURL = parcelableTrack.previewURL;
            mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    // receives the currently playing tracks seek position every second and updateas the UI
    private BroadcastReceiver currPositionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // PlayerService knows what it is playing so we update the view
//            if (playingURL == null) {
                playingURL = intent.getStringExtra(PlayerService.PLAYING_URL);
//            }

            // only update the seekbar and trackTime if we're viewing the playing track
            if (parcelableTrack.previewURL.equals(playingURL)) {
                int currPosition = intent.getIntExtra(PlayerService.CURR_TRACK_POSITION, 0);

                mPlayerViewHolder.playButton.setImageResource(android.R.drawable.ic_media_pause);
                mPlayerViewHolder.seekBar.setProgress(currPosition);

                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(currPosition));
                mPlayerViewHolder.trackTimeTextView.setText(formattedDuration);
                playing = true;

            }
        }
    };

    // knows when the track is finsihed playing. Resets the player UI
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

    // populates the player fragments view
    private void populateView() {
        //assign artistNames -- sometimes there are more than one artist
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

        //set track time
        String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(seek));
        mPlayerViewHolder.trackTimeTextView.setText(formattedDuration);
    }
}
