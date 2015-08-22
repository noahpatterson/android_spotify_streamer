package com.example.noahpatterson.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.Callbacks {

    private boolean largeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("main_activity", "in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        largeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("main_activity", "in onRestoreInstanceState");
    }

    @Override
    public void onItemSelected(Bundle bundle) {
        if (largeLayout) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.

            Intent intent = new Intent(this, TopTracksActivity.class);

            intent.putExtra("artist_id", bundle.getString("artist_id"));
            intent.putExtra("artist_name", bundle.getString("artist_name"));
            intent.putExtra("artist_large_image", bundle.getString("artist_large_image"));

            startActivity(intent);
        }
    }
}
