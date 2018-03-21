package com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Reviews implements Parcelable {

    public Reviews() { }

    private Reviews(Parcel in) {
        id = in.readInt();
        page = in.readInt();
        total_results = in.readInt();
        total_pages = in.readInt();
    }

    public static final Creator<Reviews> CREATOR = new Creator<Reviews>() {
        @Override
        public Reviews createFromParcel(Parcel in) {
            return new Reviews(in);
        }

        @Override
        public Reviews[] newArray(int size) {
            return new Reviews[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(page);
        parcel.writeInt(total_results);
        parcel.writeInt(total_pages);
    }

    @SerializedName("id")
    private int id;
    public void setIdNumber(int id) { this.id = id; }
    public int getIdNumber() { return id; }

    @SerializedName("page")
    private int page;
    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }

    @SerializedName("total_results")
    private int total_results;
    public void setTotalResults(int total_results) { this.total_results = total_results; }
    public int getTotalResults() { return total_results; }

    @SerializedName("total_pages")
    private int total_pages;
    public void setTotalPages(int total_pages) { this.total_pages = total_pages; }
    public int getTotalPages() { return total_pages; }

    @SerializedName("results")
    private List<Results> results = new ArrayList<>();
    public void setResults(List<Results> results) { this.results = results; }
    public List<Results> getResults() { return results; }


    public static class Results implements Parcelable {

        Results() {}

        private Results(Parcel in) {
            id = in.readString();
            author = in.readString();
            content = in.readString();
            url = in.readString();
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
            parcel.writeString(author);
            parcel.writeString(content);
            parcel.writeString(url);
        }

        @SerializedName("id")
        private String id;
        public void setIdNumber(String id) { this.id = id; }
        public String getIdNumber() { return id; }

        @SerializedName("author")
        private String author;
        public void setAuthor(String author) { this.author = author; }
        public String getAuthor() { return author; }

        @SerializedName("content")
        private String content;
        public void setContent(String content) { this.content = content; }
        public String getContent() { return content; }

        @SerializedName("url")
        private String url;
        public void setUrl(String url) { this.url = url; }
        public String getUrl() { return url; }

    }
}
