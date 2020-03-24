package com.axionteq.onlineradio.radio.radio;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RadioType implements Parcelable {

    @Expose
    @SerializedName("title")
    private String radioTitle;

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("image")
    private String radioImage;

    private int playState;

    @Expose
    @SerializedName("pastor")
    private String radioPastor;

    @Expose
    @SerializedName("radiolink")
    private String radioLink;

    @Expose
    @SerializedName("subtitle")
    private String radioSubtitle;

    RadioType(String radioTitle, String radioImage, String radioLink, String radioPastor, String radioSubtitle) {

        this.radioTitle = radioTitle;
        this.radioImage = radioImage;
        this.radioLink = radioLink;
        this.radioPastor = radioPastor;
        this.radioSubtitle = radioSubtitle;
    }

    public int getPlayState() {
        return playState;
    }

    void setPlayState(int playState) {
        this.playState = playState;
    }

    String getRadioSubtitle() {
        return radioSubtitle;
    }

    public void setRadioSubtitle(String radioSubtitle) {
        this.radioSubtitle = radioSubtitle;
    }

    String getRadioPastor() {
        return radioPastor;
    }

    String getRadioTitle() {
        return radioTitle;
    }

    public void setRadioTitle(String radioTitle) {
        this.radioTitle = radioTitle;
    }

    String getRadioImage() {
        return radioImage;
    }

    public void setRadioImage(String radioImage) {
        this.radioImage = radioImage;
    }

    String getRadioLink() {
        return radioLink;
    }

    public void setRadioLink(String radioLink) {
        this.radioLink = radioLink;
    }

    public void setRadioPastor(String radioPastor) {
        this.radioPastor = radioPastor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RadioType() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString( radioPastor );
        parcel.writeString( radioSubtitle );
        parcel.writeString( radioTitle );
        parcel.writeString( radioImage );
        parcel.writeString( id );
    }

    protected RadioType(Parcel in) {
        radioPastor = in.readString();
        radioSubtitle = in.readString();
        radioTitle = in.readString();
        radioImage = in.readString();
        playState = in.readInt();
        id = in.readString();
    }

    public static final Parcelable.Creator<RadioType> CREATOR = new Parcelable.Creator<RadioType>() {
        @Override
        public RadioType createFromParcel(Parcel in) {
            return new RadioType( in );
        }

        @Override
        public RadioType[] newArray(int size) {
            return new RadioType[size];
        }
    };
}
