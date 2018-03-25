package com.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.popularmovies.data.MoviesDbContract.*;

public class MoviesDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "popularmovies.db";
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_ALTER_1 = "ALTER TABLE "
            + MoviesDbEntry.TABLE_NAME + " ADD COLUMN " + MoviesDbEntry.COLUMN_NEW + " TEXT NOT NULL;";

    MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_KEYWORDS_TABLE = "CREATE TABLE " + MoviesDbEntry.TABLE_NAME +
                " (" +
                MoviesDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesDbEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
                MoviesDbEntry.COLUMN_TMDB_ID + " INTEGER NOT NULL, " +
                MoviesDbEntry.COLUMN_VIDEO + " INTEGER NOT NULL, " +
                MoviesDbEntry.COLUMN_VOTE_AVERAGE + " FLOAT NOT NULL, " +
                MoviesDbEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_POPULARITY + " FLOAT NOT NULL, " +
                MoviesDbEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_GENRE_IDS + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_ADULT + " INTEGER NOT NULL, " +
                MoviesDbEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_LIST_TYPE + " TEXT NOT NULL, " +
                MoviesDbEntry.COLUMN_FAVORITE + " INTEGER NOT NULL" +
                "); ";


        sqLiteDatabase.execSQL(SQL_CREATE_KEYWORDS_TABLE);
    }

    @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        //For users who didn't get the latest update to the database columns
        if (oldVersion < 11) {
            sqLiteDatabase.execSQL("ALTER TABLE " + MoviesDbEntry.TABLE_NAME +
                    " ADD COLUMN " + MoviesDbEntry.COLUMN_LIST_TYPE + " TEXT NOT NULL;");
            sqLiteDatabase.execSQL("ALTER TABLE " + MoviesDbEntry.TABLE_NAME +
                    " ADD COLUMN " + MoviesDbEntry.COLUMN_FAVORITE + " INTEGER NOT NULL;");
        }

        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDbEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
