package com.moviemagic.dpaul.android.app.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.moviemagic.dpaul.android.app.DetailMovieActivity
import com.moviemagic.dpaul.android.app.HomeMovieFragment
import com.moviemagic.dpaul.android.app.R
import com.moviemagic.dpaul.android.app.backgroundmodules.GlobalStaticVariables
import com.moviemagic.dpaul.android.app.backgroundmodules.LogDisplay
import com.moviemagic.dpaul.android.app.backgroundmodules.PicassoLoadImage
import com.moviemagic.dpaul.android.app.backgroundmodules.Utility
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso;
import groovy.transform.CompileStatic

@CompileStatic
class HomeMovieAdapter extends RecyclerView.Adapter<HomeMovieAdapter.HomeMovieAdapterViewHolder> {
    private static final String LOG_TAG = HomeMovieAdapter.class.getSimpleName()

    private Cursor mCursor
    private final Context mContext
    private final TextView mRecyclerviewEmptyTextView
    private final HomeMovieAdapterOnClickHandler mHomeMovieAdapterOnClickHandler


    //Empty constructor
    public HomeMovieAdapter() {
        LogDisplay.callLog(LOG_TAG, 'HomeMovieAdapter empty constructor is called', LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
    }

    public HomeMovieAdapter(Context ctx, TextView recyclerviewEmptyTextView, HomeMovieAdapterOnClickHandler clickHandler) {
        LogDisplay.callLog(LOG_TAG, 'HomeMovieAdapter non-empty constructor is called', LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        mContext = ctx
        mRecyclerviewEmptyTextView = recyclerviewEmptyTextView
        mHomeMovieAdapterOnClickHandler = clickHandler
    }

    public class HomeMovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView movieBackdropImageView
        private final TextView movieTitleTextView
        private final ImageView moviePosterImageView
        private final TextView movieGenreTextView
        private final TextView movieRunTimeTextView
        private final TextView movieReleaseDateTextView
        private final TextView movieUserListTextView


        public HomeMovieAdapterViewHolder(View view) {
            super(view)
            movieBackdropImageView = view.findViewById(R.id.home_movie_backdrop_image) as ImageView
            movieTitleTextView = view.findViewById(R.id.home_movie_title) as TextView
            moviePosterImageView = view.findViewById(R.id.home_movie_poster_image) as ImageView
            movieGenreTextView = view.findViewById(R.id.home_movie_genre) as TextView
            movieRunTimeTextView = view.findViewById(R.id.home_movie_runtime) as TextView
            movieReleaseDateTextView = view.findViewById(R.id.home_movie_release_date) as TextView
            movieUserListTextView = view.findViewById(R.id.home_movie_list_text_container) as TextView
            view.setOnClickListener(this)
        }
        public void onClick(View v) {
            LogDisplay.callLog(LOG_TAG,"onClick is called.LayoutPos=${getLayoutPosition()}.AdapterPos=${getAdapterPosition()}",LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
            mCursor.moveToPosition(getAdapterPosition())
            final int movieId = mCursor.getInt(HomeMovieFragment.COL_MOVIE_BASIC_MOVIE_ID)
            final String movieCategory = mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_MOVIE_CATEGORY)
            mHomeMovieAdapterOnClickHandler.onClick(movieId, movieCategory, this)
        }
    }

    @Override
    HomeMovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogDisplay.callLog(LOG_TAG, 'onCreateViewHolder is called', LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_home_movie_grid, parent, false)
        view.setFocusable(true)
        return new HomeMovieAdapterViewHolder(view)
    }

