package com.example.noahpatterson.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noahpatterson.spotifystreamer.service.PlayerService;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment{

    private ParcelableTrack parcelableTrack = null;
    private Boolean playing = false;
    private String playingURL;
    private int seek = 0;
//    private PlayerService musicSrv;
//    private Intent playIntent;
//    private boolean musicBound=false;
    private View fragmentView;
    private ArrayList<ParcelableTrack> mParcelableTrackArrayList;
    private int mCurrentTrackPosition;
//    private Thread seekBarThread;
//    private int seekBarProgress = 0;
//    PlayerService.PlayerBinder binder;
//    private ServiceConnection musicConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d("PlayerActivityFragment", "in onServiceConnected");
//            binder = (PlayerService.PlayerBinder) service;
//
//            //get service
//            musicSrv = binder.getService();
//            musicBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
//        }
//    };

    public PlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("PlayerActivityFragment", "in onCreate");
        super.onCreate(savedInstanceState);
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
//        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        playIntent = new Intent(getActivity(), PlayerService.class);
//        getActivity().startService(playIntent);
//        getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);


        Log.d("PlayerActivityFragment", "in onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_player, container, false);

        // populate player layout
        //pull track parceable

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

        //assign trackDurationc
        TextView trackNameTextView = (TextView) fragmentView.findViewById(R.id.playerTrackName);
        trackNameTextView.setText(parcelableTrack.name);

        //assign trackDuration
        //TODO: this should really be the preview track length, possibly obtained from mediaPlayer
//        TextView trackDurationTextView = (TextView) fragmentView.findViewById(R.id.playerTotalTrackTime);
//        String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(parcelableTrack.duration));
//        trackDurationTextView.setText(formattedDuration);

        // set playbutton click listener
        final ImageButton playButton = (ImageButton) fragmentView.findViewById(R.id.playerPlayButton);
        if (playing) {
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playButton.setImageResource(android.R.drawable.ic_media_play);
        }
//        if (musicSrv == null && playing) {
//            playButton.setImageResource(android.R.drawable.ic_media_pause);
//        } else if (musicSrv != null && musicSrv.getMediaPlayer().isPlaying()) {
//            playButton.setImageResource(android.R.drawable.ic_media_pause);
//        } else {
//            playButton.setImageResource(android.R.drawable.ic_media_play);
//        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrack(fragmentView.getContext());
            }
        });

        //next track
        final ImageButton nextTrackButton = (ImageButton) fragmentView.findViewById(R.id.playerNextButton);
        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentTrackPosition == mParcelableTrackArrayList.size() -1) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.end_of_top_tracks_list, Toast.LENGTH_LONG).show();
                } else {
                    parcelableTrack = mParcelableTrackArrayList.get(mCurrentTrackPosition + 1);
                    mCurrentTrackPosition += 1;
                    //swap track info
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

                    //assign trackDurationc
                    TextView trackNameTextView = (TextView) fragmentView.findViewById(R.id.playerTrackName);
                    trackNameTextView.setText(parcelableTrack.name);

                    //play track
                    seek = 0;
                    playTrack(fragmentView.getContext());
                }
            }
        });

        //previous track
        final ImageButton prevTrackButton = (ImageButton) fragmentView.findViewById(R.id.playerPreviousButton);
        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentTrackPosition == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.end_of_top_tracks_list, Toast.LENGTH_LONG).show();
                } else {
                    parcelableTrack = mParcelableTrackArrayList.get(mCurrentTrackPosition - 1);
                    mCurrentTrackPosition -= 1;
                    //swap track info
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

                    //assign trackDurationc
                    TextView trackNameTextView = (TextView) fragmentView.findViewById(R.id.playerTrackName);
                    trackNameTextView.setText(parcelableTrack.name);

                    //play track
                    seek = 0;
                    playTrack(fragmentView.getContext());
                }
            }
        });

        //set seekBar change listener
        //TODO: sync MediaPlayer position to seekbar
        final SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
        seekBar.setMax(30 * 1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    final TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(progress));
                trackTimeTextView.setText(formattedDuration);

                    if (playing) {
                        Intent intent = new Intent(fragmentView.getContext(), PlayerService.class);
                        intent.putExtra("seek_position", progress);
                        intent.setAction("com.example.action.SEEK");
                        getActivity().startService(intent);
//                    musicSrv.controlMusic(intent);
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
        Log.d("PlayerActivityFragment", "in onStart");
        super.onStart();

    }

    @Override
    public void onResume() {
        Log.d("PlayerActivityFragment", "in onResume");
        super.onResume();
        IntentFilter currPositionFilter = new IntentFilter(PlayerService.ACTION_CURR_POSITION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(currPositionReciever, currPositionFilter);

        IntentFilter playerCompleteFilter = new IntentFilter(PlayerService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playerCompleteReciever, playerCompleteFilter);
    }

    @Override
    public void onPause() {
        Log.d("PlayerActivityFragment", "in onPause");
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(currPositionReciever);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(playerCompleteReciever);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("PlayerActivityFragment", "in onSaveInstanceState");
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
        Log.d("PlayerActivityFragment", "in onStop");
//        if (musicConnection != null) {
//            getActivity().unbindService(musicConnection);
//        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("PlayerActivityFragment", "in onDestroy");
//        getActivity().unbindService(musicConnection);
        super.onDestroy();
    }



    public void playTrack(Context context) {
        Log.d("PlayerActivityFragment", "in playTrack");
        ImageButton button = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
//        if (musicSrv != null) {
//            musicSrv.setFragmentView(fragmentView);
//        }
//        ImageButton button = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
        Intent intent = new Intent(context, PlayerService.class);

        if (playing && parcelableTrack.previewURL.equals(playingURL)) {
            //pause command
            intent.setAction("com.example.action.PAUSE");
            getActivity().startService(intent);
            playing = false;
            button.setImageResource(android.R.drawable.ic_media_play);
            final SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
            seek = seekBar.getProgress();

        }
//        else if (playing == 1) {
//            //start new track
//            intent.putExtra("previewURL", parcelableTrack.previewURL);
//            intent.setAction("com.example.action.PLAY");
//            getActivity().startService(intent);
//        }
        else {
            intent.putExtra("previewURL", parcelableTrack.previewURL);
            intent.putExtra("seek", seek);
            intent.setAction("com.example.action.PLAY");
            getActivity().startService(intent);
            playingURL = parcelableTrack.previewURL;
            button.setImageResource(android.R.drawable.ic_media_pause);
        }
//        intent.putExtra("previewUrl", parcelableTrack.previewURL);
//        if (musicSrv != null && musicSrv.getMediaPlayer().isPlaying()) {
//            button.setImageResource(android.R.drawable.ic_media_play);
//            intent.setAction("com.example.action.PAUSE");
////            getActivity().startService(intent);
//            musicSrv.controlMusic(intent);
//            playing = false;
//
//        } else {
//            button.setImageResource(android.R.drawable.ic_media_pause);
//            intent.setAction("com.example.action.PLAY");
////            getActivity().startService(intent);
//            musicSrv.controlMusic(intent);
//            playing = true;
//
//        }
    }

    private BroadcastReceiver currPositionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (parcelableTrack.previewURL.equals(playingURL)) {
                int currPosition = intent.getIntExtra("currPosition", 0);
                SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
                seekBar.setProgress(currPosition);

                TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(currPosition));
                trackTimeTextView.setText(formattedDuration);
                playing = true;
            }
        }
    };

    private BroadcastReceiver playerCompleteReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ImageButton playButton = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
            playButton.setImageResource(android.R.drawable.ic_media_play);

            SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
            seekBar.setProgress(0);

            TextView trackTime = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
            trackTime.setText("00:00");
            playing = false;
            seek = 0;
        }
    };




}
