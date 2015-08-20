package com.example.noahpatterson.spotifystreamer.service;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final IBinder playerBind = new PlayerBinder();

    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_RESET = "com.example.action.RESET";
    private static final String ACTION_SEEK = "com.example.action.SEEK";
    MediaPlayer mMediaPlayer;
    String playingURL = null;
    private View fragmentView =  null;
    private Thread updaterThread;
    private Boolean completed = false;
    Handler handler;

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

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void notifyUpdate() {
        Log.d("update thread", "updating seekbar");
        if (mMediaPlayer != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());

                    TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
                    String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(mMediaPlayer.getCurrentPosition()));
                    trackTimeTextView.setText(formattedDuration);
                }
            });

        }
    }


    private String name = "PlayerService" + new Random().nextInt();

    @Override
    public void onCreate() {
        Log.d("player service", "in onCreate: "+ name);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        handler = new Handler();
        super.onCreate();


    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void setFragmentView(View fragmentView) {
        this.fragmentView = fragmentView;
    }

    public void controlMusic(Intent intent) {
        Log.d("player service", "in controlMusic: "+ name);
        String previewURL = intent.getStringExtra("previewUrl");
        if (intent.getAction().equals(ACTION_PLAY)) {
             if (playingURL == previewURL && !completed) {
                 if (fragmentView != null) {
                     final SeekBar mSeekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
                     mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

                     final TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
                     String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(mMediaPlayer.getCurrentPosition()));
                     trackTimeTextView.setText(formattedDuration);
                 }
                 notifyStart();
                 mMediaPlayer.start();
            } else {
                 mMediaPlayer.reset();
                 completed = false;
                 try {
                     mMediaPlayer.setDataSource(previewURL);
                 } catch(IllegalArgumentException e) {
                     Log.e("PlayTrackService start", "malformed url");
                 } catch (IOException e) {
                     Log.e("PlayTrackService start", "track may not exist");
                 }

                 mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                 playingURL = previewURL;
            }

        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            if (mMediaPlayer != null && playingURL == previewURL) {
                updaterThread.interrupt();
                mMediaPlayer.pause();
            } else {
                ImageButton button = (ImageButton) fragmentView.findViewById(R.id.playerPlayButton);
                button.setImageResource(android.R.drawable.ic_media_pause);
                updaterThread.interrupt();
                mMediaPlayer.reset();
                completed = false;
                try {
                    mMediaPlayer.setDataSource(previewURL);
                } catch(IllegalArgumentException e) {
                    Log.e("PlayTrackService start", "malformed url");
                } catch (IOException e) {
                    Log.e("PlayTrackService start", "track may not exist");
                }

                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                playingURL = previewURL;
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
                final TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
                String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(mMediaPlayer.getCurrentPosition()));
                trackTimeTextView.setText(formattedDuration);
            }
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }


    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d("player service", "in onPrepared: "+ name);
        if(fragmentView != null) {
            final SeekBar mSeekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

            final TextView trackTimeTextView = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
            String formattedDuration = new SimpleDateFormat("mm:ss").format(new Date(mMediaPlayer.getCurrentPosition()));
            trackTimeTextView.setText(formattedDuration);
        }
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
        return playerBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("player service", "in onCompletion: "+ name);
        completed = true;
        updaterThread.interrupt();
        mp.reset();
//        mp.release();
//        mMediaPlayer = null;

        ImageButton playButton = (ImageButton)fragmentView.findViewById(R.id.playerPlayButton);
        playButton.setImageResource(android.R.drawable.ic_media_play);

        SeekBar seekBar = (SeekBar) fragmentView.findViewById(R.id.playerSeekBar);
        seekBar.setProgress(0);

        TextView trackTime = (TextView) fragmentView.findViewById(R.id.playerCurrentTrackPosition);
        trackTime.setText("00:00");



//
        // release and clear mMediaPlayer?
        // somehow update button and reset scrubBar
    }

    @Override
    public void onDestroy() {
        Log.d("player service", "in onDestroy: "+ name);
        if (mMediaPlayer != null) {
            updaterThread.interrupt();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
