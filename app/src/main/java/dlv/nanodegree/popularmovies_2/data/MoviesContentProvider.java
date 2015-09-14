package dlv.nanodegree.popularmovies_2.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import dlv.nanodegree.popularmovies_2.adapters.MainViewPagerAdapter;

/**
 *
 * Created by daniellujanvillarreal on 9/7/15.
 */
public class MoviesContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mOpenHelper;

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int REVIEWS = 200;
    public static final int REVIEWS_WITH_MOVIE = 201;
    public static final int VIDEOS = 300;
    public static final int VIDEOS_WITH_MOVIE = 301;

    private String TAG = getClass().getSimpleName();

    /*******************************/
    /****** URIMATCHER ***************/
    /*******************************/
    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        //create code for each URI
        //movies/
        matcher.addURI(authority, MoviesContract.MOVIES_PATH, MOVIES);
        //movies/id
        matcher.addURI(authority, MoviesContract.MOVIES_PATH+"/#", MOVIE_WITH_ID);
        //movies/reviews
        matcher.addURI(authority, MoviesContract.REVIEWS_PATH, REVIEWS);
        //movies/reviews/id
        matcher.addURI(authority, MoviesContract.REVIEWS_PATH+"/#", REVIEWS_WITH_MOVIE);
        //movies/videos
        matcher.addURI(authority, MoviesContract.VIDEOS_PATH, VIDEOS);
        //movies/videos/id
        matcher.addURI(authority, MoviesContract.VIDEOS_PATH+"/#", VIDEOS_WITH_MOVIE);

        return matcher;
    }

    /*******************************/
    /****** PROVIDER ***************/
    /*******************************/
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri){
        final int match = sUriMatcher.match(uri);

        switch(match){
            case MOVIES: //DIR
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID: //ITEM
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_WITH_MOVIE: //DIR
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case VIDEOS_WITH_MOVIE: //DIR
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:"+uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //given a URI determine the request and query DB
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_WITH_ID:
                //get Movie normally
                returnCursor = getMovieById(mOpenHelper, uri, projection, sortOrder);

                //here we check if movie is favorite... if it is we load the favorite movie instead
                //of the popular/rated movie
                Cursor tempCursor = returnCursor;
                if(tempCursor.moveToFirst()){
                    int index = tempCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_IS_FAVORITE);
                    if(tempCursor.getInt(index) == 1){
                        int indexId = tempCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_SERVER_ID);
                        long serverId = tempCursor.getLong(indexId);

                        String selectionTemp =
                                MoviesContract.MovieEntry.COLUMN_SORTING+" = ? AND "
                                + MoviesContract.MovieEntry.COLUMN_SERVER_ID+" = ?";
                        String selectionArgsTemp[] = new String[]{
                                MoviesContract.SORTING_FAVORITE,
                                Long.toString(serverId)
                        };

                        returnCursor = mOpenHelper.getReadableDatabase().query(
                                MoviesContract.MovieEntry.TABLE_NAME,
                                projection,
                                selectionTemp,
                                selectionArgsTemp,
                                null,
                                null,
                                sortOrder
                        );
                    }
                }
                break;
            case REVIEWS_WITH_MOVIE:
                returnCursor = getReviewsForMovie(mOpenHelper, uri, projection, sortOrder);
                break;
            case VIDEOS_WITH_MOVIE:
                returnCursor = getVideosForMovie(mOpenHelper, uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d("ContentProvider", "setting notification uri :: "+uri);
        return returnCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long newId = 0;

        switch (match) {
            case MOVIES:
                newId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null,  values);
                Uri newUri = MoviesContract.MovieEntry.buildMovieUri(newId);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
        }
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {

            case MOVIES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    if(values != null) {
                        deleteMovies(db, values[0]);
                    }
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        Log.d("ContentProvider", "inserted movie :: "
                                + MoviesContract.MovieEntry.buildMovieUri(_id));
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d("ContentProvider-Movies", "notify change uri :: " + uri);

                return returnCount;

            case REVIEWS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d("ContentProvider-Reviews", "notify change uri :: " + uri);
                return returnCount;

            case VIDEOS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case MOVIE_WITH_ID:
                selection = MoviesContract.MovieEntry._ID+" = ?";
                selectionArgs = new String[]{
                        Long.toString(MoviesContract.MovieEntry.getIdFromUri(uri))};
                db.execSQL("PRAGMA foreign_keys=ON");
                returnCount = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updatedRows = 0;

        switch (match) {
            case MOVIE_WITH_ID:
                //updating movies happens only when new favs are added... thus id on uri is serverId from movie
                long serverId = MoviesContract.MovieEntry.getIdFromUri(uri);
                selection = MoviesContract.MovieEntry.COLUMN_SERVER_ID + " = ?";
                selectionArgs = new String[]{Long.toString(serverId)};

                updatedRows = db.update(
                        MoviesContract.MovieEntry.TABLE_NAME
                        , values
                        , selection
                        , selectionArgs);
                Log.i(TAG, "updated movies :: "+updatedRows);
                getContext().getContentResolver().notifyChange(uri, null);
                return updatedRows;
        }
        return 0;
    }

    /*****************************/
    /****** QUERY HELPERS ********/
    /*****************************/
    //Movie detail -- return single movie
    private Cursor getMovieById(SQLiteOpenHelper db, Uri uri, String[] projection, String sortOrder){
        long movieId = MoviesContract.MovieEntry.getIdFromUri(uri);
        String selection =  MoviesContract.MovieEntry._ID + " = ? ";
        return db.getReadableDatabase().query(
                MoviesContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{Long.toString(movieId)},
                null,
                null,
                sortOrder
        );
    }
    //Reviews of Movie
    private Cursor getReviewsForMovie(SQLiteOpenHelper db, Uri uri, String[] projection, String sortOrder){
        long movieId = MoviesContract.ReviewEntry.getIdFromUri(uri);
        String selection =  MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";
        return db.getReadableDatabase().query(
                MoviesContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{Long.toString(movieId)},
                null,
                null,
                sortOrder
        );
    }
    //Videos of Movie
    private Cursor getVideosForMovie(SQLiteOpenHelper db, Uri uri, String[] projection, String sortOrder){
        long movieId = MoviesContract.VideoEntry.getIdFromUri(uri);
        String selection =  MoviesContract.VideoEntry.COLUMN_MOVIE_ID + " = ? ";
        return db.getReadableDatabase().query(
                MoviesContract.VideoEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{Long.toString(movieId)},
                null,
                null,
                sortOrder
        );
    }

    //delete movies based on sorting... videos&reviews removed with ON DELETE CASCADE
    private void deleteMovies(SQLiteDatabase db, ContentValues value){
        String sorting  = value.getAsString(MoviesContract.MovieEntry.COLUMN_SORTING);
        String selection = null;
        String[] selectionArgs = new String[1];
        if(sorting.equals(MoviesContract.SORTING_POPULAR)){
            selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
            selectionArgs[0] = MoviesContract.SORTING_POPULAR;
        }else if(sorting.equals(MoviesContract.SORTING_RATED)){
            selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
            selectionArgs[0] = MoviesContract.SORTING_RATED;
        }else if(sorting.equals(MoviesContract.SORTING_FAVORITE)){
            selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
            selectionArgs[0] = MoviesContract.SORTING_RATED;
        }
        db.execSQL("PRAGMA foreign_keys=ON");
        int deleted = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
        Log.d(TAG, "deleted "+sorting+" MOVIES ::"+selectionArgs[0]+"::"+deleted);
    }


}
