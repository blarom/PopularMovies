package com.popularmovies;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.popularmovies.data.Movies;
import com.popularmovies.data.Reviews;
import com.popularmovies.data.Videos;
import com.popularmovies.utilities.NetworkUtilities;
import com.popularmovies.utilities.TheMovieDbUtilities;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final String MOVIE_RESULTS_PARCEL = "movie_results_parcel";
    private static final int MOVIE_DETAILS_LOADER = 201;
    private Videos mTrailers;
    private Reviews mReviews;
    private Movies.Results mSelectedResult;
    private boolean mLoadedTrailers;

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mLoadedTrailers = false;

        mSelectedResult = getIntent().getParcelableExtra(MOVIE_RESULTS_PARCEL);
        String movieTitle = mSelectedResult.getTitleValue();
        final String posterPath = mSelectedResult.getPosterPath();
        String plotSynopsis = mSelectedResult.getOverview();
        float userRating = mSelectedResult.getVoteAverage();
        String releaseDate = mSelectedResult.getReleaseDate();


        //Getting the views
        final ImageView imageView = findViewById(R.id.imageView);

        //Load the image after the layout is inflated, so that the true imageView width can be measured
        imageView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure we call this only once
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int imageViewWidth = imageView.getWidth();

                //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
                String size = "w185";
                Picasso.with(getApplicationContext())
                        .load("http://image.tmdb.org/t/p/" + size + "/" + posterPath)
                        .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        //Inspired from: https://stackoverflow.com/questions/34067472/update-recyclerview-items-height-after-image-was-loaded
                        ;//After the image is loaded, get its drawable width & height, then set the imageView height accordingly
                        Drawable drawable = imageView.getDrawable();
                        int drawableWidth = drawable.getIntrinsicWidth();
                        int drawableHeight = drawable.getIntrinsicHeight();

                        int imageViewHeight = (int) (((float) drawableHeight) / ((float) drawableWidth) * ((float) imageViewWidth));
                        imageView.setMinimumHeight(imageViewHeight);
                        imageView.setMaxHeight(imageViewHeight);
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        });

        ((TextView) findViewById(R.id.title_value)).setText(movieTitle);
        ((TextView) findViewById(R.id.synopsis_value)).setText(plotSynopsis);
        ((TextView) findViewById(R.id.rating_value)).setText(Float.toString(userRating));
        ((TextView) findViewById(R.id.release_date_value)).setText(releaseDate);

        loadMovieReviewsAndTrailers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLoadedTrailers) hideLoadingIndicator();
    }

    public void loadMovieReviewsAndTrailers() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(MOVIE_DETAILS_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(MOVIE_DETAILS_LOADER, null, this);
        else loaderManager.restartLoader(MOVIE_DETAILS_LOADER, null, this);
    }

    //Http request asynctask loader
    @SuppressLint("StaticFieldLeak") @Override public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getApplicationContext()) {
            @Override protected void onStartLoading() {
                //if (args == null) return;
                showLoadingIndicator();
                mLoadedTrailers = false;
                if (!NetworkUtilities.isInternetAvailable(getContext())) NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());
                forceLoad();
            }
            @Override public String loadInBackground() {
                String trailersQueryResult = "";
                String reviewsQueryResult = "";

                if (NetworkUtilities.isInternetAvailable(getContext())) {
                    String trailersRequestUrl = TheMovieDbUtilities.getTrailersAPILink(mSelectedResult.getIdNumber());
                    String reviewsRequestUrl = TheMovieDbUtilities.getReviewsAPILink(mSelectedResult.getIdNumber());

                    try {
                        trailersQueryResult = NetworkUtilities.getJson(trailersRequestUrl);
                        reviewsQueryResult = NetworkUtilities.getJson(reviewsRequestUrl);
                    } catch (IOException e) {
                        NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());
                        e.printStackTrace();
                    }

                    Gson trailersGson = new Gson();
                    Gson reviewsGson = new Gson();

                    mTrailers = trailersGson.fromJson(trailersQueryResult, Videos.class);
                    mReviews = reviewsGson.fromJson(reviewsQueryResult, Reviews.class);

                }

                return null;
            }
        };
    }
    @Override public void onLoadFinished(Loader<String> loader, String data) {

        hideLoadingIndicator();
        mLoadedTrailers = true;

        //Create the Trailers layout
        if (mTrailers != null) {
            RecyclerView trailersContainer = findViewById(R.id.trailers_container);
            final List<Videos.Results> trailers = mTrailers.getResults();

            int numberOfColumns;
            if (trailers.size() == 0) {
                numberOfColumns = 1;
                String text = getResources().getString(R.string.no_trailers);
                trailersContainer.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
                TrailersRecycleViewAdapter mTrailersRecycleViewAdapter = new TrailersRecycleViewAdapter(this, trailers, text);
                trailersContainer.setAdapter(mTrailersRecycleViewAdapter);
            }
            else {
                numberOfColumns = 3;

                //Set the height of the Trailers GridLayout
                int rowHeightPixels = (int) getResources().getDimension(R.dimen.trailer_list_item_height);
                int remainder = trailers.size();
                int numberOfRows = 0;
                while (remainder > 0) {
                    remainder = remainder - numberOfColumns;
                    numberOfRows++;
                }
                int totalHeightPixels = numberOfRows * rowHeightPixels;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, totalHeightPixels);
                trailersContainer.setLayoutParams(params);

                //Set the adapter
                trailersContainer.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
                TrailersRecycleViewAdapter mTrailersRecycleViewAdapter = new TrailersRecycleViewAdapter(this, trailers, "");
                trailersContainer.setAdapter(mTrailersRecycleViewAdapter);
            }
        }

        //Create the Reviews layout
        if (mReviews != null) {
            LinearLayout trailersContainer = findViewById(R.id.reviews_container);
            trailersContainer.removeAllViews();
            List<Reviews.Results> reviews = mReviews.getResults();
            if (reviews.size() == 0) {
                TextView noReviews = new TextView(getApplicationContext());
                noReviews.setText(getResources().getString(R.string.no_reviews));
                trailersContainer.addView(noReviews);
            }

            LinearLayout.LayoutParams delimiterParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);

            for (int i=0; i<reviews.size(); i++) {
                TextView review = new TextView(getApplicationContext());
                review.setText(reviews.get(i).getContent());
                review.setPadding(16,16,16,16);
                trailersContainer.addView(review);

                TextView author = new TextView(getApplicationContext());
                author.setText(reviews.get(i).getAuthor());
                author.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                author.setPadding(16,16,16,64);
                trailersContainer.addView(author);

                if (i<reviews.size()-1) {
                    View delimiter = new View(getApplicationContext());
                    delimiter.setLayoutParams(delimiterParams);
                    delimiter.setBackgroundColor(Color.DKGRAY);
                    trailersContainer.addView(delimiter);
                }
            }
        }
    }
    @Override public void onLoaderReset(Loader<String> loader) {
    }

    private void showLoadingIndicator() {
        findViewById(R.id.trailers_loading_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.dynamic_layouts_container).setVisibility(View.GONE);
    }
    private void hideLoadingIndicator() {
        findViewById(R.id.trailers_loading_indicator).setVisibility(View.GONE);
        findViewById(R.id.dynamic_layouts_container).setVisibility(View.VISIBLE);
    }
}
