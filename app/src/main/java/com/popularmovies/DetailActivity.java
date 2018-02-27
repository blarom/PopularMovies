package com.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private static final String SELECTED_MOVIE_TITLE = "selected_movie_tag";
    private static final String SELECTED_POSTER_PATH = "selected_poster_path";
    private static final String SELECTED_PLOT_SYNOPSIS = "selected_plot_synopsis";
    private static final String SELECTED_USER_RATING = "selected_user_rating";
    private static final String SELECTED_RELEASE_DATE = "selected_release_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;

        String movieTitle = bundle.getString(SELECTED_MOVIE_TITLE, "");
        String posterPath = bundle.getString(SELECTED_POSTER_PATH, "");
        String plotSynopsis = bundle.getString(SELECTED_PLOT_SYNOPSIS, "");
        float userRating = bundle.getFloat(SELECTED_USER_RATING, 0);
        String releaseDate = bundle.getString(SELECTED_RELEASE_DATE, "");


        //Getting the views
        ImageView imageView = findViewById(R.id.imageView);

        //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
        String size = "w185";
        Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/" + size + "/" + posterPath).into(imageView);

    }
}
