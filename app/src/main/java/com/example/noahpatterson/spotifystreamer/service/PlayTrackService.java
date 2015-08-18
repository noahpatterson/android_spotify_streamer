package com.example.noahpatterson.spotifystreamer.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PlayTrackService extends IntentService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    MediaPlayer mMediaPlayer;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String START = "com.example.noahpatterson.spotifystreamer.service.action.START";
    public static final String PAUSE = "com.example.noahpatterson.spotifystreamer.service.action.PAUSE";

    // TODO: Rename parameters
    private static final String TRACK_URI = "com.example.noahpatterson.spotifystreamer.service.extra.TRACK_URI";

    /**
     * Starts this service to perform action start with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void start(Context context, String param1) {
        Intent intent = new Intent(context, PlayTrackService.class);
        intent.setAction(START);
        intent.putExtra(TRACK_URI, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action puase with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void pause(Context context) {
        Intent intent = new Intent(context, PlayTrackService.class);
        intent.setAction(PAUSE);
        context.startService(intent);
    }

    public PlayTrackService() {
        super("PlayTrackService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (START.equals(action)) {
                final String param1 = intent.getStringExtra(TRACK_URI);
                handleStart(param1);
            } else if (PAUSE.equals(action)) {
                handlePause();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleStart(String url) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        try {
            mMediaPlayer.setDataSource(url);
        } catch(IllegalArgumentException e) {
            Log.e("PlayTrackService start", "malformed url");
        } catch (IOException e) {
            Log.e("PlayTrackService start", "track may not exist");
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handlePause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }

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
}
