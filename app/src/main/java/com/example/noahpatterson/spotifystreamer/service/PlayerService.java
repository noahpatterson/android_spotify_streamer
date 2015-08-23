package com.example.noahpatterson.spotifystreamer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.noahpatterson.spotifystreamer.MainActivity;
import com.example.noahpatterson.spotifystreamer.ParcelableTrack;
import com.example.noahpatterson.spotifystreamer.PlayerFragment;
import com.example.noahpatterson.spotifystreamer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
<<<<<<< Updated upstream
=======
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_RESET = "com.example.action.RESET";
    private static final String ACTION_SEEK = "com.example.action.SEEK";
    public static final int NOTIFICATION_ID = 1;
>>>>>>> Stashed changes
    public static final String ACTION_CURR_POSITION = "com.example.action.CURR_POSITION";
    public static final String ACTION_COMPLETE = "com.example.action.CURR_COMPLETE";
    private MediaPlayer mMediaPlayer;
    private String playingURL = null;
    private Thread updaterThread;
    private Handler handler;
    private int seek = 0;
<<<<<<< Updated upstream
    private String LOG = "player service";
=======
    private ParcelableTrack playingTrack;
    private int trackListPosition;
    private ArrayList<ParcelableTrack> parcelableTracks;
>>>>>>> Stashed changes


    // Intent String Constants
    public static final String CURR_TRACK_POSITION = "current_track_position";
    public static final String PLAYING_URL = "playingURL";

    // this controls updating the seekBar and time while playing
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
                        Log.e(LOG, "Error pausing update thread");
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

    // sends a broadcast to the fragment to update seek and track time in real time
    private void notifyUpdate() {
        Log.d(LOG, "updating seekbar");
        if (mMediaPlayer != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                   Intent currPositionIntent = new Intent(ACTION_CURR_POSITION);
                    currPositionIntent.putExtra(CURR_TRACK_POSITION, mMediaPlayer.getCurrentPosition());
                    currPositionIntent.putExtra(PLAYING_URL, playingURL);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(currPositionIntent);
                }
            });
        }
    }

    // random name for the service for logging
    private String name = "PlayerService" + new Random().nextInt();

    @Override
    public void onCreate() {
        Log.d(LOG, "in onCreate: " + name);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        handler = new Handler();

        // register receivers for the pause, play, and seek actions from the fragment
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
        Log.d(LOG, "in onStartCommand: " + name);
        // moved normal implementation to the broadcast receivers because it seems easier to manipulate
        return 1;
    }

    private BroadcastReceiver playTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
<<<<<<< Updated upstream
            Log.d(LOG, "in playTrackReciever");
            String previewURL = intent.getStringExtra(PlayerFragment.TRACK_PREVIEW_URL);

            // play request to start an existing track
            if (playingURL != null && playingURL.equals(previewURL)) {
                mMediaPlayer.seekTo(intent.getIntExtra(PlayerFragment.SEEK_POSITION, 0));
=======
            Log.d("player service", "in playTrackReciever");
            String previewURL = intent.getStringExtra("previewURL");
            int seekFromIntent = intent.getIntExtra("seek", 0);
            playingTrack = intent.getParcelableExtra("playingTrack");
            trackListPosition = intent.getIntExtra("trackListPosition", 0);
            parcelableTracks = intent.getParcelableArrayListExtra("arrayListTracks");

            // play seeked and/or paused track
            if (playingURL != null && playingURL.equals(previewURL)) {
                mMediaPlayer.seekTo(seekFromIntent);
>>>>>>> Stashed changes
                notifyStart();
                mMediaPlayer.start();
            }
            // otherwise play the newly selected track
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
<<<<<<< Updated upstream
                seek = intent.getIntExtra(PlayerFragment.SEEK_POSITION, 0);
=======
                seek = seekFromIntent;
>>>>>>> Stashed changes
            }
        }
    };

    // receives a pause command from the fragment
    private BroadcastReceiver pauseTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG, "in pauseTrackReciever");
            updaterThread.interrupt();
            mMediaPlayer.pause();
        }
    };

    // receives a seek command from the fragment
    private BroadcastReceiver seekTrackReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG, "in seekTrackReciever");
            int seekPosition = intent.getIntExtra(PlayerFragment.SEEK_POSITION,0);

            // only seek if there is a player to seek on
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(seekPosition);
            }
        }
    };

    private void notificationStart() {

        // open app main pending intent
        Intent notIntent = new Intent(this, MainActivity.class);

        String nextTrack;
        String previousTrack;
        if (trackListPosition == 0) {
            previousTrack = null;
        } else {
            previousTrack = parcelableTracks.get(trackListPosition - 1).previewURL;
        }
        if (trackListPosition == parcelableTracks.size() -1) {
            nextTrack = null;
        } else {
            nextTrack = parcelableTracks.get(trackListPosition + 1).previewURL;
        }

        //play track pending intent

        //pause track pending intent

        //next track pending intent
        Intent nextTrackIntent = new Intent(PlayerFragment.ACTION_PLAY_TRACK);
        nextTrackIntent.putExtra("previewURL", nextTrack);
        nextTrackIntent.putExtra("seek", 0);
        nextTrackIntent.putExtra("playingTrack", playingTrack);
        nextTrackIntent.putExtra("trackListPosition", trackListPosition + 1);
        nextTrackIntent.putExtra("arrayListTracks", parcelableTracks);
        PendingIntent pendingNextTrack = PendingIntent.getBroadcast(getApplicationContext(),0, nextTrackIntent, 0);

        //previous track pending intent
        Intent previousTrackIntent = new Intent(PlayerFragment.ACTION_PLAY_TRACK);
        previousTrackIntent.putExtra("previewURL", previousTrack);
        previousTrackIntent.putExtra("seek", 0);
        previousTrackIntent.putExtra("playingTrack", playingTrack);
        previousTrackIntent.putExtra("trackListPosition", trackListPosition - 1);
        previousTrackIntent.putExtra("arrayListTracks", parcelableTracks);
        PendingIntent pendingPreviousTrack = PendingIntent.getBroadcast(getApplicationContext(),0, previousTrackIntent, 0);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Playing")
                        .setContentText(playingTrack.name)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Playing: " + playingTrack.artistNames + "-" + playingTrack.name))
                        .addAction(android.R.drawable.ic_media_next, "Next", pendingNextTrack)
                        .addAction(android.R.drawable.ic_media_previous, "Pevious", pendingPreviousTrack);


        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        //                stackBuilder.addParentStack(DetailActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notIntent);
        PendingIntent pendInt =  stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(pendInt);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//        startForeground(NOTIFICATION_ID, mBuilder.build());

    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(LOG, "in onPrepared: "+ name);
        player.seekTo(seek);
        notifyStart();
        player.start();
        notificationStart();
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
        Log.d(LOG, "in onCompletion: "+ name);
        updaterThread.interrupt();
        playingURL = null;
        mp.reset();

        // send a track completed broadcast to the fragment
        Intent playerComplete = new Intent(ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playerComplete);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "in onDestroy: "+ name);
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
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
