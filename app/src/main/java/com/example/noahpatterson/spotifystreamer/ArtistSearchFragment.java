package com.example.noahpatterson.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;



/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<Artist> arrayOfArtists = new ArrayList<Artist>();
        ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), arrayOfArtists);

        final ListView artist_search_list_view = (ListView) fragmentView.findViewById(R.id.listview_artist_search);
        artist_search_list_view.setAdapter(adapter);

        Artist testArtist = new Artist("Noah", "https://i.scdn.co/image/18141db33353a7b84c311b7068e29ea53fad2326");
        adapter.add(testArtist);

        return fragmentView;
    }

    public class ArtistsAdapter extends ArrayAdapter<Artist> {

        public ArtistsAdapter(Context context, ArrayList<Artist> artists){
            super(context,0,artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Artist artist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist_search, parent, false);
            }

            ImageView artistImage = (ImageView) convertView.findViewById(R.id.artist_search_artist_image);
            TextView artistName = (TextView) convertView.findViewById(R.id.artist_search_artist_name);

            Picasso.with(convertView.getContext()).load(artist.artistImageURL).into(artistImage);
            artistName.setText(artist.name);

            return convertView;
        }
    }
}
