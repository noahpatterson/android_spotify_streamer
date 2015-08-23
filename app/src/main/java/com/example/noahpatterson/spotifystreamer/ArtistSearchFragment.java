package com.example.noahpatterson.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noahpatterson.spotifystreamer.view_holder.ArtistsAdapterViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;

public class ArtistSearchFragment extends Fragment {

    private ArtistsAdapter adapter;
    private ArrayList<ParcelableArtist> mArtistArrayList;
    private Callbacks mCallbacks = itemSelectedCallback;
    private int itemSelectedPosition;
    private final static String LOG = "artist_search_fragment";

    // savedInstanceState constants
    private final static String SAVED_ARTIST_SEARCH = "saved_artist_search";
    private final static String SAVED_ITEM_SELECTED_POSITION = "itemSelectedPosition";

    // callback constants
    public final static String ARTIST_ID = "artist_id";
    public final static String ARTIST_NAME = "artist_name";
    public final static String ARTIST_LARGE_IMAGE = "artist_large_image";

    public ArtistSearchFragment() {
    }

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(Bundle bundle);
    }

    private static Callbacks itemSelectedCallback = new Callbacks() {
        @Override
        public void onItemSelected(Bundle bundle) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "in onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(LOG, "has savedInstanceState");

            // restore saved artist search list
            mArtistArrayList = savedInstanceState.getParcelableArrayList(SAVED_ARTIST_SEARCH);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG, "in onSaveInstanceState");

        // save artist seach list and artist selected position
        if (mArtistArrayList != null) {
            outState.putParcelableArrayList(SAVED_ARTIST_SEARCH, mArtistArrayList);
            outState.putInt(SAVED_ITEM_SELECTED_POSITION, itemSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "in onCreateView");
        if (savedInstanceState != null) {
            // restore save artist selected position
            itemSelectedPosition = savedInstanceState.getInt(SAVED_ITEM_SELECTED_POSITION, 0);
        }
        // get the fragment view
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        //find the list view
        final ListView artist_search_list_view = (ListView) fragmentView.findViewById(R.id.listview_artist_search);

        bindAdapterToListView(artist_search_list_view);

        searchForArtist(fragmentView);

        // start callback for top tracks based on selected artist in list
        artist_search_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemSelectedPosition = position;
                ParcelableArtist artist = (ParcelableArtist) artist_search_list_view.getItemAtPosition(position);
                Bundle args = new Bundle();

                args.putString(ARTIST_ID, artist.id);
                args.putString(ARTIST_NAME, artist.name);
                args.putString(ARTIST_LARGE_IMAGE, artist.large_image);

                mCallbacks.onItemSelected(args);
            }
        });
        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // set the callback to the itemSelected function
        mCallbacks = itemSelectedCallback;
    }

    private void bindAdapterToListView(ListView artist_search_list_view) {
        Log.d(LOG, "in bindAdapterToListView");
        // if we haven't previously searched or restored an artist,
        //   create a new list to use
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

            //use a viewholder for artist searches
            //TODO: not sure this saves anything
            ArtistsAdapterViewHolder viewHolder = new ArtistsAdapterViewHolder(convertView);

            if (artist.small_image == null) {
                Picasso.with(convertView.getContext()).load(R.drawable.no_artist).into(viewHolder.artistImage);
            } else {
                Picasso.with(convertView.getContext()).load(artist.small_image).into(viewHolder.artistImage);
           }

            viewHolder.artistName.setText(artist.name);
            return convertView;
        }
    }

    private void searchForArtist(View view) {
        Log.d(LOG, "in searchForArtist");
        
        //TODO: should these be moved to an earlier call or a viewholder
        EditText inputText = (EditText) view.findViewById(R.id.search_input);
        final ListView listView = (ListView) view.findViewById(R.id.listview_artist_search);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String artistToSearch = v.getText().toString();
                listView.setItemChecked(-1, true);
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !artistToSearch.isEmpty()) {

                    // closes the soft keyboard
                    v.clearFocus();
                    InputMethodManager in = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    new FetchArtistsTask().execute(artistToSearch);
                    return true;
                }
                Toast.makeText(getActivity().getApplicationContext(), R.string.blank_artist_name, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {
        RetrofitError artistSearchError;
        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {
                ArtistsPager results = spotify.searchArtists(params[0]);
                return (ArrayList<Artist>) results.artists.items;
            } catch (RetrofitError e) {
                artistSearchError = e;
                List<Artist> emptyArtistList = new ArrayList<>();
                return (ArrayList<Artist>) emptyArtistList;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            adapter.clear();
            if (artistSearchError != null) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            } else if (artists != null && !artists.isEmpty()) {
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
