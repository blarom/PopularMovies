package com.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.popularmovies.data.MoviesDbContract.*;

public class MoviesDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "popularmovies.db";
    public static final int DATABASE_VERSION = 1;
    private Context mContext;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_KEYWORDS_TABLE = "CREATE TABLE " + MoviesDbEntry.TABLE_NAME +
                " (" +
                MoviesDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesDbEntry.COLUMN_VOTE_COUNT + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_TMDB_ID + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_VIDEO + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_POPULARITY + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_GENRE_IDS + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_ADULT + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL" +
                "); ";


        sqLiteDatabase.execSQL(SQL_CREATE_KEYWORDS_TABLE);
    }

    @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDbEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    // CRUD (Create, Read, Update, Delete) Operations
    public void addMovie(MovieList.Results movie) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MoviesDbEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
        values.put(MoviesDbEntry.COLUMN_TMDB_ID, movie.getIdNumber());
        values.put(MoviesDbEntry.COLUMN_VIDEO, movie.getVideo());
        values.put(MoviesDbEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MoviesDbEntry.COLUMN_TITLE, movie.getTitleValue());
        values.put(MoviesDbEntry.COLUMN_POPULARITY, movie.getPopularity());
        values.put(MoviesDbEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MoviesDbEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
        String genreIdsString = "";
        for (int i=0; i<movie.getGenreIds().length; i++) {
            genreIdsString += movie.getGenreIds()[i];
            if (i<movie.getGenreIds().length-1) genreIdsString += ";";
        }
        values.put(MoviesDbEntry.COLUMN_GENRE_IDS, genreIdsString);
        values.put(MoviesDbEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        values.put(MoviesDbEntry.COLUMN_ADULT, movie.getAdultFlag());
        values.put(MoviesDbEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MoviesDbEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());

        // Inserting Row
        db.insert(MoviesDbEntry.TABLE_NAME, null, values);
        db.close();
    }
    public Cursor getAllMovieEntries() {
        SQLiteDatabase db = this.getWritableDatabase();

        String tableName = MoviesDbEntry.TABLE_NAME;
        String orderBy = MoviesDbEntry.COLUMN_VOTE_AVERAGE;

        return db.query(tableName, null, null, null, null, null, orderBy);
    }
    public boolean removeMovieEntry(long id, SQLiteDatabase sqLiteDatabase) {
        String whereClause = MoviesDbEntry._ID + "=" + id;
        boolean successfullyRemoved = sqLiteDatabase.delete(MoviesDbEntry.TABLE_NAME, whereClause, null) > 0;
        return successfullyRemoved;
    }
}
