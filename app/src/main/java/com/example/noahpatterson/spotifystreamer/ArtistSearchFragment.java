package com.example.noahpatterson.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistSearchFragment extends Fragment {

    private ArtistsAdapter adapter;
    private ArrayList<ParcelableArtist> mArtistArrayList;

    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView artist_search_list_view = (ListView) fragmentView.findViewById(R.id.listview_artist_search);

        bindAdapterToListView(artist_search_list_view);

        searchForArtist(fragmentView);

        artist_search_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableArtist artist = (ParcelableArtist) artist_search_list_view.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), TopTracks.class);

                intent.putExtra("artist_id", artist.id );
                intent.putExtra("artist_name", artist.name);
                intent.putExtra("artist_large_image", artist.large_image);

                startActivity(intent);
            }
        });

        return fragmentView;
    }

    private void bindAdapterToListView(ListView artist_search_list_view) {
        if ( mArtistArrayList == null || mArtistArrayList.isEmpty()) {
            mArtistArrayList = new ArrayList<>();
        }

        adapter = new ArtistsAdapter(getActivity(), mArtistArrayList);

        artist_search_list_view.setAdapter(adapter);
    }

    public class ArtistsAdapter extends ArrayAdapter<ParcelableArtist> {

        public ArtistsAdapter(Context context, ArrayList<ParcelableArtist> artists){
            super(context,0,artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ParcelableArtist artist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist_search, parent, false);
            }

            ImageView artistImage = (ImageView) convertView.findViewById(R.id.artist_search_artist_image);
            TextView artistName = (TextView) convertView.findViewById(R.id.artist_search_artist_name);

            if (artist.small_image == null) {
                Picasso.with(convertView.getContext()).load(R.drawable.no_artist).into(artistImage);
            } else {
                Picasso.with(convertView.getContext()).load(artist.small_image).into(artistImage);
           }

            artistName.setText(artist.name);

            return convertView;
        }
    }

    private void searchForArtist(View view) {
        EditText inputText = (EditText) view.findViewById(R.id.search_input);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String artistToSearch = v.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !artistToSearch.isEmpty()) {
                    new FetchArtistsTask().execute(artistToSearch);
                    return true;
                }
                Toast.makeText(getActivity().getApplicationContext(), R.string.blank_artist_name, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {
        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);
            return (ArrayList<Artist>) results.artists.items;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            adapter.clear();
            if (artists != null && !artists.isEmpty()) {
                ArrayList<ParcelableArtist> parcelableArtists = new ArrayList<>();

                populateParcelableArtistList(artists, parcelableArtists);

                adapter.addAll(parcelableArtists);
                mArtistArrayList = parcelableArtists;
            } else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.no_artists_found, Toast.LENGTH_LONG).show();
            }
        }

        private void populateParcelableArtistList(ArrayList<Artist> artists, ArrayList<ParcelableArtist> parcelableArtists) {
            for (Artist artist : artists) {
                String small_image = null;
                String large_image = null;

                // is it cheaper in java to declare these variables or do the object traversal multiple times?
                final List<Image> images = artist.images;
                final int imageListSize = images.size();

                if (imageListSize >= 3) {
                    small_image = images.get(2).url;
                    large_image = images.get(0).url;
                }
                if (imageListSize > 0 && imageListSize < 3) {
                    small_image = images.get(0).url;
                    large_image = small_image;
                }
                parcelableArtists.add(new ParcelableArtist(artist.id, artist.name, small_image, large_image));
            }
        }
    }

}
