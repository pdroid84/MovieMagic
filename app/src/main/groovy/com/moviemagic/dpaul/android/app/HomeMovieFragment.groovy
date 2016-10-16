package com.moviemagic.dpaul.android.app

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.PagerTitleStrip
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.moviemagic.dpaul.android.app.adapter.HomeMovieAdapter
import com.moviemagic.dpaul.android.app.adapter.HomeVideoDummyAdapter
import com.moviemagic.dpaul.android.app.adapter.HomeVideoPagerAdapter
import com.moviemagic.dpaul.android.app.adapter.PersonCastAdapter
import com.moviemagic.dpaul.android.app.backgroundmodules.GlobalStaticVariables
import com.moviemagic.dpaul.android.app.backgroundmodules.LoadMovieDetails
import com.moviemagic.dpaul.android.app.backgroundmodules.LogDisplay
import com.moviemagic.dpaul.android.app.contentprovider.MovieMagicContract
import com.moviemagic.dpaul.android.app.youtube.MovieMagicYoutubeFragment;
import groovy.transform.CompileStatic

@CompileStatic
class HomeMovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = HomeMovieFragment.class.getSimpleName()

//    private ViewPager mViewPager
//    private PagerTitleStrip mPagerTitleStrip
    private RecyclerView mInCinemaRecyclerView
    private RecyclerView mComingSoonRecyclerView
    private RecyclerView mRecentlyAddedUserListRecyclerView
    private TextView mInCinemaRecyclerViewEmptyTextView
    private TextView mComingSoonRecyclerViewEmptyTextView
    private TextView mRecentlyAddedUserListRecyclerViewEmptyTextView
    private HomeMovieAdapter mInCinemaAdapter
    private HomeMovieAdapter mComingSoonAdapter
    private HomeMovieAdapter mRecentlyAddedUserListAdapter
    private CallbackForHomeMovieClick mCallbackForHomeMovieClick
    private String[] mMovieVideoArg


    private static final int HOME_MOVIE_FRAGMENT_VIEW_PAGER_LOADER_ID = 0
    private static final int HOME_MOVIE_FRAGMENT_IN_CINEMA_LOADER_ID = 1
    private static final int HOME_MOVIE_FRAGMENT_COMING_SOON_LOADER_ID = 2
    private static final int HOME_MOVIE_FRAGMENT_RECENTLY_ADDED_USER_LIST_LOADER_ID = 3

    //Columns to fetch from movie_video table
    private static final String[] MOVIE_VIDEO_COLUMNS = [MovieMagicContract.MovieVideo._ID,
                                                         MovieMagicContract.MovieVideo.COLUMN_VIDEO_ORIG_MOVIE_ID,
                                                         MovieMagicContract.MovieVideo.COLUMN_VIDEO_KEY,
                                                         MovieMagicContract.MovieVideo.COLUMN_VIDEO_NAME,
                                                         MovieMagicContract.MovieVideo.COLUMN_VIDEO_SITE,
                                                         MovieMagicContract.MovieVideo.COLUMN_VIDEO_TYPE]
    //These are indices of the above columns, if projection array changes then this needs to be changed
    final static int COL_MOVIE_VIDEO_ID = 0
    final static int COL_MOVIE_VIDEO_ORIG_MOVIE_ID = 1
    final static int COL_MOVIE_VIDEO_KEY = 2
    final static int COL_MOVIE_VIDEO_NAME = 3
    final static int COL_MOVIE_VIDEO_SITE = 4
    final static int COL_MOVIE_VIDEO_TYPE = 5

    //Columns to fetch from movie_basic_info table
    private static final String[] MOVIE_BASIC_INFO_COLUMNS = [MovieMagicContract.MovieBasicInfo._ID,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_ID,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_BACKDROP_PATH,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_TITLE,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_DATE,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_POSTER_PATH,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_LIST_TYPE,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_GENRE,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_RUNTIME,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_STATUS,
                                                              MovieMagicContract.MovieBasicInfo.COLUMN_DETAIL_DATA_PRESENT_FLAG]

    //These are indices of the above columns, if projection array changes then this needs to be changed
    final static int COL_MOVIE_BASIC_ID = 0
    final static int COL_MOVIE_BASIC_MOVIE_ID = 1
    final static int COL_MOVIE_BASIC_BACKDROP_PATH = 2
    final static int COL_MOVIE_BASIC_TITLE = 3
    final static int COL_MOVIE_BASIC_RELEASE_DATE = 4
    final static int COL_MOVIE_BASIC_POSTER_PATH = 5
    final static int COL_MOVIE_BASIC_MOVIE_CATEGORY = 6
    final static int COL_MOVIE_BASIC_MOVIE_LIST_TYPE = 7
    final static int COL_MOVIE_BASIC_GENRE = 8
    final static int COL_MOVIE_BASIC_RUN_TIME = 9
    final static int COL_MOVIE_BASIC_RELEASE_STATUS = 10
    final static int COL_MOVIE_BASIC_DETAIL_DATA_PRESENT_FLAG = 11

    //An empty constructor is needed so that lifecycle is properly handled
    public GridMovieFragment(){
        LogDisplay.callLog(LOG_TAG,'HomeMovieFragment empty constructor is called',LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogDisplay.callLog(LOG_TAG, 'onCreate is called', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        super.onCreate(savedInstanceState)
        //Following line needed to let android know that Fragment has options menu
        //If this line is not added then associated method (e.g. OnCreateOptionsMenu) does not get supported
        //even in auto code completion
        setHasOptionsMenu(true)
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        // Inflate the menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.home_fragment_menu, menu)
    }

    @Override
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogDisplay.callLog(LOG_TAG,'onCreateView is called',LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        View mRootView = inflater.inflate(R.layout.fragment_home_movie,container,false)
//        mViewPager = mRootView.findViewById(R.id.home_movie_viewpager) as ViewPager
//        mPagerTitleStrip = mRootView.findViewById(R.id.home_movie_viewpager_title_strip) as PagerTitleStrip
        mInCinemaRecyclerView = mRootView.findViewById(R.id.home_movie_in_cinema_recycler_view) as RecyclerView
        mInCinemaRecyclerViewEmptyTextView = mRootView.findViewById(R.id.home_movie_in_cinema_recycler_view_empty_msg_text_view) as TextView
        mComingSoonRecyclerView = mRootView.findViewById(R.id.home_movie_coming_soon_recycler_view) as RecyclerView
        mComingSoonRecyclerViewEmptyTextView = mRootView.findViewById(R.id.home_movie_coming_soon_recycler_view_empty_msg_text_view) as TextView
        mRecentlyAddedUserListRecyclerView = mRootView.findViewById(R.id.home_movie_recently_added_recycler_view) as RecyclerView
        mRecentlyAddedUserListRecyclerViewEmptyTextView = mRootView.findViewById(R.id.home_movie_recently_added_recycler_view_empty_msg_text_view) as TextView
        //Set this to false for smooth scrolling of recyclerview
        mInCinemaRecyclerView.setNestedScrollingEnabled(false)
        mComingSoonRecyclerView.setFocusable(false)
        mRecentlyAddedUserListRecyclerView.setNestedScrollingEnabled(false)
        /**
         * In Cinema Recycler View
         */
        final RecyclerView.LayoutManager inCinemaLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        inCinemaLinearLayoutManager.setAutoMeasureEnabled(true)
        mInCinemaRecyclerView.setLayoutManager(inCinemaLinearLayoutManager)
        mInCinemaRecyclerView.setFocusable(false)
        mInCinemaAdapter = new HomeMovieAdapter(getActivity(), mInCinemaRecyclerViewEmptyTextView,
                new HomeMovieAdapter.HomeMovieAdapterOnClickHandler(){
                    @Override
                    void onClick(int movieId, String movieCategory, HomeMovieAdapter.HomeMovieAdapterViewHolder viewHolder) {
                        mCallbackForHomeMovieClick.onHomeMovieItemSelected(movieId,movieCategory,viewHolder)
                    }
                })
        mInCinemaRecyclerView.setAdapter(mInCinemaAdapter)
        /**
         * Coming Soon Recycler View
         */
        final RecyclerView.LayoutManager comingSoonLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        comingSoonLinearLayoutManager.setAutoMeasureEnabled(true)
        mComingSoonRecyclerView.setLayoutManager(comingSoonLinearLayoutManager)
        mComingSoonRecyclerView.setFocusable(false)
        mComingSoonAdapter = new HomeMovieAdapter(getActivity(), mComingSoonRecyclerViewEmptyTextView,
                new HomeMovieAdapter.HomeMovieAdapterOnClickHandler(){
                    @Override
                    void onClick(int movieId, String movieCategory, HomeMovieAdapter.HomeMovieAdapterViewHolder viewHolder) {
                        mCallbackForHomeMovieClick.onHomeMovieItemSelected(movieId,movieCategory,viewHolder)
                    }
                })
        mComingSoonRecyclerView.setAdapter(mComingSoonAdapter)
        /**
         * Recently Added User List Recycler View
         */
        final RecyclerView.LayoutManager recentlyAddedUserListLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        recentlyAddedUserListLinearLayoutManager.setAutoMeasureEnabled(true)
        mRecentlyAddedUserListRecyclerView.setLayoutManager(recentlyAddedUserListLinearLayoutManager)
        mRecentlyAddedUserListRecyclerView.setNestedScrollingEnabled(false)
        mRecentlyAddedUserListAdapter = new HomeMovieAdapter(getActivity(), mRecentlyAddedUserListRecyclerViewEmptyTextView,
                new HomeMovieAdapter.HomeMovieAdapterOnClickHandler(){
                    @Override
                    void onClick(int movieId, String movieCategory, HomeMovieAdapter.HomeMovieAdapterViewHolder viewHolder) {
                        mCallbackForHomeMovieClick.onHomeMovieItemSelected(movieId,movieCategory,viewHolder)
                    }
                })
        mRecentlyAddedUserListRecyclerView.setAdapter(mRecentlyAddedUserListAdapter)

        return mRootView
    }

    @Override
    void onActivityCreated(Bundle savedInstanceState) {
        LogDisplay.callLog(LOG_TAG, 'onActivityCreated is called', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        super.onActivityCreated(savedInstanceState)
        mMovieVideoArg = ['', GlobalStaticVariables.MOVIE_VIDEO_SITE_YOUTUBE, GlobalStaticVariables.MOVIE_VIDEO_SITE_TYPE] as String[]
        //If it's a fresh start then call init loader
        if(savedInstanceState == null) {
            LogDisplay.callLog(LOG_TAG, 'onActivityCreated:first time, so init loaders', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
            getLoaderManager().initLoader(HOME_MOVIE_FRAGMENT_VIEW_PAGER_LOADER_ID, null, this)
            getLoaderManager().initLoader(HOME_MOVIE_FRAGMENT_IN_CINEMA_LOADER_ID, null, this)
            getLoaderManager().initLoader(HOME_MOVIE_FRAGMENT_COMING_SOON_LOADER_ID, null, this)
            getLoaderManager().initLoader(HOME_MOVIE_FRAGMENT_RECENTLY_ADDED_USER_LIST_LOADER_ID, null, this)
        } else {        //If it's restore then restart the loader
            LogDisplay.callLog(LOG_TAG, 'onActivityCreated:not first time, so restart loaders', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
            getLoaderManager().restartLoader(HOME_MOVIE_FRAGMENT_VIEW_PAGER_LOADER_ID, null, this)
            getLoaderManager().restartLoader(HOME_MOVIE_FRAGMENT_IN_CINEMA_LOADER_ID, null, this)
            getLoaderManager().restartLoader(HOME_MOVIE_FRAGMENT_COMING_SOON_LOADER_ID, null, this)
            getLoaderManager().restartLoader(HOME_MOVIE_FRAGMENT_RECENTLY_ADDED_USER_LIST_LOADER_ID, null, this)
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.home_fragment_menu) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Override
    Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogDisplay.callLog(LOG_TAG, "onCreateLoader.loader id->$id", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        switch (id) {
            case HOME_MOVIE_FRAGMENT_VIEW_PAGER_LOADER_ID:
                return new CursorLoader(
                        getActivity(),                                                        //Parent Activity Context
                        MovieMagicContract.MovieVideo.CONTENT_URI,                            //Table to query
                        MOVIE_VIDEO_COLUMNS,                                                  //Projection to return
                        """$MovieMagicContract.MovieVideo.COLUMN_VIDEO_ORIG_MOVIE_ID != ? and
                            $MovieMagicContract.MovieVideo.COLUMN_VIDEO_SITE = ? and
                            $MovieMagicContract.MovieVideo.COLUMN_VIDEO_TYPE = ? """,         //Selection Clause
                        mMovieVideoArg,                                                            //Selection Arg
                        null)                                                                 //Not bother on sorting

            case HOME_MOVIE_FRAGMENT_IN_CINEMA_LOADER_ID:
                return new CursorLoader(
                        getActivity(),                                                          //Parent Activity Context
                        MovieMagicContract.MovieBasicInfo.CONTENT_URI,                          //Table to query
                        MOVIE_BASIC_INFO_COLUMNS,                                               //Projection to return
                        """$MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY = ? and
                        $MovieMagicContract.MovieBasicInfo.COLUMN_BACKDROP_PATH <> ? """,       //Selection Clause
                        [GlobalStaticVariables.MOVIE_CATEGORY_NOW_PLAYING, ''] as String[],     //Selection Arg
                        "$MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_DATE desc limit $GlobalStaticVariables.HOME_PAGE_MAX_MOVIE_SHOW_COUNTER") //Sorted on release date

            case HOME_MOVIE_FRAGMENT_COMING_SOON_LOADER_ID:
                return new CursorLoader(
                        getActivity(),                                                          //Parent Activity Context
                        MovieMagicContract.MovieBasicInfo.CONTENT_URI,                          //Table to query
                        MOVIE_BASIC_INFO_COLUMNS,                                               //Projection to return
                        """$MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY = ? and
                        $MovieMagicContract.MovieBasicInfo.COLUMN_POSTER_PATH <> ? and
                        $MovieMagicContract.MovieBasicInfo.COLUMN_BACKDROP_PATH <> ? """,       //Selection Clause
                        [GlobalStaticVariables.MOVIE_CATEGORY_UPCOMING, '', ''] as String[],        //Selection Arg
                        "$MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_DATE desc limit $GlobalStaticVariables.HOME_PAGE_MAX_MOVIE_SHOW_COUNTER") //Sorted on release date

            case HOME_MOVIE_FRAGMENT_RECENTLY_ADDED_USER_LIST_LOADER_ID:
                return new CursorLoader(
                        getActivity(),                                                              //Parent Activity Context
                        MovieMagicContract.MovieBasicInfo.CONTENT_URI,                              //Table to query
                        MOVIE_BASIC_INFO_COLUMNS,                                                   //Projection to return
                        """$MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_LIST_TYPE = ? and
                        $MovieMagicContract.MovieBasicInfo.COLUMN_POSTER_PATH <> ? and
                        $MovieMagicContract.MovieBasicInfo.COLUMN_BACKDROP_PATH <> ? """,           //Selection Clause
                        [GlobalStaticVariables.MOVIE_LIST_TYPE_USER_LOCAL_LIST, '', ''] as String[],    //Selection Arg
                        "$MovieMagicContract.MovieBasicInfo.COLUMN_CREATE_TIMESTAMP desc limit $GlobalStaticVariables.HOME_PAGE_MAX_MOVIE_SHOW_COUNTER") //Sorted on release date
        }
    }

    @Override
    void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId()
        LogDisplay.callLog(LOG_TAG, "onLoadFinished.loader id->$loaderId", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        switch (loaderId) {
            case HOME_MOVIE_FRAGMENT_VIEW_PAGER_LOADER_ID:
                handleTrailerOnLoadFinished(data)
                break
            case HOME_MOVIE_FRAGMENT_IN_CINEMA_LOADER_ID:
                handleInCinemaOnLoadFinished(data)
                break
            case HOME_MOVIE_FRAGMENT_COMING_SOON_LOADER_ID:
                handleComingSoonOnLoadFinished(data)
                break
            case HOME_MOVIE_FRAGMENT_RECENTLY_ADDED_USER_LIST_LOADER_ID:
                recentlyAddedUserListOnLoadFinished(data)
                break
            default:
                LogDisplay.callLog(LOG_TAG, "Unknown loader id. id->$loaderId", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        }
    }

    @Override
    void onLoaderReset(Loader<Cursor> loader) {
        LogDisplay.callLog(LOG_TAG, 'onLoaderReset is called', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        //Reset the adapters
        mInCinemaAdapter.swapCursor(null)
        mComingSoonAdapter.swapCursor(null)
        mRecentlyAddedUserListAdapter.swapCursor(null)
    }

    /**
     * This method handles the video (movie trailer) cursor
     * @param data Cursor
     */
    void handleTrailerOnLoadFinished(Cursor data) {
        LogDisplay.callLog(LOG_TAG, "handleTrailerOnLoadFinished.Cursor rec count -> ${data.getCount()}", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        List<String> youtubeVideoKey = new ArrayList<>()
        if (data.moveToFirst()) {
            for (i in 0..(data.count - 1)) {
                youtubeVideoKey.add(data.getString(COL_MOVIE_VIDEO_KEY))
                data.moveToNext()
            }
            LogDisplay.callLog(LOG_TAG, "YouTube now_playing key= $youtubeVideoKey", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        }
        final int videoCount = data.getCount()
//        final HomeVideoPagerAdapter homeVideoPagerAdapter = new HomeVideoPagerAdapter(getChildFragmentManager(),
//            videoCount,youtubeVideoKey)
//        final HomeVideoDummyAdapter homeVideoPagerAdapter = new HomeVideoDummyAdapter(getActivity() ,getChildFragmentManager(),
//        videoCount,youtubeVideoKey)
//        mViewPager.setAdapter(homeVideoPagerAdapter)
//        mViewPager.setOffscreenPageLimit(1)
//        final View view = getView()
        final MovieMagicYoutubeFragment movieMagicYoutubeFragment = MovieMagicYoutubeFragment
                .createMovieMagicYouTubeFragment(youtubeVideoKey)
        getChildFragmentManager().beginTransaction().replace(R.id.home_youtube_fragment_container, movieMagicYoutubeFragment).commit()

//        mViewPager.setCurrentItem(0)
//        MovieMagicYoutubeFragment.set
//        mViewPager.setVisibility(ViewPager.INVISIBLE)
    }

    /**
     * This method handles the in cinema (i.e. Now Playing) movie cursor
     * @param data Cursor
     */
    void handleInCinemaOnLoadFinished(Cursor data) {
        LogDisplay.callLog(LOG_TAG, "handleInCinemaOnLoadFinished.Cursor rec count -> ${data.getCount()}", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        //Load details - needed to populate genre, run time & video ids
//        final ArrayList<Integer> mMovieIdList = new ArrayList<>()
//        final ArrayList<Integer> mMovieRowIdList = new ArrayList<>()
//
//        final counter = 0
//        if(data.moveToFirst()) {
//            for (i in 0..(data.getCount() - 1)) {
//                if(data.getInt(COL_MOVIE_BASIC_DETAIL_DATA_PRESENT_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE) {
//                    mMovieIdList.add(counter, data.getInt(COL_MOVIE_BASIC_MOVIE_ID))
//                    mMovieRowIdList.add(counter, data.getInt(COL_MOVIE_BASIC_ID))
//                    counter++
//                }
//                data.moveToNext()
//            }
//            if(counter > 0) {
//                LogDisplay.callLog(LOG_TAG, "Going to load data for $counter records", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
//                final ArrayList<Integer>[] loadMovieDetailsArg = [mMovieIdList, mMovieRowIdList] as ArrayList<Integer>[]
//                new LoadMovieDetails(getActivity()).execute(loadMovieDetailsArg)
//
//            }
//            mInCinemaAdapter.swapCursor(data)
//        } else {
//            LogDisplay.callLog(LOG_TAG, 'handleInCinemaOnLoadFinished: empty cursor', LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
//        }
        mInCinemaAdapter.swapCursor(data)
    }

    /**
     * This method handles the coming soon (i.e. Upcoming) movie cursor
     * @param data Cursor
     */
    void handleComingSoonOnLoadFinished(Cursor data) {
        LogDisplay.callLog(LOG_TAG, "handleComingSoonOnLoadFinished.Cursor rec count -> ${data.getCount()}", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        mComingSoonAdapter.swapCursor(data)
    }

    /**
     * This method handles the recently added user list (i.e. Watched or Wishlist or Favourite or Collection) movie cursor
     * @param data Cursor
     */
    void recentlyAddedUserListOnLoadFinished(Cursor data) {
        LogDisplay.callLog(LOG_TAG, "recentlyAddedUserListOnLoadFinished.Cursor rec count -> ${data.getCount()}", LogDisplay.HOME_MOVIE_FRAGMENT_LOG_FLAG)
        mRecentlyAddedUserListAdapter.swapCursor(data)
    }

    @Override
    public void onAttach(Context context) {
        LogDisplay.callLog(LOG_TAG,'onAttach is called',LogDisplay.PERSON_MOVIE_FRAGMENT_LOG_FLAG)
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            if(context instanceof Activity) {
                mCallbackForHomeMovieClick = (CallbackForHomeMovieClick) context
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CallbackForCastClick interface")
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of home movie
     * item click.
     */
    public interface CallbackForHomeMovieClick {
        /**
         * PersonCastFragmentCallback when a movie item has been clicked for cast.
         */
        public void onHomeMovieItemSelected(int movieId, String movieCategory, HomeMovieAdapter.HomeMovieAdapterViewHolder viewHolder)
    }
}