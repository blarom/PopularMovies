package com.popularmovies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.popularmovies.data.*;
import com.popularmovies.utilities.NetworkUtilities;
import com.popularmovies.utilities.TheMovieDbUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MoviesRecycleViewAdapter.ListItemClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MOVIE_LIST_LOADER = 101;
    private static final String MOVIES_CONTENT_PROVIDER_INDEX = "movies_content_provider_index";
    Movies mMovies;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mMoviesRecycleView;
    private MoviesRecycleViewAdapter mMoviesRecycleViewAdapter;
    private String mPreferredSortOrder;
    private Boolean mPreferredFavoritesShown;
    private Cursor mListShownToUserCursor;
    private SwipeRefreshLayout mSwipeLayout;
    private int mNumberOfColumns;
    private List<Movies.Results> mMovieResultsListAfterUpdate;

    public static final String[] MOVIES_TABLE_ELEMENTS = {
        MoviesDbContract.MoviesDbEntry._ID,
        MoviesDbContract.MoviesDbEntry.COLUMN_VOTE_COUNT,
        MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID,
        MoviesDbContract.MoviesDbEntry.COLUMN_VIDEO,
        MoviesDbContract.MoviesDbEntry.COLUMN_VOTE_AVERAGE,
        MoviesDbContract.MoviesDbEntry.COLUMN_TITLE,
        MoviesDbContract.MoviesDbEntry.COLUMN_POPULARITY,
        MoviesDbContract.MoviesDbEntry.COLUMN_POSTER_PATH,
        MoviesDbContract.MoviesDbEntry.COLUMN_ORIGINAL_LANGUAGE,
        MoviesDbContract.MoviesDbEntry.COLUMN_GENRE_IDS,
        MoviesDbContract.MoviesDbEntry.COLUMN_BACKDROP_PATH,
        MoviesDbContract.MoviesDbEntry.COLUMN_ADULT,
        MoviesDbContract.MoviesDbEntry.COLUMN_OVERVIEW,
        MoviesDbContract.MoviesDbEntry.COLUMN_RELEASE_DATE,
        MoviesDbContract.MoviesDbEntry.COLUMN_FAVORITE
    };

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovies = new Movies();
        mMovieResultsListAfterUpdate = new ArrayList<>();
        mLoadingIndicator = findViewById(R.id.loading_indicator);
        mMoviesRecycleView = findViewById(R.id.movies_recycler_view);
        mSwipeLayout = findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMoviesGrid();
            }
        });

        showLoadingIndicatorInsteadOfRecycleView();

        setupSharedPreferences();
        setupGridRecyclerView();
        loadMoviesGrid();
    }
    @Override protected void onResume() {
        super.onResume();
        loadMoviesGrid();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mListShownToUserCursor!=null) mListShownToUserCursor.close();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    //Options Menu methods
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Http request asynctask loader
    @SuppressLint("StaticFieldLeak") @Override public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(getApplicationContext()) {

            @Override
            protected void onStartLoading() {
                //if (args == null) return;
                showLoadingIndicatorInsteadOfRecycleView();
                if (!NetworkUtilities.isInternetAvailable(getContext())) NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String movieDbQueryResult = "";

                if (NetworkUtilities.isInternetAvailable(getContext())) {
                    String requestUrl = "";
                    if (mPreferredSortOrder.equals(getString(R.string.pref_sort_order_popular)))
                        requestUrl = TheMovieDbUtilities.getPopularMoviesAPILink();
                    else if (mPreferredSortOrder.equals(getString(R.string.pref_sort_order_top_rated)))
                        requestUrl = TheMovieDbUtilities.getTopRatedMoviesAPILink();

                    try {
                        movieDbQueryResult = NetworkUtilities.getJson(requestUrl);
                    } catch (IOException e) {
                        NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());
                        e.printStackTrace();
                    }

                    Gson movieDbGson = new Gson();

                    mMovies = movieDbGson.fromJson(movieDbQueryResult, Movies.class);

                }

                return null;
            }
        };
    }
    @Override public void onLoadFinished(Loader<String> loader, String data) {
        if (mMovies != null) {

            showRecycleViewInsteadOfLoadingIndicator();

            if (mMovies.getResults().size() == 0) NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());

            mMovieResultsListAfterUpdate.clear();
            mMovieResultsListAfterUpdate.addAll(mMovies.getResults());

            updateLocalDatabaseWithResults();
            mListShownToUserCursor = getListShownToUser();
            mMoviesRecycleViewAdapter.swapCursor(mListShownToUserCursor);
            mMoviesRecycleViewAdapter.notifyDataSetChanged();

            mSwipeLayout.setRefreshing(false);
        }

    }
    @Override public void onLoaderReset(Loader<String> loader) {

    }
    private void updateLocalDatabaseWithResults() {

        for (int i=0; i<mMovieResultsListAfterUpdate.size(); i++) {

            //Query for movie in local database based on Internet results movie id
            int tmdbId = mMovieResultsListAfterUpdate.get(i).getIdNumber();
            Cursor cursorMovies = getContentResolver().query(
                    MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                    MainActivity.MOVIES_TABLE_ELEMENTS,
                    MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID +" = ?",
                    new String[]{Integer.toString(tmdbId)},
                    null);

            /*
            Used for debugging
            Log.w("PopularMoviesDebug", DatabaseUtils.dumpCursorToString(cursorMovies));
            */

            //If the movie exists in the local database, update its values, otherwise create a new entry
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_VOTE_COUNT, mMovieResultsListAfterUpdate.get(i).getVoteCount());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID, tmdbId);
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_VIDEO, mMovieResultsListAfterUpdate.get(i).getVideo());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_VOTE_AVERAGE, mMovieResultsListAfterUpdate.get(i).getVoteAverage());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_TITLE, mMovieResultsListAfterUpdate.get(i).getTitleValue());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_POPULARITY, mMovieResultsListAfterUpdate.get(i).getPopularity());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_POSTER_PATH, mMovieResultsListAfterUpdate.get(i).getPosterPath());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_ORIGINAL_LANGUAGE, mMovieResultsListAfterUpdate.get(i).getOriginalLanguage());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_GENRE_IDS, Arrays.toString(mMovieResultsListAfterUpdate.get(i).getGenreIds()));
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_BACKDROP_PATH, mMovieResultsListAfterUpdate.get(i).getBackdropPath());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_ADULT, mMovieResultsListAfterUpdate.get(i).getAdultFlag());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_OVERVIEW, mMovieResultsListAfterUpdate.get(i).getOverview());
            contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_RELEASE_DATE, mMovieResultsListAfterUpdate.get(i).getReleaseDate());

            if (cursorMovies == null || cursorMovies.getCount() < 1) {
                contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_FAVORITE, 0);
                Uri uri = getContentResolver().insert(
                        MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                        contentValues);
            } else {
                int updatedRows = getContentResolver().update(
                        MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                        contentValues,
                        MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID + " = ?",
                        new String[]{Integer.toString(tmdbId)});
            }

            //Finally, close the cursor
            if (cursorMovies != null) cursorMovies.close();
        }
    }
    private Cursor getListShownToUser() {

        Cursor cursor;
        if (mPreferredFavoritesShown) {
            //Query for movies in local database that have favorite = true
            cursor = getContentResolver().query(
                    MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                    MainActivity.MOVIES_TABLE_ELEMENTS,
                    MoviesDbContract.MoviesDbEntry.COLUMN_FAVORITE + " = 1",
                    null,
                    null);

        }
        else {
            //Get all the movies in the local database
            cursor = getContentResolver().query(
                    MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                    MainActivity.MOVIES_TABLE_ELEMENTS,
                    null,
                    null,
                    null);
        }

        /*Used for cursor debugging
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) Log.w("PopularMoviesDebug", DatabaseUtils.dumpCursorToString(cursor));
            }
            cursor.moveToFirst();
        }
        */

        return cursor;
    }

    //RecyclerView methods
    public void setupGridRecyclerView() {
        mNumberOfColumns = 2;
        mMoviesRecycleView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));
        mListShownToUserCursor =  getListShownToUser();
        mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this);
        mMoviesRecycleViewAdapter.swapCursor(mListShownToUserCursor);
        mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);
    }
    public void loadMoviesGrid() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(MOVIE_LIST_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(MOVIE_LIST_LOADER, null, this);
        else loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);
    }
    @Override public void onListItemClick(int clickedItemIndex) {

        if (mListShownToUserCursor != null) {
            if (!mListShownToUserCursor.moveToPosition(clickedItemIndex)) return;

            int movieIdFromCursor = mListShownToUserCursor.getInt(mListShownToUserCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID));

            Intent startDetailsActivity = new Intent(this, DetailActivity.class);
            startDetailsActivity.putExtra(MOVIES_CONTENT_PROVIDER_INDEX, movieIdFromCursor);
            startActivity(startDetailsActivity);
        }
    }
    private void showRecycleViewInsteadOfLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mMoviesRecycleView.setVisibility(View.VISIBLE);
    }
    private void showLoadingIndicatorInsteadOfRecycleView() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mMoviesRecycleView.setVisibility(View.INVISIBLE);
    }

    //Preferences methods
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setPreferredSortOrder(sharedPreferences, getString(R.string.pref_sort_order_popular));
        setShowFavoritesPreference(sharedPreferences, getResources().getBoolean(R.bool.pref_show_favorites_default));
        showLoadingIndicatorInsteadOfRecycleView();
        loadMoviesGrid();
    }
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setPreferredSortOrder(sharedPreferences, getString(R.string.pref_sort_order_popular));
        setShowFavoritesPreference(sharedPreferences, getResources().getBoolean(R.bool.pref_show_favorites_default));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    private void setPreferredSortOrder(SharedPreferences sharedPreferences, String defaultValue) {
        mPreferredSortOrder = sharedPreferences.getString(getString(R.string.pref_preferred_sort_order_key), defaultValue);
    }
    private void setShowFavoritesPreference(SharedPreferences sharedPreferences, Boolean defaultValue) {
        mPreferredFavoritesShown = sharedPreferences.getBoolean(getString(R.string.pref_show_favorites_key), defaultValue);
    }
}
