package com.popularmovies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.popularmovies.data.MoviesDbContract;
import com.popularmovies.data.Reviews;
import com.popularmovies.data.Videos;
import com.popularmovies.utilities.NetworkUtilities;
import com.popularmovies.utilities.TheMovieDbUtilities;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    //@BindView(R.id.title_value) TextView titleValueTv;
    @BindView(R.id.synopsis_value) TextView synopsisValueTv;
    @BindView(R.id.rating_value) TextView ratingValueTv;
    @BindView(R.id.release_date_value) TextView releaseDateValueTv;
    @BindView(R.id.imageView) ImageView posterIv;
    @BindView(R.id.favorite_image) ImageView favoriteIv;
    @BindView(R.id.dynamic_layouts_container) LinearLayout dynamicLayoutsContainerLl;
    @BindView(R.id.trailers_loading_indicator) ProgressBar trailersLoadingIndicatorPb;
    @BindView(R.id.trailers_container) RecyclerView trailersContainerRv;
    @BindView(R.id.container_scrollview) NestedScrollView containerSv;

    private static final String MOVIES_CONTENT_PROVIDER_INDEX = "movies_content_provider_index";
    private static final String MOVIES_RECYCLERVIEW_POSITION = "movies_recyclerview_position";
    private static final int MOVIE_DETAILS_LOADER = 201;
    private static final String SCROLL_POSITION = "scroll_position";
    private Videos mTrailers;
    private Reviews mReviews;
    private boolean mLoadedTrailers;
    private String mMovieTitle;
    private String mPosterPath;
    private String mPlotSynopsis;
    private int mUserRating;
    private String mReleaseDate;
    private int mFavorite;
    private Cursor mMovieCursor;
    private int mMovieId;
    private int mMoviesListPosition;
    private int mScrollPosition;

    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        retrieveValuesForLayout();
        setupCollapsingActionBarProperties();
        loadValuesIntoLayout();
        loadMovieReviewsAndTrailers();
    }
    @Override protected void onResume() {
        super.onResume();
        if (mLoadedTrailers) hideLoadingIndicator();
    }
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCROLL_POSITION, containerSv.getScrollY());
        outState.putInt(MOVIES_RECYCLERVIEW_POSITION, mMoviesListPosition);
    }
    @Override public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mScrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
            containerSv.setScrollY(mScrollPosition);
            mMoviesListPosition = savedInstanceState.getInt(MOVIES_RECYCLERVIEW_POSITION);
        }
    }
    @Override public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra(MOVIES_RECYCLERVIEW_POSITION, mMoviesListPosition);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    private void setupCollapsingActionBarProperties() {
        //Note: new settings in styles.xml/windowActionBar & windowNoTitle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mMovieTitle!=null) actionBar.setTitle(mMovieTitle);
        }

    }
    public void loadMovieReviewsAndTrailers() {

        mLoadedTrailers = false;
        hideLoadingIndicator();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> WebSearchLoader = loaderManager.getLoader(MOVIE_DETAILS_LOADER);
        if (WebSearchLoader == null) loaderManager.initLoader(MOVIE_DETAILS_LOADER, null, this);
        else loaderManager.restartLoader(MOVIE_DETAILS_LOADER, null, this);
    }
    private void retrieveValuesForLayout() {

        //Getting the values from MainActivity
        Intent intentThatStartedThisActivity = getIntent();
        int tmdbId = 0;
        if (intentThatStartedThisActivity.hasExtra(MOVIES_CONTENT_PROVIDER_INDEX)) {
            tmdbId = intentThatStartedThisActivity.getIntExtra(MOVIES_CONTENT_PROVIDER_INDEX,0);
        }
        if (intentThatStartedThisActivity.hasExtra(MOVIES_RECYCLERVIEW_POSITION)) {
            mMoviesListPosition = intentThatStartedThisActivity.getIntExtra(MOVIES_RECYCLERVIEW_POSITION,0);
        }

        //Retrieving the values from the Keywords database
        mMovieCursor = getContentResolver().query(
                MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                MainActivity.MOVIES_TABLE_ELEMENTS,
                MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID +" = ?",
                new String[]{Integer.toString(tmdbId)},
                null);

        mMovieId = 0;
        mMovieTitle = "";
        mPosterPath = "";
        mPlotSynopsis ="";
        mUserRating = 0;
        mReleaseDate = "";
        mFavorite = 0;

        if (mMovieCursor != null) {
            if (mMovieCursor.moveToFirst()){
                mMovieId = mMovieCursor.getInt(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID));
                mMovieTitle = mMovieCursor.getString(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_TITLE));
                mPosterPath = mMovieCursor.getString(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_POSTER_PATH));
                mPlotSynopsis = mMovieCursor.getString(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_OVERVIEW));
                mUserRating = mMovieCursor.getInt(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_VOTE_AVERAGE));
                mReleaseDate = mMovieCursor.getString(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_RELEASE_DATE));
                mFavorite = mMovieCursor.getInt(mMovieCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_FAVORITE));
            }
            mMovieCursor.close();
        }
    }
    private void loadValuesIntoLayout() {

        //Load the image after the layout is inflated, so that the true posterIv width can be measured
        posterIv.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Ensure we call this only once
                        posterIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        final int imageViewWidth = posterIv.getWidth();

                        //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
                        String size = "w185";
                        Picasso.with(getApplicationContext())
                                .load("http://image.tmdb.org/t/p/" + size + "/" + mPosterPath)
                                .error(R.drawable.ic_missing_image)
                                .into(posterIv, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        //Inspired from: https://stackoverflow.com/questions/34067472/update-recyclerview-items-height-after-image-was-loaded
                                        ;//After the image is loaded, get its drawable width & height, then set the posterIv height accordingly
                                        Drawable drawable = posterIv.getDrawable();
                                        int drawableWidth = drawable.getIntrinsicWidth();
                                        int drawableHeight = drawable.getIntrinsicHeight();

                                        int imageViewHeight = (int) (((float) drawableHeight) / ((float) drawableWidth) * ((float) imageViewWidth));
                                        posterIv.setMinimumHeight(imageViewHeight);
                                        posterIv.setMaxHeight(imageViewHeight);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
                });

        //titleValueTv.setText(mMovieTitle);
        synopsisValueTv.setText(mPlotSynopsis);
        ratingValueTv.setText(Float.toString(mUserRating));
        releaseDateValueTv.setText(mReleaseDate);

        if (mFavorite == 1) {
            favoriteIv.setImageResource(R.drawable.ic_star_black_24dp);
        } else {
            favoriteIv.setImageResource(R.drawable.ic_star_border_black_24dp);
        }
        favoriteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Switch the favorite display when clicked
                if (mFavorite == 1) {
                    mFavorite = 0;
                    favoriteIv.setImageResource(R.drawable.ic_star_border_black_24dp);

                } else {
                    mFavorite = 1;
                    favoriteIv.setImageResource(R.drawable.ic_star_black_24dp);
                }

                //Update the database entry with the favorite value
                if (mMovieCursor != null && mMovieCursor.getCount() > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesDbContract.MoviesDbEntry.COLUMN_FAVORITE, mFavorite);
                    int updatedRows = getContentResolver().update(
                            MoviesDbContract.MoviesDbEntry.CONTENT_URI,
                            contentValues,
                            MoviesDbContract.MoviesDbEntry.COLUMN_TMDB_ID + " = ?",
                            new String[]{Integer.toString(mMovieId)});
                }
            }
        });

    }

    //Http request asynctask loader
    @SuppressLint("StaticFieldLeak") @Override public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getApplicationContext()) {
            @Override protected void onStartLoading() {
                //if (args == null) return;
                if (!mLoadedTrailers) {
                    showLoadingIndicator();
                    mLoadedTrailers = false;
                    if (!NetworkUtilities.isInternetAvailable(getContext()))
                        NetworkUtilities.tellUserInternetIsUnavailable(getApplicationContext());
                    forceLoad();
                }
            }
            @Override public String loadInBackground() {
                String trailersQueryResult = "";
                String reviewsQueryResult = "";

                if (NetworkUtilities.isInternetAvailable(getContext()) && mMovieId != 0) {
                    String trailersRequestUrl = TheMovieDbUtilities.getTrailersAPILink(mMovieId);
                    String reviewsRequestUrl = TheMovieDbUtilities.getReviewsAPILink(mMovieId);

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
            final List<Videos.Results> trailers = mTrailers.getResults();

            int numberOfColumns;
            if (trailers.size() == 0) {
                numberOfColumns = 1;
                String text = getResources().getString(R.string.no_trailers);
                trailersContainerRv.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
                TrailersRecycleViewAdapter mTrailersRecycleViewAdapter = new TrailersRecycleViewAdapter(this, trailers, text);
                trailersContainerRv.setAdapter(mTrailersRecycleViewAdapter);
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
                trailersContainerRv.setLayoutParams(params);

                //Set the adapter
                trailersContainerRv.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
                TrailersRecycleViewAdapter mTrailersRecycleViewAdapter = new TrailersRecycleViewAdapter(this, trailers, "");
                trailersContainerRv.setAdapter(mTrailersRecycleViewAdapter);
            }
        }

        //Create the Reviews layout
        if (mReviews != null) {
            LinearLayout reviewsContainer = findViewById(R.id.reviews_container);
            reviewsContainer.removeAllViews();
            List<Reviews.Results> reviews = mReviews.getResults();
            if (reviews.size() == 0) {
                TextView noReviews = new TextView(getApplicationContext());
                noReviews.setText(getResources().getString(R.string.no_reviews));
                reviewsContainer.addView(noReviews);
            }

            LinearLayout.LayoutParams delimiterParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);

            for (int i=0; i<reviews.size(); i++) {
                TextView review = new TextView(getApplicationContext());
                review.setText(reviews.get(i).getContent());
                review.setPadding(16,16,16,16);
                reviewsContainer.addView(review);

                TextView author = new TextView(getApplicationContext());
                author.setText(reviews.get(i).getAuthor());
                author.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                author.setPadding(16,16,16,64);
                reviewsContainer.addView(author);

                if (i<reviews.size()-1) {
                    View delimiter = new View(getApplicationContext());
                    delimiter.setLayoutParams(delimiterParams);
                    delimiter.setBackgroundColor(Color.DKGRAY);
                    reviewsContainer.addView(delimiter);
                }
            }
        }

        containerSv.setScrollY(mScrollPosition);
    }
    @Override public void onLoaderReset(Loader<String> loader) {
    }

    private void showLoadingIndicator() {
        trailersLoadingIndicatorPb.setVisibility(View.VISIBLE);
        dynamicLayoutsContainerLl.setVisibility(View.GONE);
    }
    private void hideLoadingIndicator() {
        trailersLoadingIndicatorPb.setVisibility(View.GONE);
        dynamicLayoutsContainerLl.setVisibility(View.VISIBLE);
    }
}
