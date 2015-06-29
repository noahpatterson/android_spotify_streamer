package com.example.noahpatterson.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by noahpatterson on 6/28/15.
 */
public class ParcelableArtist implements Parcelable {
    public String id;
    public String name;
    public String small_image;
    public String large_image;

    public ParcelableArtist(String id, String name, String small_image, String large_image) {
        this.id = id;
        this.name = name;
        this.small_image = small_image;
        this.large_image = large_image;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        //Edit: out.writeInt(mData);
        out.writeString(id);
        out.writeString(name);
        out.writeString(small_image);
        out.writeString(large_image);
    }

    public static final Parcelable.Creator<ParcelableArtist> CREATOR
            = new Parcelable.Creator<ParcelableArtist>() {
        public ParcelableArtist createFromParcel(Parcel in) {
            return new ParcelableArtist(in);
        }

        public ParcelableArtist[] newArray(int size) {
            return new ParcelableArtist[size];
        }
    };

    private ParcelableArtist(Parcel in) {
        //EDIT: mData = in.readInt();
        id = in.readString();
        name = in.readString();
        small_image = in.readString();
        large_image = in.readString();
    }
}
