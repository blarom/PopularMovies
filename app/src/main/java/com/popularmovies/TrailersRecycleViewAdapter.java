package com.popularmovies;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.popularmovies.data.Videos;

import java.util.List;

public class TrailersRecycleViewAdapter extends RecyclerView.Adapter<TrailersRecycleViewAdapter.TrailerEntryViewHolder>  {

    private final String mText;
    private List<Videos.Results> mTrailers;
    private Context mContext;

    TrailersRecycleViewAdapter(Context context, List<Videos.Results> trailers, String text) {
        this.mContext = context;
        this.mTrailers = trailers;
        this.mText = text;
    }

    @Override public TrailerEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.trailers_list_item, parent, false);
        view.setFocusable(true);
        return new TrailerEntryViewHolder(view);
    }
    @Override public void onBindViewHolder(TrailerEntryViewHolder holder, int position) {

        ImageView trailerPreview = holder.imageInRecycleView;
        trailerPreview.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        trailerPreview.setBackgroundColor(Color.BLACK);
        trailerPreview.setColorFilter(Color.WHITE);
    }
    @Override public int getItemCount() {
        return mTrailers.size();
    }

    class TrailerEntryViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView trailerTextInRecycleView;
        ImageView imageInRecycleView;
        ConstraintLayout container;

        TrailerEntryViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.trailerRecyclerViewItemLayout);
            trailerTextInRecycleView = itemView.findViewById(R.id.trailer_text);
            imageInRecycleView = itemView.findViewById(R.id.trailer_image);

            if (mTrailers.size() > 0) {
                imageInRecycleView.setVisibility(View.VISIBLE);
                trailerTextInRecycleView.setVisibility(View.GONE);
                itemView.setOnClickListener(this);
            }
            else {
                imageInRecycleView.setVisibility(View.GONE);
                trailerTextInRecycleView.setVisibility(View.VISIBLE);
                trailerTextInRecycleView.setText(mText);
            }
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            if (mTrailers.size() > 0) {
                String currentTrailerKey = mTrailers.get(clickedPosition).getKey();

                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + currentTrailerKey));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + currentTrailerKey));

                try {
                    mContext.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    mContext.startActivity(webIntent);
                }
            }
        }
    }
}
