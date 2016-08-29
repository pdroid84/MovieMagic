package com.moviemagic.dpaul.android.app.utility

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.AsyncTask
import android.support.design.widget.Snackbar
import android.widget.LinearLayout
import com.moviemagic.dpaul.android.app.R
import com.moviemagic.dpaul.android.app.contentprovider.MovieMagicContract
import groovy.transform.CompileStatic

@CompileStatic
class UpdateUserList extends AsyncTask<String, Void, Integer> {
    private static final String LOG_TAG = UpdateUserList.class.getSimpleName()
    private final ContentResolver mContentResolver
    private final Context mContext
    private final LinearLayout mUserDrawableLayout
    private final int mMovieBasicInfo_ID
    private final int mBackgroundColor, mBodyTextColor
    private final int mMovieId
    private final String mMovieTitle
    private String mUserListMsg
    private int mUserFlag
    private float mUserRating
    private final ProgressDialog mProgressDialog

    //Columns to fetch from movie_user_list_flag table
    private static final String[] MOVIE_USER_LIST_FLAG_COLUMNS = [MovieMagicContract.MovieUserListFlag._ID,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_ORIG_MOVIE_ID,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_WATCHED,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_WISH_LIST,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_FAVOURITE,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_COLLECTION,
                                                                  MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_USER_RATING]
    //These are indices of the above columns, if projection array changes then this needs to be changed
    private final static int COL_MOVIE_USER_LIST_FLAG_ID = 0
    private final static int COL_MOVIE_USER_LIST_FLAG_ORIG_MOVIE_ID = 1
    private final static int COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG = 2
    private final static int COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG = 3
    private final static int COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG = 4
    private final static int COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG = 5
    private final static int COL_MOVIE_USER_LIST_FLAG_USER_RATING = 6


