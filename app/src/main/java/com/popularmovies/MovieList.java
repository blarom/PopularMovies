package com.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieList {

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

    class Results {

        public Results() {}

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

    }
}
