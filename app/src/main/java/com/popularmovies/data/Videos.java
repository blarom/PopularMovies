package com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Videos implements Parcelable {

    public Videos() { }

    private Videos(Parcel in) {
        id = in.readInt();
    }

    public static final Creator<Videos> CREATOR = new Creator<Videos>() {
        @Override
        public Videos createFromParcel(Parcel in) {
            return new Videos(in);
        }

        @Override
        public Videos[] newArray(int size) {
            return new Videos[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
    }

    @SerializedName("id")
    private int id;
    public void setIdNumber(int id) { this.id = id; }
    public int getIdNumber() { return id; }


    @SerializedName("results")
    private List<Results> results = new ArrayList<>();
    public void setResults(List<Results> results) { this.results = results; }
    public List<Results> getResults() { return results; }


    public static class Results implements Parcelable {

        Results() {}

        private Results(Parcel in) {
            id = in.readString();
            iso_639_1 = in.readString();
            iso_3166_1 = in.readString();
            key = in.readString();
            site = in.readString();
            size = in.readInt();
            type = in.readString();
        }

        public static final Creator<Results> CREATOR = new Creator<Results>() {
            @Override
            public Results createFromParcel(Parcel in) {
                return new Results(in);
            }

            @Override
            public Results[] newArray(int size) {
                return new Results[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(id);
            parcel.writeString(iso_639_1);
            parcel.writeString(iso_3166_1);
            parcel.writeString(key);
            parcel.writeString(site);
            parcel.writeInt(size);
            parcel.writeString(type);
        }

        @SerializedName("id")
        private String id;
        public void setIdNumber(String id) { this.id = id; }
        public String getIdNumber() { return id; }

        @SerializedName("iso_639_1")
        private String iso_639_1;
        public void setIso_639_1(String iso_639_1) { this.iso_639_1 = iso_639_1; }
        public String getIso_639_1() { return iso_639_1; }

        @SerializedName("iso_3166_1")
        private String iso_3166_1;
        public void setIso_3166_1(String iso_3166_1) { this.iso_3166_1 = iso_3166_1; }
        public String getIso_3166_1() { return iso_3166_1; }

        @SerializedName("key")
        private String key;
        public void setKey(String key) { this.key = key; }
        public String getKey() { return key; }

        @SerializedName("site")
        private String site;
        public void setSite(String site) { this.site = site; }
        public String getSite() { return site; }

        @SerializedName("size")
        private int size;
        public void setSize(int size) { this.size = size; }
        public int getSize() { return size; }

        @SerializedName("type")
        private String type;
        public void setType(String type) { this.type = type; }
        public String getType() { return type; }

    }
}
