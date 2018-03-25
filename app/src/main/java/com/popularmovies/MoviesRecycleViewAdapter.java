package com.popularmovies;


import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.popularmovies.data.Movies;
import com.popularmovies.data.MoviesDbContract;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesRecycleViewAdapter extends RecyclerView.Adapter<MoviesRecycleViewAdapter.MovieEntryViewHolder>  {

    private Context mContext;
    final private ListItemClickHandler mOnClickHandler;
    private int mBiggestHolderWidth;
    private Cursor mMoviesCursor;

    MoviesRecycleViewAdapter(Context context, ListItemClickHandler listener) {
        this.mContext = context;
        this.mOnClickHandler = listener;
    }

    @Override public MovieEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.movies_list_item, parent, false);
        view.setFocusable(true);
        return new MovieEntryViewHolder(view);
    }
    @Override public void onBindViewHolder(MovieEntryViewHolder holder, int position) {

        //Moving the cursor to the desired row and skipping the next calls if there is no such position
        if (!mMoviesCursor.moveToPosition(position)) return;

        // Determine the values of the wanted data
        String movieTitleFromCursor = mMoviesCursor.getString(mMoviesCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_TITLE));
        String posterPathFromCursor = mMoviesCursor.getString(mMoviesCursor.getColumnIndex(MoviesDbContract.MoviesDbEntry.COLUMN_POSTER_PATH));

        //Update the values in the layout
        TextView textView = holder.imageDescriptionInRecycleView;
        textView.setText(movieTitleFromCursor);
        updateImageInGrid(posterPathFromCursor, holder);
    }
    @Override public int getItemCount() {
        return mMoviesCursor.getCount();
    }
    private void updateImageInGrid(String path, final MovieEntryViewHolder holder) {

        //sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
        String size = "w185";

        ImageView imageView = holder.imageInRecycleView;
        holder.container.requestLayout();
        //Picasso.with(mContext).load("http://image.tmdb.org/t/p/" + size + "/" + path).into(posterIv);
        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/" + size + "/" + path)
                .error(R.drawable.ic_missing_image)
                .into(imageView, new com.squareup.picasso.Callback() {
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

    void swapCursor(Cursor newCursor) {
        if (mMoviesCursor != null) mMoviesCursor.close();
        mMoviesCursor = newCursor;
        if (newCursor != null) this.notifyDataSetChanged();
    }

    class MovieEntryViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView imageDescriptionInRecycleView;
        ImageView imageInRecycleView;
        ConstraintLayout container;

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
            mOnClickHandler.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickHandler {
        void onListItemClick(int clickedItemIndex);
    }
}
