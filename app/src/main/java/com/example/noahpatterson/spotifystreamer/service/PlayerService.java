package com.example.noahpatterson.spotifystreamer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.noahpatterson.spotifystreamer.PlayerFragment;

import java.io.IOException;
import java.util.Random;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_RESET = "com.example.action.RESET";
    private static final String ACTION_SEEK = "com.example.action.SEEK";
    public static final String ACTION_CURR_POSITION = "com.example.action.CURR_POSITION";
    public static final String ACTION_COMPLETE = "com.example.action.CURR_COMPLETE";
    MediaPlayer mMediaPlayer;
    String playingURL = null;
    private Thread updaterThread;
    Handler handler;
    private int seek = 0;

    private void startUpdater() {
        updaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread myCurrent = Thread.currentThread();
                while (!myCurrent.isInterrupted() && updaterThread == myCurrent) {
                    notifyUpdate();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e("update thread", "Error pausing update thread");
                        break;
                    }
                }
            }
        });
        updaterThread.start();
    }

    private void notifyStart() {
        startUpdater();
    }

    private void notifyUpdate() {
        Log.d("update thread", "updating seekbar");
        if (mMediaPlayer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                   Intent currPositionIntent = new Intent(ACTION_CURR_POSITION);
                    currPositionIntent.putExtra("currPosition", mMediaPlayer.getCurrentPosition());
                    currPositionIntent.putExtra("playingURL", playingURL);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(currPositionIntent);
                }
            });
        }
    }


    private String name = "PlayerService" + new Random().nextInt();

    @Override
    public void onCreate() {
        Log.d("player service", "in onCreate: " + name);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        handler = new Handler();

        IntentFilter playTrackFilter = new IntentFilter(PlayerFragment.ACTION_PLAY_TRACK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(playTrackReciever, playTrackFilter);

        IntentFilter pauseTrackFilter = new IntentFilter(PlayerFragment.ACTION_PAUSE_TRACK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(pauseTrackReciever, pauseTrackFilter);

        IntentFilter seekTrackFilter = new IntentFilter(PlayerFragment.ACTION_SEEK_TRACK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(seekTrackReciever, seekTrackFilter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("player service", "in onStartCommand: " + name);

        return 1;
    }

    private BroadcastReceiver playTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("player service", "in playTrackReciever");
            String previewURL = intent.getStringExtra("previewURL");
            if (playingURL != null && playingURL.equals(previewURL)) {
                mMediaPlayer.seekTo(intent.getIntExtra("seek", 0));
                notifyStart();
                mMediaPlayer.start();
            }
            else
            {
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
                seek = intent.getIntExtra("seek", 0);
            }
        }
    };
    private BroadcastReceiver pauseTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("player service", "in pauseTrackReciever");
            updaterThread.interrupt();
            mMediaPlayer.pause();
        }
    };
    private BroadcastReceiver seekTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("player service", "in seekTrackReciever");
            int seekPosition = intent.getIntExtra("seek_position",0);
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(seekPosition);
            }
        }
    };

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d("player service", "in onPrepared: "+ name);
        player.seekTo(seek);
        notifyStart();
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
        Log.d("player service", "in onCompletion: "+ name);
        updaterThread.interrupt();
        playingURL = null;
        mp.reset();

        Intent playerComplete = new Intent(ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playerComplete);
    }

    @Override
    public void onDestroy() {
        Log.d("player service", "in onDestroy: "+ name);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(playTrackReciever);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(pauseTrackReciever);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(seekTrackReciever);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (updaterThread != null) {
            updaterThread.interrupt();
        }
    }
}
