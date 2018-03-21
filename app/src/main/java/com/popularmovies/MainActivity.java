package com.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MoviesRecycleViewAdapter.ListItemClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MOVIE_LIST_LOADER = 101;
    private static final String MOVIE_RESULTS_PARCEL = "movie_results_parcel";
    MovieList mMovieList;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mMoviesRecycleView;
    private MoviesRecycleViewAdapter mMoviesRecycleViewAdapter;
    private String mPreferredSortOrder;
    private SwipeRefreshLayout mSwipeLayout;
    private int mNumberOfColumns;
    private List<MovieList.Results> mMovieResultsListAfterUpdate;

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieList = new MovieList();
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

        setupGridRecyclerView();
        setupSharedPreferences();
        loadMoviesGrid();
    }
    @Override protected void onResume() {
        super.onResume();
        loadMoviesGrid();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
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

                    mMovieList = movieDbGson.fromJson(movieDbQueryResult, MovieList.class);

                }

                return null;
            }
        };
    }
    @Override public void onLoadFinished(Loader<String> loader, String data) {
        if (mMovieList != null) {

            showRecycleViewInsteadOfLoadingIndicator();

            if (mMovieList.getResults().size() == 0) NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());

            mMovieResultsListAfterUpdate.clear();
            mMovieResultsListAfterUpdate.addAll(mMovieList.getResults());
            mMoviesRecycleViewAdapter.notifyDataSetChanged();

            //mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, mMovieList);
            //mMoviesRecycleView.getLayoutManager().scrollToPosition(0);

            mSwipeLayout.setRefreshing(false);
        }

    }
    @Override public void onLoaderReset(Loader<String> loader) {

    }

    //RecyclerView methods
    public void setupGridRecyclerView() {
        mNumberOfColumns = 2;
        mMoviesRecycleView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));
        //mMoviesRecycleView.setHasFixedSize(true);
        mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, mMovieResultsListAfterUpdate);
        mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);
        //mMoviesRecycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    public void loadMoviesGrid() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(MOVIE_LIST_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(MOVIE_LIST_LOADER, null, this);
        else loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);
    }
    @Override public void onListItemClick(MovieList.Results selectedResults) {

        Intent startDetailsActivity = new Intent(this, DetailActivity.class);
        startDetailsActivity.putExtra(MOVIE_RESULTS_PARCEL, selectedResults);
        startActivity(startDetailsActivity);
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
        setPreferredSortOrder(sharedPreferences);
        showLoadingIndicatorInsteadOfRecycleView();
        loadMoviesGrid();
    }
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setPreferredSortOrder(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    private void setPreferredSortOrder(SharedPreferences sharedPreferences) {
        mPreferredSortOrder = sharedPreferences.getString(getString(R.string.pref_preferred_sort_order_key), getString(R.string.pref_sort_order_popular));
    }
}
