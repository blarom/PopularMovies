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
import android.widget.Toast;

import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MoviesRecycleViewAdapter.ListItemClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String movieDbAPIkey = "21740cac46494cd78e017c4889c27f05";
    private static final int WEB_SEARCH_LOADER = 101;
    private static final String SELECTED_MOVIE_TITLE = "selected_movie_tag";
    private static final String SELECTED_POSTER_PATH = "selected_poster_path";
    private static final String SELECTED_PLOT_SYNOPSIS = "selected_plot_synopsis";
    private static final String SELECTED_USER_RATING = "selected_user_rating";
    private static final String SELECTED_RELEASE_DATE = "selected_release_date";
    MovieList mMovieList;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mMoviesRecycleView;
    private MoviesRecycleViewAdapter mMoviesRecycleViewAdapter;
    private String mPreferredSortOrder;
    private SwipeRefreshLayout mSwipeLayout;
    private int mNumberOfColumns;

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieList = new MovieList();
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
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String movieDbQueryResult = "";

                String request = "";
                if (mPreferredSortOrder.equals(getString(R.string.pref_sort_order_popular))) request = getPopularMoviesAPILink();
                else if (mPreferredSortOrder.equals(getString(R.string.pref_sort_order_top_rated))) request = getTopRatedMoviesAPILink();

                try {
                    movieDbQueryResult = getMovieDbJSON(request);
                } catch (IOException e) {
                    Toast.makeText(getContext(),getString(R.string.failedToGetMoviesFromInternet), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                Gson movieDbJGson = new Gson();

                mMovieList = movieDbJGson.fromJson(movieDbQueryResult, MovieList.class);

                if (mMovieList.getResults().size() == 0) Toast.makeText(getContext(),getString(R.string.failedToGetMoviesFromInternet), Toast.LENGTH_SHORT).show();

                return null;
            }
        };
    }
    @Override public void onLoadFinished(Loader<String> loader, String data) {
        if (mMovieList != null) {

            showRecycleViewInsteadOfLoadingIndicator();

            mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, mMovieList);
            mMoviesRecycleViewAdapter.notifyDataSetChanged();

            //mMoviesRecycleView.invalidate();
            //mMoviesRecycleView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));
            mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);
            mMoviesRecycleView.getLayoutManager().scrollToPosition(0);

            //TODO Tried all kinds of things, can't fix issue of recyclerview loading at the 8th image on second refresh and then no showing at all on 3rd refresh

            mSwipeLayout.setRefreshing(false);
        }

    }
    @Override public void onLoaderReset(Loader<String> loader) {

    }
    private String getMovieDbJSON(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (response.body() != null) {
            return response.body().string();
        }
        else return "";
    }
    private String getPopularMoviesAPILink() {
        return "https://api.themoviedb.org/3/movie/popular?page=1&language=en-US&api_key=" + movieDbAPIkey;
    }
    private String getTopRatedMoviesAPILink() {
        return "https://api.themoviedb.org/3/movie/top_rated?page=1&language=en-US&api_key=" + movieDbAPIkey;
    }

    //RecyclerView methods
    public void setupGridRecyclerView() {
        mNumberOfColumns = 2;
        mMoviesRecycleView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));
        //mMoviesRecycleView.setHasFixedSize(true);
        mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, mMovieList);
        mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);
        //mMoviesRecycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    public void loadMoviesGrid() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(WEB_SEARCH_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(WEB_SEARCH_LOADER, null, this);
        else loaderManager.restartLoader(WEB_SEARCH_LOADER, null, this);
    }
    @Override public void onListItemClick(MovieList.Results selectedResults) {

        String movieTitle = selectedResults.getTitleValue();
        String posterPath = selectedResults.getPosterPath();
        String plotSynopsis = selectedResults.getOverview();
        float userRating = selectedResults.getVoteAverage();
        String releaseDate = selectedResults.getReleaseDate();

        Bundle movieBundle = new Bundle();
        movieBundle.putString(SELECTED_MOVIE_TITLE, movieTitle);
        movieBundle.putString(SELECTED_POSTER_PATH, posterPath);
        movieBundle.putString(SELECTED_PLOT_SYNOPSIS, plotSynopsis);
        movieBundle.putFloat(SELECTED_USER_RATING, userRating);
        movieBundle.putString(SELECTED_RELEASE_DATE, releaseDate);

        Intent startDetailsActivity = new Intent(this, DetailActivity.class);
        startDetailsActivity.putExtras(movieBundle);
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
