package com.example.noahpatterson.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by noahpatterson on 7/1/15.
 */
public class ParcelableTrack implements Parcelable{
    public String name;
    public String albumName;
    public String albumImage;
    public String previewURL;
    public ArrayList<String> artistNames;
    public Long duration;

    public ParcelableTrack(String name, String albumName, String albumImage, String previewURL, ArrayList<String> artistNames, Long duration) {
        this.name = name;
        this.albumName = albumName;
        this.albumImage = albumImage;
        this.previewURL = previewURL;
        this.artistNames = artistNames;
        this.duration = duration;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(albumName);
        out.writeString(albumImage);
        out.writeString(previewURL);
        out.writeStringList(artistNames);
        out.writeLong(duration);
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR
            = new Parcelable.Creator<ParcelableTrack>() {
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    private ParcelableTrack(Parcel in) {
        name = in.readString();
        albumName = in.readString();
        albumImage = in.readString();
        previewURL = in.readString();
        artistNames = in.createStringArrayList();
        duration = in.readLong();
    }

}