    @Override
    void onBindViewHolder(HomeMovieAdapterViewHolder holder, int position) {
        // move the cursor to correct position
        mCursor.moveToPosition(position)
        LogDisplay.callLog(LOG_TAG,'onBindViewHolder is called',LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        final String backdropPath = "$GlobalStaticVariables.TMDB_IMAGE_BASE_URL/$GlobalStaticVariables.TMDB_IMAGE_SIZE_W780" +
                "${mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_BACKDROP_PATH)}"
        PicassoLoadImage.loadViewPagerImage(mContext,backdropPath,holder.movieBackdropImageView)
        holder.movieTitleTextView.setText(mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_TITLE))
        final String posterPath = "$GlobalStaticVariables.TMDB_IMAGE_BASE_URL/$GlobalStaticVariables.TMDB_IMAGE_SIZE_W300" +
                "${mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_POSTER_PATH)}"
        final Callback picassoPosterCallback = new Callback() {
            @Override
            void onSuccess() {
                //Do nothing
            }

            @Override
            void onError() {
                //Hide the poster
                holder.moviePosterImageView.setVisibility(ImageView.GONE)
                //TODO: Need to check if this is the best way to do this - remove the hard ref of callback
                Picasso.with(mContext).cancelRequest(holder.moviePosterImageView)
            }
        }
        PicassoLoadImage.loadDetailFragmentPosterImage(mContext,posterPath,holder.moviePosterImageView, picassoPosterCallback)
        if(mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_GENRE)) {
            holder.movieGenreTextView.setVisibility(TextView.VISIBLE)
            holder.movieGenreTextView.setText(mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_GENRE))
        } else {
            holder.movieGenreTextView.setVisibility(TextView.GONE)
        }
        if (mCursor.getInt(HomeMovieFragment.COL_MOVIE_BASIC_RUN_TIME) > 0) {
            final String runtime = mContext.getString(R.string.home_movie_run_time_text)
            holder.movieRunTimeTextView.setVisibility(TextView.VISIBLE)
            holder.movieRunTimeTextView.setText("$runtime: ${Utility.formatRunTime(mContext, mCursor.getInt(HomeMovieFragment.COL_MOVIE_BASIC_RUN_TIME))}")
        } else {
            holder.movieRunTimeTextView.setVisibility(TextView.GONE)
        }
        if (mCursor.getLong(HomeMovieFragment.COL_MOVIE_BASIC_RELEASE_DATE) > 0) {
            final String relDate = Utility.formatMilliSecondsToDate(mCursor.getLong(HomeMovieFragment.COL_MOVIE_BASIC_RELEASE_DATE))
            holder.movieReleaseDateTextView.setText("${mContext.getResources().getString(R.string.home_movie_release_date_text)}: $relDate")
        } else {
            holder.movieReleaseDateTextView.setText("${mContext.getResources().getString(R.string.home_movie_release_date_text)}: ${mContext.getResources().getString(R.string.movie_data_not_available)}")
        }
        final String listType
        switch (mCursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_MOVIE_CATEGORY)) {
            case GlobalStaticVariables.MOVIE_CATEGORY_NOW_PLAYING:
                listType = mContext.getResources().getString(R.string.home_movie_in_cinema_text)
                break

            case GlobalStaticVariables.MOVIE_CATEGORY_UPCOMING:
                listType = mContext.getResources().getString(R.string.home_movie_coming_soon_text)
                break

            case GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_WATCHED:
                listType = mContext.getResources().getString(R.string.home_movie_watched_list_text)
                break

            case GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_WISH_LIST:
                listType = mContext.getResources().getString(R.string.home_movie_wish_list_text)
                break

            case GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_FAVOURITE:
                listType = mContext.getResources().getString(R.string.home_movie_favourite_list_text)
                break

            case GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_COLLECTION:
                listType = mContext.getResources().getString(R.string.home_movie_collection_list_text)
                break

            default:
                LogDisplay.callLog(LOG_TAG, "Unknown category. category->${Cursor.getString(HomeMovieFragment.COL_MOVIE_BASIC_MOVIE_CATEGORY)}", LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        }
        holder.movieUserListTextView.setText(listType)
    }

    @Override
    int getItemCount() {
//        LogDisplay.callLog(LOG_TAG,'Cursor item count is called',LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        if (null == mCursor) {
//            LogDisplay.callLog(LOG_TAG, "Cursor item count = 0", LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
            return 0
        }
//        LogDisplay.callLog(LOG_TAG, "Cursor item count = ${mCursor.getCount()}", LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        return mCursor.getCount()
    }

    public void swapCursor(Cursor newCursor) {
        LogDisplay.callLog(LOG_TAG,'swapCursor is called',LogDisplay.HOME_MOVIE_ADAPTER_LOG_FLAG)
        mCursor = newCursor
        if (getItemCount() == 0) {
            mRecyclerviewEmptyTextView.setVisibility(TextView.VISIBLE)
        } else {
            mRecyclerviewEmptyTextView.setVisibility(TextView.INVISIBLE)
            notifyDataSetChanged()
        }
    }

    /**
     * This is the interface which will be implemented by the host PersonMovieFragment
     */
    public interface HomeMovieAdapterOnClickHandler {
        public void onClick(int movieId, String movieCategory, HomeMovieAdapterViewHolder viewHolder)
    }
}