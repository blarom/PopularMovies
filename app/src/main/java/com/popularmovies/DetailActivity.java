package com.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private static final String MOVIE_RESULTS_PARCEL = "movie_results_parcel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        MovieList.Results selectedResults = getIntent().getParcelableExtra(MOVIE_RESULTS_PARCEL);
        String movieTitle = selectedResults.getTitleValue();
        String posterPath = selectedResults.getPosterPath();
        String plotSynopsis = selectedResults.getOverview();
        float userRating = selectedResults.getVoteAverage();
        String releaseDate = selectedResults.getReleaseDate();


        /*
        //Wrote this code before it was suggested to implement Parcelable, which is a much more elegant solution :)

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;

        String movieTitle = bundle.getString(SELECTED_MOVIE_TITLE, "");
        String posterPath = bundle.getString(SELECTED_POSTER_PATH, "");
        String plotSynopsis = bundle.getString(SELECTED_PLOT_SYNOPSIS, "");
        float userRating = bundle.getFloat(SELECTED_USER_RATING, 0);
        String releaseDate = bundle.getString(SELECTED_RELEASE_DATE, "");
        */

        //Getting the views
        ImageView imageView = findViewById(R.id.imageView);

        //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
        String size = "w185";
        Picasso.with(getApplicationContext())
                .load("http://image.tmdb.org/t/p/" + size + "/" + posterPath)
                .resize(10000, 800)
                .centerInside()
                .into(imageView);

        ((TextView) findViewById(R.id.title_value)).setText(movieTitle);
        ((TextView) findViewById(R.id.synopsis_value)).setText(plotSynopsis);
        ((TextView) findViewById(R.id.rating_value)).setText(Float.toString(userRating));
        ((TextView) findViewById(R.id.release_date_value)).setText(releaseDate);

    }
}
