package com.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Movies implements Parcelable {

    public Movies() { }

    private Movies(Parcel in) {
        page = in.readInt();
        total_results = in.readInt();
        total_pages = in.readInt();
    }

    public static final Creator<Movies> CREATOR = new Creator<Movies>() {
        @Override
        public Movies createFromParcel(Parcel in) {
            return new Movies(in);
        }

        @Override
        public Movies[] newArray(int size) {
            return new Movies[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(page);
        parcel.writeInt(total_results);
        parcel.writeInt(total_pages);
    }

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
            vote_count = in.readInt();
            id = in.readInt();
            video = in.readByte() != 0;
            vote_average = in.readFloat();
            title = in.readString();
            popularity = in.readFloat();
            poster_path = in.readString();
            original_language = in.readString();
            original_title = in.readString();
            genre_ids = in.createIntArray();
            backdrop_path = in.readString();
            adult = in.readByte() != 0;
            overview = in.readString();
            release_date = in.readString();
            reviews = in.createStringArray();
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
            parcel.writeInt(vote_count);
            parcel.writeInt(id);
            parcel.writeByte((byte) (video ? 1 : 0));
            parcel.writeFloat(vote_average);
            parcel.writeString(title);
            parcel.writeFloat(popularity);
            parcel.writeString(poster_path);
            parcel.writeString(original_language);
            parcel.writeString(original_title);
            parcel.writeIntArray(genre_ids);
            parcel.writeString(backdrop_path);
            parcel.writeByte((byte) (adult ? 1 : 0));
            parcel.writeString(overview);
            parcel.writeString(release_date);
            parcel.writeStringArray(reviews);
        }

        @SerializedName("vote_count")
        private int vote_count;
        public void setVoteCount(int vote_count) { this.vote_count = vote_count; }
        public int getVoteCount() { return vote_count; }

        @SerializedName("id")
        private int id;
        public void setIdNumber(int id) { this.id = id; }
        public int getIdNumber() { return id; }

        @SerializedName("video")
        private boolean video;
        public void setVideo(boolean video) { this.video = video; }
        public boolean getVideo() { return video; }

        @SerializedName("vote_average")
        private float vote_average;
        public void setVoteAverage(float vote_average) { this.vote_average = vote_average; }
        public float getVoteAverage() { return vote_average; }

        @SerializedName("title")
        private String title;
        public void setTitleValue(String title) { this.title = title; }
        public String getTitleValue() { return title; }

        @SerializedName("popularity")
        private float popularity;
        public void setPopularity(float popularity) { this.popularity = popularity; }
        public float getPopularity() { return popularity; }

        @SerializedName("poster_path")
        private String poster_path;
        public void setPosterPath(String poster_path) { this.poster_path = poster_path; }
        public String getPosterPath() { return poster_path; }

        @SerializedName("original_language")
        private String original_language;
        public void setOriginalLanguage(String original_language) { this.original_language = original_language; }
        public String getOriginalLanguage() { return original_language; }

        @SerializedName("original_title")
        private String original_title;
        public void setOriginalTitle(String original_title) { this.original_title = original_title; }
        public String getOriginalTitle() { return original_title; }

        @SerializedName("genre_ids")
        private int[] genre_ids;
        public void setGenreIds(int[] genre_ids) { this.genre_ids = genre_ids; }
        public int[] getGenreIds() { return genre_ids; }

        @SerializedName("backdrop_path")
        private String backdrop_path;
        public void setBackdropPath(String backdrop_path) { this.backdrop_path = backdrop_path; }
        public String getBackdropPath() { return backdrop_path; }

        @SerializedName("adult")
        private boolean adult;
        public void setAdultFlag(boolean adult) { this.adult = adult; }
        public boolean getAdultFlag() { return adult; }

        @SerializedName("overview")
        private String overview;
        public void setOverview(String overview) { this.overview = overview; }
        public String getOverview() { return overview; }

        @SerializedName("release_date")
        private String release_date;
        public void setReleaseDate(String release_date) { this.release_date = release_date; }
        public String getReleaseDate() { return release_date; }

        @SerializedName("reviews")
        private String[] reviews;
        public void setReviews(String release_date) { this.reviews = reviews; }
        public String[] getReviewse() { return reviews; }

    }
}
