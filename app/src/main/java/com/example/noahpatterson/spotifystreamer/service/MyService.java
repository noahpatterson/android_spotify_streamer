package com.example.noahpatterson.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MyService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_RESET = "com.example.action.RESET";
    MediaPlayer mMediaPlayer = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnPreparedListener(this);

                try {
                    mMediaPlayer.setDataSource(intent.getStringExtra("previewUrl"));
                } catch(IllegalArgumentException e) {
                    Log.e("PlayTrackService start", "malformed url");
                } catch (IOException e) {
                    Log.e("PlayTrackService start", "track may not exist");
                }

                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            } else {
                mMediaPlayer.start();
            }

        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } else if (intent.getAction().equals(ACTION_RESET)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
        return Service.START_NOT_STICKY;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("PlayTrackService start", "Error: " + what + ", " + extra);
        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

}