    public UpdateUserList(Context ctx, LinearLayout userDrawableLayout, int _ID_movieBasicInfo, int movieId,
                          String movieTitle, int backgroundColor, int bodyTextColor) {
        mContext = ctx
        mContentResolver = mContext.getContentResolver()
        mUserDrawableLayout = userDrawableLayout
        mMovieBasicInfo_ID = _ID_movieBasicInfo
        mMovieId = movieId
        mMovieTitle = movieTitle
        mBodyTextColor = backgroundColor
        mBodyTextColor = bodyTextColor
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER)
    }

    @Override
    protected Integer doInBackground(String... params) {
        final String listType = params[0]
        final String operationType = params[1]
        final float ratingValue = params[2] as Float
        final int retValue
        final String userListCategory
        final ContentValues movieBasicInfoContentValues = new ContentValues()
        final ContentValues movieUserListFlagContentValues = new ContentValues()
//        final String[] _IDArg = [Integer.toString(mMovieBasicInfo_ID)]
        final movieBasicInfoCursor
        //Build the URIs
//        final Uri movieBasicUri = MovieMagicContract.MovieBasicInfo.buildMovieUriWithMovieId(mMovieId)
        final Uri movieUserListFlagUri = MovieMagicContract.MovieUserListFlag.buildMovieUserListFlagUriWithMovieId(mMovieId)

        //Get the record from movie_user_list_flag
        final Cursor movieUSerListFlagCursor = mContentResolver.query(movieUserListFlagUri,MOVIE_USER_LIST_FLAG_COLUMNS,null,null,null)

        LogDisplay.callLog(LOG_TAG,"listType->$listType",LogDisplay.UPDATE_USER_LIST_FLAG)
        LogDisplay.callLog(LOG_TAG,"operationType->$operationType",LogDisplay.UPDATE_USER_LIST_FLAG)
        LogDisplay.callLog(LOG_TAG,"ratingValue->$ratingValue",LogDisplay.UPDATE_USER_LIST_FLAG)
        if(operationType == GlobalStaticVariables.USER_LIST_ADD_FLAG) {
            mUserFlag = GlobalStaticVariables.MOVIE_MAGIC_FLAG_TRUE
            //Get the movie basic details which will be used to create user record
            //This is needed for "ADD" case only as a new user record is to be created
            movieBasicInfoCursor = mContentResolver.query(
                    MovieMagicContract.MovieBasicInfo.CONTENT_URI,
                    null,
                    "$MovieMagicContract.MovieBasicInfo._ID = ? ",
                    [Integer.toString(mMovieBasicInfo_ID)] as String[],
                    null)
            //Position the cursor then convert
            if(movieBasicInfoCursor.moveToFirst()) {
                //Convert the cursor to content values
                DatabaseUtils.cursorRowToContentValues(movieBasicInfoCursor, movieBasicInfoContentValues)
            } else {
                LogDisplay.callLog(LOG_TAG,"Bad cursor from movie_basic_info.Row id->$mMovieBasicInfo_ID",LogDisplay.UPDATE_USER_LIST_FLAG)
            }
        } else if(operationType == GlobalStaticVariables.USER_LIST_REMOVE_FLAG) {
            mUserFlag = GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE
        } else if(operationType == GlobalStaticVariables.USER_RATING_ADD_FLAG) {
            //Store the user rating to be updated
            mUserRating = ratingValue
            //"-1" indicates that it's rating we need to deal with (later use for SnackBar message)
            mUserFlag = -1
        } else if(operationType == GlobalStaticVariables.USER_RATING_REMOVE_FLAG) {
            //just update mUserFlag with "-1" (indicates that it's rating) we need to deal with (later use for SnackBar message)
            mUserFlag = -1
        } else {
            LogDisplay.callLog(LOG_TAG,"Unknown operation type->$operationType",LogDisplay.UPDATE_USER_LIST_FLAG)
        }

        //Based on listType prepare the ContentValues and other parameters
        switch (listType) {
            case GlobalStaticVariables.USER_LIST_WATCHED:
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_WATCHED,mUserFlag)
                userListCategory = GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_WATCHED
                mUserListMsg = mContext.getString(R.string.drawer_menu_user_watched)
                break
            case GlobalStaticVariables.USER_LIST_WISH_LIST:
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_WISH_LIST,mUserFlag)
                userListCategory = GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_WISH_LIST
                mUserListMsg = mContext.getString(R.string.drawer_menu_user_wishlist)
                break
            case GlobalStaticVariables.USER_LIST_FAVOURITE:
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_FAVOURITE,mUserFlag)
                userListCategory = GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_FAVOURITE
                mUserListMsg = mContext.getString(R.string.drawer_menu_user_favourite)
                break
            case GlobalStaticVariables.USER_LIST_COLLECTION:
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_COLLECTION,mUserFlag)
                userListCategory = GlobalStaticVariables.MOVIE_CATEGORY_LOCAL_USER_COLLECTION
                mUserListMsg = mContext.getString(R.string.drawer_menu_user_collection)
                break
            case GlobalStaticVariables.USER_LIST_USER_RATING:
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_USER_RATING,mUserRating)
//                mUserListMsg = mContext.getString(R.string.drawer_menu_user_collection)
                break
            default:
                LogDisplay.callLog(LOG_TAG,"Unknown user list type->$listType",LogDisplay.UPDATE_USER_LIST_FLAG)
        }

        if(operationType == GlobalStaticVariables.USER_LIST_ADD_FLAG ||
                operationType == GlobalStaticVariables.USER_RATING_ADD_FLAG) {
            //If the movie_user_list_flag already present then just update the record
            if(movieUSerListFlagCursor.moveToFirst()) {
                retValue = mContentResolver.update(
                        MovieMagicContract.MovieUserListFlag.CONTENT_URI,
                        movieUserListFlagContentValues,
                        MovieMagicContract.MovieUserListFlag._ID + "= ?",
                        [Long.toString(movieUSerListFlagCursor.getLong(COL_MOVIE_USER_LIST_FLAG_ID))] as String[])
                if(retValue != 1) {
                    LogDisplay.callLog(LOG_TAG,"Update in movie_user_list_flag failed. Update Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                } else { //set the return value to 1, indicate successful insert
                    LogDisplay.callLog(LOG_TAG,"Update in movie_user_list_flag successful. Update Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                }
            } else { //Insert the record in the movie_user_list_flag table
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_FOREIGN_KEY_ID,mMovieBasicInfo_ID)
                movieUserListFlagContentValues.put(MovieMagicContract.MovieUserListFlag.COLUMN_USER_LIST_FLAG_ORIG_MOVIE_ID,mMovieId)
                final Uri uri = mContentResolver.insert(MovieMagicContract.MovieUserListFlag.CONTENT_URI,movieUserListFlagContentValues)
                if(ContentUris.parseId(uri) == -1) {
                    LogDisplay.callLog(LOG_TAG,"Insert in movie_user_list_flag failed. Uri->$uri",LogDisplay.UPDATE_USER_LIST_FLAG)
                } else { //set the return value to 1, indicate successful insert
                    LogDisplay.callLog(LOG_TAG,"Insert in movie_user_list_flag successful. Uri->$uri",LogDisplay.UPDATE_USER_LIST_FLAG)
                    retValue = 1
                }
            }
            //Now create the user record in movie_basic_info table
            //Add the record to movie_basic_info is needed for user list and NOT for rating operation
            if(operationType == GlobalStaticVariables.USER_LIST_ADD_FLAG){
                movieBasicInfoContentValues.put(MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY,userListCategory)
                movieBasicInfoContentValues.put(MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_LIST_TYPE,
                        GlobalStaticVariables.MOVIE_LIST_TYPE_USER_LOCAL_LIST)
                movieBasicInfoContentValues.put(MovieMagicContract.MovieBasicInfo.COLUMN_CREATE_TIMESTAMP,Utility.getTodayDate())
                movieBasicInfoContentValues.put(MovieMagicContract.MovieBasicInfo.COLUMN_UPDATE_TIMESTAMP,Utility.getTodayDate())
                //Since the program logic is written in a way where the release date is expected as yyyy-MM-dd
                //so convert release date (which is stored as milli seconds) to that format
                movieBasicInfoCursor.moveToFirst()
                final int colIndex = movieBasicInfoCursor.getColumnIndex(MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_DATE)
                final long releaseDate = movieBasicInfoCursor.getLong(colIndex)
                final String formattedReleaseDate = Utility.convertMilliSecsToOrigReleaseDate(releaseDate)
                movieBasicInfoContentValues.put(MovieMagicContract.MovieBasicInfo.COLUMN_RELEASE_DATE,formattedReleaseDate)
                //Need to remove the "_ID" as that is system generated
                movieBasicInfoContentValues.remove(MovieMagicContract.MovieBasicInfo._ID)
                final Uri uri = mContentResolver.insert(MovieMagicContract.MovieBasicInfo.CONTENT_URI,movieBasicInfoContentValues)
                if(ContentUris.parseId(uri) == -1) {
                    LogDisplay.callLog(LOG_TAG,"Insert in movie_basic_info failed. Uri->$uri",LogDisplay.UPDATE_USER_LIST_FLAG)
                } else {
                    LogDisplay.callLog(LOG_TAG,"Insert in movie_basic_info successful. Uri->$uri",LogDisplay.UPDATE_USER_LIST_FLAG)
                }
            }
        } else if (operationType == GlobalStaticVariables.USER_LIST_REMOVE_FLAG ||
                    operationType == GlobalStaticVariables.USER_RATING_REMOVE_FLAG) {
            final boolean deleteUserListFlagRecord = false
            if(movieUSerListFlagCursor.moveToFirst()) {
//                LogDisplay.callLog(LOG_TAG,"COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG->${movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG)}",LogDisplay.UPDATE_USER_LIST_FLAG)
//                LogDisplay.callLog(LOG_TAG,"COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG->${movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG)}",LogDisplay.UPDATE_USER_LIST_FLAG)
//                LogDisplay.callLog(LOG_TAG,"COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG->${movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG)}",LogDisplay.UPDATE_USER_LIST_FLAG)
//                LogDisplay.callLog(LOG_TAG,"COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG->${movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG)}",LogDisplay.UPDATE_USER_LIST_FLAG)
//                LogDisplay.callLog(LOG_TAG,"COL_MOVIE_USER_LIST_FLAG_USER_RATING->${movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_USER_RATING)}",LogDisplay.UPDATE_USER_LIST_FLAG)
//                LogDisplay.callLog(LOG_TAG,"mUserRating->$mUserRating",LogDisplay.UPDATE_USER_LIST_FLAG)
                if(listType == GlobalStaticVariables.USER_LIST_WATCHED) {
                    if( movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_USER_RATING) == 0.0) {
                        deleteUserListFlagRecord = true
                    }
                } else if(listType == GlobalStaticVariables.USER_LIST_WISH_LIST) {
                    if( movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_USER_RATING) == 0.0) {
                        deleteUserListFlagRecord = true
                    }
                } else if(listType == GlobalStaticVariables.USER_LIST_FAVOURITE) {
                    if( movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_USER_RATING) == 0.0) {
                        deleteUserListFlagRecord = true
                    }
                } else if(listType == GlobalStaticVariables.USER_LIST_COLLECTION) {
                    if( movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_USER_RATING) == 0.0) {
                        deleteUserListFlagRecord = true
                    }
                } else if(listType == GlobalStaticVariables.USER_LIST_USER_RATING) {
                    if( movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WATCHED_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_WISH_LIST_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_FAVOURITE_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE &&
                        movieUSerListFlagCursor.getInt(COL_MOVIE_USER_LIST_FLAG_COLLECTION_FLAG) == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE) {
                        deleteUserListFlagRecord = true
                    }
                } else {
                    LogDisplay.callLog(LOG_TAG,"Unknown user list type->$listType",LogDisplay.UPDATE_USER_LIST_FLAG)
                }
                //Delete the record from movie_user_list_flag if flag value satisfies
                if(deleteUserListFlagRecord) {
                    retValue = mContentResolver.delete(
                            MovieMagicContract.MovieUserListFlag.CONTENT_URI,
                            "$MovieMagicContract.MovieUserListFlag._ID = ?",
                            [Long.toString(movieUSerListFlagCursor.getLong(COL_MOVIE_USER_LIST_FLAG_ID))] as String[])
                    if(retValue != 1) {
                        LogDisplay.callLog(LOG_TAG,"Delete from movie_user_list_flag failed. Delete Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                    } else {
                        LogDisplay.callLog(LOG_TAG,"Delete from movie_user_list_flag successful. Delete Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                    }
                } else { // update the record in movie_user_list_flag
                    retValue = mContentResolver.update(
                            MovieMagicContract.MovieUserListFlag.CONTENT_URI,
                            movieUserListFlagContentValues,
                            MovieMagicContract.MovieUserListFlag._ID + "= ?",
                            [Long.toString(movieUSerListFlagCursor.getLong(COL_MOVIE_USER_LIST_FLAG_ID))] as String[])
                    if(retValue != 1) {
                        LogDisplay.callLog(LOG_TAG,"Update in movie_user_list_flag failed. Update Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                    } else {
                        LogDisplay.callLog(LOG_TAG,"Update in movie_user_list_flag successful. Update Count->$retValue",LogDisplay.UPDATE_USER_LIST_FLAG)
                    }
                }
                //Now delete the user record from movie_basic_info table
//                final rowCount = mContentResolver.delete(
//                        MovieMagicContract.MovieBasicInfo.CONTENT_URI,
//                        """$MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_ID = ? and
//                            $MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY = ? """,
//                        [Integer.toString(mMovieId),userListCategory] as String[])
                //When user remove the movie from the list it should be ideally deleted but due to the logic
                //of the application, the user can still see the details of the movie even after the delete. So in order
                //to achieve that we need to keep that row in the table otherwise application will crash as the loader 0 of
                //detail fragment will not find the the corresponding movie and would return null. So instead of delete
                //update the record with category "orphaned" which will ensure that it will not come in the user list but
                //record will remain there in the table and later will be cleaned up by the sync adapter while loading new
                //data as the sync adapter has logic to delete anything which is not user list
                //TODO: Will see if a better solution can be found :)
                //Remove the record from movie_basic_info is needed for user list and NOT for rating operation
                if(operationType == GlobalStaticVariables.USER_LIST_REMOVE_FLAG) {
                    final ContentValues movieOrphanContentValue = new ContentValues()
                    movieOrphanContentValue.put(MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY, GlobalStaticVariables.MOVIE_CATEGORY_ORPHANED)
                    movieOrphanContentValue.put(MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_LIST_TYPE, GlobalStaticVariables.MOVIE_LIST_TYPE_ORPHANED)
                    final rowCount = mContentResolver.update(
                            MovieMagicContract.MovieBasicInfo.CONTENT_URI,
                            movieOrphanContentValue,
                            """$MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_ID = ? and
                            $MovieMagicContract.MovieBasicInfo.COLUMN_MOVIE_CATEGORY = ? """,
                            [Integer.toString(mMovieId), userListCategory] as String[])
                    //Expecting just one record to be deleted from movie_basic_info
                    if (rowCount != 1) {
                        LogDisplay.callLog(LOG_TAG, "Update movie_basic_info record to orphaned failed. Update Count->$rowCount", LogDisplay.UPDATE_USER_LIST_FLAG)
                    } else {
                        LogDisplay.callLog(LOG_TAG, "Update movie_basic_info record to orphaned successful. Update Count->$rowCount", LogDisplay.UPDATE_USER_LIST_FLAG)
                    }
                }
            } else {
                LogDisplay.callLog(LOG_TAG,"Record not present in movie_user_list_flag. Movie ID->$mMovieId",LogDisplay.UPDATE_USER_LIST_FLAG)
            }
        }
        //Close the cursors
        if(movieBasicInfoCursor) {
            movieBasicInfoCursor.close()
        }
        if(movieUSerListFlagCursor) {
            movieUSerListFlagCursor.close()
        }
        //Return the value
        return retValue
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute()
        mProgressDialog.show()
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(mProgressDialog) {
            mProgressDialog.dismiss()
        }
        final String snackBarMsg
        if(mUserFlag == GlobalStaticVariables.MOVIE_MAGIC_FLAG_TRUE) {
            snackBarMsg = String.format(mContext.getString(R.string.user_list_add_message),mMovieTitle,mUserListMsg)
        } else if(mUserFlag == GlobalStaticVariables.MOVIE_MAGIC_FLAG_FALSE) {
            snackBarMsg = String.format(mContext.getString(R.string.user_list_del_message),mMovieTitle,mUserListMsg)
        } else if (mUserFlag == -1) { //"-1" indicates user rating
            snackBarMsg = String.format(mContext.getString(R.string.user_rating_add_message,mMovieTitle))
        } else {
            LogDisplay.callLog(LOG_TAG,"Unknown user flag value.User flag value->$mUserFlag",LogDisplay.UPDATE_USER_LIST_FLAG)
        }
        //Expecting a single row update or insert only
        if(result == 1) {
            Snackbar.make(mUserDrawableLayout.findViewById(R.id.movie_detail_user_list_drawable_layout),
                    snackBarMsg, Snackbar.LENGTH_LONG).show()
//            final Snackbar snackbar
//            snackbar = Snackbar.make(mUserDrawableLayout.findViewById(R.id.movie_detail_user_list_drawable_layout),
//                    snackBarMsg, Snackbar.LENGTH_LONG)
//            final snackbarView = snackbar.getView()
//            snackbarView.setBackgroundColorolor(ContextCompat.getColor(mContext, R.color.accent))
//            snackbarView.setBackgroundColor(mBackgroundColor)
//            snackbarView.setAlpha(0.6f)
//            final TextView snackbarTextView = snackbarView.findViewById(android.support.design.R.id.snackbar_text) as TextView
//            snackbarTextView.setTextColor(mBodyTextColor)
//            snackbar.show()
        } else {
            LogDisplay.callLog(LOG_TAG,"Something went wrong during user list update.Result value->$result",LogDisplay.UPDATE_USER_LIST_FLAG)
        }
    }
}