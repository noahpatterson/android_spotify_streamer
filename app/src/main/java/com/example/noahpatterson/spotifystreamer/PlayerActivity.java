package com.example.noahpatterson.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("playerActivity", "in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }

}
