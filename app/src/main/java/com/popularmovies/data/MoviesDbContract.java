package com.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesDbContract {

    public static final String AUTHORITY = "com.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    public static final class MoviesDbEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "moviesTable";
        public static final String COLUMN_VOTE_COUNT = "voteCount";
        public static final String COLUMN_TMDB_ID = "tmdbId";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_POSTER_PATH = "posterPath";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "originalLanguage";
        public static final String COLUMN_GENRE_IDS = "genreIds";
        public static final String COLUMN_BACKDROP_PATH = "backdropPath";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";


    }

}
