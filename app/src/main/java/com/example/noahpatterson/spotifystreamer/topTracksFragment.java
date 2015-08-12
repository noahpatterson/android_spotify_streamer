package com.example.noahpatterson.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    private TracksAdapter adapter;
    // do member variable have special lifecycle in java or is this just convention?
    private ArrayList<ParcelableTrack> mArrayOfTracks;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        if (savedInstanceState != null) {
            mArrayOfTracks = savedInstanceState.getParcelableArrayList("saved_top_tracks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mArrayOfTracks != null) {
            outState.putParcelableArrayList("saved_top_tracks", mArrayOfTracks);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        Intent artistIntent = getActivity().getIntent();
        String artistName = artistIntent.getStringExtra("artist_name");
        String artistID = artistIntent.getStringExtra("artist_id");

        addArtistNameToActionBar(artistName);

        createArtistHeroLayout(fragmentView, artistIntent, artistName);

        bindAdapterToListView(fragmentView, artistID);

        return fragmentView;
    }

    private void bindAdapterToListView(View fragmentView, String artistID) {
        if ( mArrayOfTracks == null || mArrayOfTracks.isEmpty()) {
            mArrayOfTracks = new ArrayList<>();
            new FetchTopTracksTask().execute(artistID);
        }

        adapter = new TracksAdapter(getActivity(), mArrayOfTracks);

        ListView top_tracks_list_view = (ListView) fragmentView.findViewById(R.id.listview_top_tracks);
        top_tracks_list_view.setAdapter(adapter);

        // create list view item click listener
        top_tracks_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableTrack parcelableTrack = (ParcelableTrack) parent.getItemAtPosition(position);
                Intent playerIntent = new Intent(getActivity(), PlayerActivity.class);

//                playerIntent.putExtra("previewURL", parcelableTrack.previewURL);
//                playerIntent.putExtra("artistName", parcelableTrack.artistNames);
//                playerIntent.putExtra("albumName", parcelableTrack.albumName);
//                playerIntent.putExtra("albumImage", parcelableTrack.albumImage);
//                playerIntent.putExtra("trackName", parcelableTrack.name);
//                playerIntent.putExtra("duration", 0);
                playerIntent.putExtra("track", parcelableTrack);

                startActivity(playerIntent);
            }
        });
    }

    private void createArtistHeroLayout(View fragmentView, Intent artistIntent, String artistName) {
        TextView artist_hero_name = (TextView) fragmentView.findViewById(R.id.artist_hero_name);
        artist_hero_name.setText(artistName);

        ImageView artist_hero_image = (ImageView) fragmentView.findViewById(R.id.artist_hero_image);
        String large_image_url = artistIntent.getStringExtra("artist_large_image");

        if (TextUtils.isEmpty(large_image_url)) {
            Picasso.with(fragmentView.getContext()).load(R.drawable.noalbum).into(artist_hero_image);
        } else {
            Picasso.with(fragmentView.getContext()).load(large_image_url).into(artist_hero_image);
        }
    }

    private void addArtistNameToActionBar(String artistName) {
        android.support.v7.app.ActionBar actionBar = ((TopTracks) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(artistName + "'s Top Tracks");
        }
    }

    public class TracksAdapter extends ArrayAdapter<ParcelableTrack> {

        public TracksAdapter(Context context, ArrayList<ParcelableTrack> tracks){
            super(context,0,tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ParcelableTrack track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_top_tracks, parent, false);
            }

            ImageView albumImage = (ImageView) convertView.findViewById(R.id.top_tracks_album_image);
            TextView trackName = (TextView) convertView.findViewById(R.id.top_tracks_song_name);
            TextView albumName = (TextView) convertView.findViewById(R.id.top_tracks_album_name);

            if (track.albumImage == null) {
                Picasso.with(convertView.getContext()).load(R.drawable.noalbum).into(albumImage);
            } else {
                Picasso.with(convertView.getContext()).load(track.albumImage).into(albumImage);
            }

            trackName.setText(track.name);
            albumName.setText(track.albumName);

            return convertView;
        }
    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {
        RetrofitError topTracksError;
        @Override
        protected List<Track> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {
                Map<String,Object> queryCountry = new android.support.v4.util.ArrayMap<>(1);
                queryCountry.put("country","US");
                return spotify.getArtistTopTrack(params[0], queryCountry).tracks;
            } catch (RetrofitError e) {
                topTracksError = e;
                return new ArrayList<Track>();
                }
            }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            adapter.clear();
            if (topTracksError != null) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            } else if (tracks != null && !tracks.isEmpty()) {
                ArrayList<ParcelableTrack> parcelableTracks = populateParcelableTrackList(tracks);
                adapter.addAll(parcelableTracks);
                mArrayOfTracks = parcelableTracks;
            } else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.no_tracks_found, Toast.LENGTH_SHORT).show();
            }
        }

        private ArrayList<ParcelableTrack> populateParcelableTrackList(List<Track> tracks) {
            ArrayList<ParcelableTrack> parcelableTracks = new ArrayList<>();
            for (Track track : tracks ) {
                String albumImage = track.album.images.isEmpty() ? null : track.album.images.get(0).url;
                ArrayList<String> artistArrayList = new ArrayList<>();
                for (ArtistSimple artist : track.artists) {
                    artistArrayList.add(artist.name);
                }
                parcelableTracks.add(new ParcelableTrack(track.name, track.album.name, albumImage, track.preview_url, artistArrayList, track.duration_ms ));
            }
            return parcelableTracks;
        }
    }
}
