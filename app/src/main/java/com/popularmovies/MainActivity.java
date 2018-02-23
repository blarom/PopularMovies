package com.popularmovies;

import android.annotation.SuppressLint;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {


    private static final String movieDbAPIkey = "21740cac46494cd78e017c4889c27f05";
    private static final int WEB_SEARCH_LOADER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(WEB_SEARCH_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(WEB_SEARCH_LOADER, null, this);
        else loaderManager.restartLoader(WEB_SEARCH_LOADER, null, this);

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
    public void updateImages() {

        ImageView imageView = findViewById(R.id.imageView);
        Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg").into(imageView);
    }

    //Http request asynctask loader
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(getApplicationContext()) {

            @Override
            protected void onStartLoading() {
                //if (args == null) return;
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String movieDbQueryResult = "";

                try {
                    movieDbQueryResult = getMovieDbJSON("https://api.themoviedb.org/3/configuration?api_key=" + movieDbAPIkey);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject movieDbJson = new JSONObject(movieDbQueryResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //TODO: get movies database

                
                return null;
            }
        };
    }
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

    }
    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
