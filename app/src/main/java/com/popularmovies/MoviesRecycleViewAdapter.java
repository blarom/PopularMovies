package com.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesRecycleViewAdapter extends RecyclerView.Adapter<MoviesRecycleViewAdapter.MovieEntryViewHolder>  {

    private MovieList mMovieList;
    private Context mContext;
    final private ListItemClickHandler mOnClickHandler;
    private int mBiggestHolderWidth;

    MoviesRecycleViewAdapter(Context context, ListItemClickHandler listener, MovieList movieList) {
        this.mContext = context;
        this.mOnClickHandler = listener;
        this.mMovieList = movieList;
    }

    @Override public MovieEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.movies_list_item, parent, false);
        view.setFocusable(true);
        return new MovieEntryViewHolder(view);
    }
    @Override public void onBindViewHolder(MovieEntryViewHolder holder, int position) {
        List<MovieList.Results> results = mMovieList.getResults();

        String movieTitle = results.get(position).getTitleValue();
        TextView textView = holder.imageDescriptionInRecycleView;
        textView.setText(movieTitle);

        String posterPath = results.get(position).getPosterPath();
        updateImageInGrid(posterPath, holder);
    }
    @Override public int getItemCount() {
        return mMovieList.getResults().size();
    }
    private void updateImageInGrid(String path, final MovieEntryViewHolder holder) {

        //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
        String size = "w185";

        ImageView imageView = holder.imageInRecycleView;
        holder.container.requestLayout();
        //Picasso.with(mContext).load("http://image.tmdb.org/t/p/" + size + "/" + path).into(imageView);
        Picasso.with(mContext).load("http://image.tmdb.org/t/p/" + size + "/" + path).into(imageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                //Inspired from: https://stackoverflow.com/questions/34067472/update-recyclerview-items-height-after-image-was-loaded
                Drawable drawable = holder.imageInRecycleView.getDrawable();
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();


                int holderWidth = holder.container.getWidth();
                if (mBiggestHolderWidth >= holderWidth) holderWidth = mBiggestHolderWidth;
                else mBiggestHolderWidth = holderWidth;

                int maxHolderHeight = (int) (((float) drawableHeight) / ((float) drawableWidth) * ((float) holderWidth));
                holder.container.setMinHeight(maxHolderHeight);
                holder.container.setMaxHeight(maxHolderHeight);
                holder.container.requestLayout();

            }


            @Override
            public void onError() {
            }
        });

    }

    class MovieEntryViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView imageDescriptionInRecycleView;
        ImageView imageInRecycleView;
        ConstraintLayout container;
        int element_id;

        MovieEntryViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.recyclerViewItemLayout);
            imageDescriptionInRecycleView = itemView.findViewById(R.id.imageDescription);
            imageInRecycleView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            List<MovieList.Results> results = mMovieList.getResults();
            MovieList.Results selectedResults = results.get(clickedPosition);
            mOnClickHandler.onListItemClick(selectedResults);
        }
    }

    public interface ListItemClickHandler {
        void onListItemClick(MovieList.Results selectedResults);
    }
}
