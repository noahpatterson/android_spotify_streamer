package com.example.noahpatterson.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.service.PlayerService;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment{

    private ParcelableTrack parcelableTrack = null;
    private Boolean playing = false;
    private PlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private View fragmentView;
    private Thread seekBarThread;
    private int seekBarProgress = 0;
    PlayerService.PlayerBinder binder;
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("PlayerActivityFragment", "in onServiceConnected");
            binder = (PlayerService.PlayerBinder) service;

            //get service
            musicSrv = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };;

    public PlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("PlayerActivityFragment", "in onCreate");
        //bind play button click listener


        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            playing = savedInstanceState.getBoolean("isPlaying");
        }

//        if(playIntent==null){



        setRetainInstance(true);
    }

//    }

    @Override
    public void onStart() {
        Log.d("PlayerActivityFragment", "in onStart");
        super.onStart();

    }

    //connect to the service

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        playIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(playIntent);
        getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);


        Log.d("PlayerActivityFragment", "in onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_player, container, false);

        // populate player layout
        //pull track parceable
        parcelableTrack = getActivity().getIntent().getParcelableExtra("track");

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
        if (musicSrv == null && playing) {
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else if (musicSrv != null && musicSrv.getMediaPlayer().isPlaying()) {
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playButton.setImageResource(android.R.drawable.ic_media_play);
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrack(fragmentView.getContext(), fragmentView);
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
                    Intent intent = new Intent(fragmentView.getContext(), PlayerService.class);
                    intent.putExtra("seek_position", progress);
                    intent.setAction("com.example.action.SEEK");
//                    getActivity().startService(intent);
                    musicSrv.controlMusic(intent);
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


    public void playTrack(Context context, final View fragmentView) {
        Log.d("PlayerActivityFragment", "in playTrack");
        if (musicSrv != null) {
            musicSrv.setFragmentView(fragmentView);
        }
        ImageButton button = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra("previewUrl", parcelableTrack.previewURL);
        if (musicSrv != null && musicSrv.getMediaPlayer().isPlaying()) {
            button.setImageResource(android.R.drawable.ic_media_play);
            intent.setAction("com.example.action.PAUSE");
//            getActivity().startService(intent);
            musicSrv.controlMusic(intent);
            playing = false;

        } else {
            button.setImageResource(android.R.drawable.ic_media_pause);
            intent.setAction("com.example.action.PLAY");
//            getActivity().startService(intent);
//            PlayTrackService.start(context, parcelableTrack.previewURL);
            musicSrv.controlMusic(intent);
            playing = true;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("PlayerActivityFragment", "in onDestroy");
        outState.putBoolean("isPlaying", musicSrv.getMediaPlayer().isPlaying());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d("PlayerActivityFragment", "in onDestroy");
//        getActivity().unbindService(musicConnection);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Log.d("PlayerActivityFragment", "in onStop");
        if (musicConnection != null) {
            getActivity().unbindService(musicConnection);
        }
        super.onStop();

    }
}
