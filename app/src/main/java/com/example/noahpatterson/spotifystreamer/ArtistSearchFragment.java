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

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

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
        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        if ( mArtistArrayList == null || mArtistArrayList.isEmpty()) {
            mArtistArrayList = new ArrayList<ParcelableArtist>();
        }

        adapter = new ArtistsAdapter(getActivity(), mArtistArrayList);

        final ListView artist_search_list_view = (ListView) fragmentView.findViewById(R.id.listview_artist_search);
        artist_search_list_view.setAdapter(adapter);


        searchForArtist(fragmentView);


        artist_search_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableArtist artist = (ParcelableArtist) artist_search_list_view.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), TopTracks.class);

                intent.putExtra(Intent.EXTRA_TEXT, artist.id );

                startActivity(intent);
            }
        });

        return fragmentView;
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
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    String artistToSearch = v.getText().toString();
                    new FetchArtistsTask().execute(artistToSearch);
                  return false;
                }
                return false;
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
            if (artists != null && !artists.isEmpty()) {
                ArrayList<ParcelableArtist> parcelableArtists = new ArrayList<ParcelableArtist>();
                for (Artist artist : artists) {
                    String name = artist.name;
                    String id = artist.id;
                    String small_image;
                    String large_image;

                    if (artist.images.isEmpty()) {
                        small_image = null;
                        large_image = null;
                    } else {
                        small_image = artist.images.get(2).url;
                        large_image = artist.images.get(0).url;
                    }
                    parcelableArtists.add(new ParcelableArtist(id,name, small_image, large_image));
                }

                adapter.clear();
                adapter.addAll(parcelableArtists);
                mArtistArrayList = parcelableArtists;
            } else {
                adapter.clear();
                Toast.makeText(getActivity().getApplicationContext(),"Sorry no artists found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
