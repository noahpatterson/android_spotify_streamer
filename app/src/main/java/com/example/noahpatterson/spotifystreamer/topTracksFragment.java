package com.example.noahpatterson.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class topTracksFragment extends Fragment {

    private TracksAdapter adapter;

    public topTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Intent artistIntent = getActivity().getIntent();
        String artistID = artistIntent.getStringExtra(Intent.EXTRA_TEXT);

        ArrayList<Track> arrayOfTracks = new ArrayList<Track>();
        adapter = new TracksAdapter(getActivity(), arrayOfTracks);

        final ListView top_tracks_list_view = (ListView) fragmentView.findViewById(R.id.listview_top_tracks);
        top_tracks_list_view.setAdapter(adapter);

        new FetchTopTracksTask().execute(artistID);
        return fragmentView;
    }

    public class TracksAdapter extends ArrayAdapter<Track> {

        public TracksAdapter(Context context, ArrayList<Track> tracks){
            super(context,0,tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Track track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_top_tracks, parent, false);
            }

            ImageView albumImage = (ImageView) convertView.findViewById(R.id.top_tracks_album_image);
            TextView trackName = (TextView) convertView.findViewById(R.id.top_tracks_song_name);
            TextView albumName = (TextView) convertView.findViewById(R.id.top_tracks_album_name);

            if (track.album.images.isEmpty()) {
                Picasso.with(convertView.getContext()).load(R.drawable.noalbum).into(albumImage);
            } else {
                Picasso.with(convertView.getContext()).load(track.album.images.get(1).url).into(albumImage);
            }

            trackName.setText(track.name);
            albumName.setText(track.album.name);

            return convertView;
        }
    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {
        @Override
        protected List<Track> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String,Object> queryCountry = new HashMap<String, Object>();
            queryCountry.put("country","US");
            return spotify.getArtistTopTrack(params[0], queryCountry).tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null && tracks.isEmpty() == false) {
                adapter.clear();
                adapter.addAll(tracks);
            } else {
                adapter.clear();
                Toast.makeText(getActivity().getApplicationContext(), "Sorry no tracks found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
