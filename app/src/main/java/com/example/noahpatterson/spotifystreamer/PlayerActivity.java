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

//        if (getSupportFragmentManager().findFragmentByTag("player") == null) {
//            PlayerFragment fragment = new PlayerFragment();
//
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("track", getIntent().getParcelableExtra("track"));
//            bundle.putParcelableArrayList("allTracks", getIntent().getParcelableArrayListExtra("allTracks"));
//            bundle.putInt("currentTrackPosition", getIntent().getIntExtra("currentTrackPosition", 0));
//
//            fragment.setArguments(bundle);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.player_container, fragment)
//                    .addToBackStack("player")
//                    .commit();
//        }

    }

}
