package com.axionteq.onlineradio.radio;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaMetaData implements Parcelable {

    private MediaMetaData(Parcel in) {
    }

    public static final Creator<MediaMetaData> CREATOR = new Creator<MediaMetaData>() {
        @Override
        public MediaMetaData createFromParcel(Parcel in) {
            return new MediaMetaData( in );
        }

        @Override
        public MediaMetaData[] newArray(int size) {
            return new MediaMetaData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
