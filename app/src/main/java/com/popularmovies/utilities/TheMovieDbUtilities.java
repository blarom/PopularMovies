package com.popularmovies.utilities;


import com.popularmovies.BuildConfig;

public class TheMovieDbUtilities {
    private static final String movieDbAPIkey = BuildConfig.API_KEY;

    public static String getTrailersAPILink(int idNumber) {
        return "https://api.themoviedb.org/3/movie/"
                + idNumber
                + "/videos?api_key="
                + movieDbAPIkey
                + "&language=en-US";
    }
    public static String getReviewsAPILink(int idNumber) {
        return "https://api.themoviedb.org/3/movie/"
                + idNumber
                + "/reviews?api_key="
                + movieDbAPIkey
                + "&language=en-US";
    }
    public static String getPopularMoviesAPILink() {
        return "https://api.themoviedb.org/3/movie/popular?page=1&language=en-US&api_key=" + movieDbAPIkey;
    }
    public static String getTopRatedMoviesAPILink() {
        return "https://api.themoviedb.org/3/movie/top_rated?page=1&language=en-US&api_key=" + movieDbAPIkey;
    }
}
