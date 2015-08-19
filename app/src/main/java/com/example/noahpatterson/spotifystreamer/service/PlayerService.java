package com.example.noahpatterson.spotifystreamer.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.noahpatterson.spotifystreamer.R;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final IBinder playerBind = new PlayerBinder();

    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_RESET = "com.example.action.RESET";
    private static final String ACTION_SEEK = "com.example.action.SEEK";
    MediaPlayer mMediaPlayer;
    String playingURL = null;
    private View fragmentView =  null;
    private Activity fragmentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void setFragmentView(View fragmentView) {
        this.fragmentView = fragmentView;
    }
    public void setFragmentActivity(Activity activity) {
        this.fragmentActivity = activity;
    }

    public void controlMusic(Intent intent) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            String previewURL = intent.getStringExtra("previewUrl");
            if (playingURL != previewURL) {
                mMediaPlayer.reset();
                try {
                    mMediaPlayer.setDataSource(previewURL);
                } catch(IllegalArgumentException e) {
                    Log.e("PlayTrackService start", "malformed url");
                } catch (IOException e) {
                    Log.e("PlayTrackService start", "track may not exist");
                }

                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                playingURL = previewURL;
            } else {
                mMediaPlayer.start();
            }

        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } else if (intent.getAction().equals(ACTION_RESET)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
        } else if (intent.getAction().equals(ACTION_SEEK)) {
            int seekPosition = intent.getIntExtra("seek_position",0);
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(seekPosition);
            }
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
        final SeekBar mSeekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
        final Handler mHandler = new Handler();
        //Make sure you update Seekbar on UI thread
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    int mCurrentPosition = mMediaPlayer.getCurrentPosition() / 1000;
                    mSeekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("PlayTrackService start", "Error: " + what + ", " + extra);
        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return playerBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("player service", "in onCompletion");
        mp.reset();
        ImageButton playButton = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
        playButton.setImageResource(android.R.drawable.ic_media_play);

        SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
        seekBar.setProgress(0);

        TextView trackTime = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
        trackTime.setText("00:00");

//        mp.release();
//        mMediaPlayer = null;
        // release and clear mMediaPlayer?
        // somehow update button and reset scrubBar
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
