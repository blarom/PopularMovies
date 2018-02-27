package com.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MoviesRecycleViewAdapter.ListItemClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String movieDbAPIkey = "21740cac46494cd78e017c4889c27f05";
    private static final int WEB_SEARCH_LOADER = 101;
    private static final String WEB_SEARCH_LOADER_EXTRA = "web_search_extra";
    MovieList movieList;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mMoviesRecycleView;
    private MoviesRecycleViewAdapter mMoviesRecycleViewAdapter;
    private String mPreferredSortOrder;

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieList = new MovieList();
        mLoadingIndicator = findViewById(R.id.loading_indicator);
        mMoviesRecycleView = findViewById(R.id.movies_recycler_view);

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
                    e.printStackTrace();
                }

                Gson movieDbJGson = new Gson();

                movieList = movieDbJGson.fromJson(movieDbQueryResult, MovieList.class);

                return null;
            }
        };
    }
    @Override public void onLoadFinished(Loader<String> loader, String data) {
        if (movieList != null) {

            showRecycleViewInsteadOfLoadingIndicator();
            mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, movieList);
            mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);

            //List<MovieList.Results> results = movieList.getResults();
            //String path = results.get(0).getPosterPath();
            //updateImage(path);
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
        int numberOfColumns = 2;
        mMoviesRecycleView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        //mMoviesRecycleView.setHasFixedSize(true);
        mMoviesRecycleViewAdapter = new MoviesRecycleViewAdapter(this, this, movieList);
        mMoviesRecycleView.setAdapter(mMoviesRecycleViewAdapter);
        //mMoviesRecycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    public void loadMoviesGrid() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(WEB_SEARCH_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(WEB_SEARCH_LOADER, null, this);
        else loaderManager.restartLoader(WEB_SEARCH_LOADER, null, this);
    }
    @Override public void onListItemClick(int clickedItemIndex) {

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
