package dlv.nanodegree.popularmovies_2.databaseTests;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.util.Log;

import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.data.MoviesDBHelper;

/**
 * Created by daniellujanvillarreal on 9/7/15.
 */
public class ContentProviderTests extends AndroidTestCase {

    private int numRecordsToCreate = 20;
    private ContentResolver mContentResolver;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
        mContentResolver = getContext().getContentResolver();
    }


    public void deleteAllRecords(){
        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();
        Log.d(TAG, "deleted Movies :: " + db.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null));
        Log.d(TAG, "deleted Reviews :: " + db.delete(MoviesContract.ReviewEntry.TABLE_NAME, null, null));
        Log.d(TAG, "deleted Videos :: " + db.delete(MoviesContract.VideoEntry.TABLE_NAME, null, null));
        db.close();
    }

    public void testBulkInset() throws Exception {
        //test movies (popular, rated) bulk insert
        ContentValues[] moviesPopularCVs = getPopularMoviesCVs();
        int insertedPopularMovies = mContentResolver.bulkInsert(
                MoviesContract.MovieEntry.CONTENT_URI,
                moviesPopularCVs
        );
        ContentValues[] moviesRatedCVs = getRatedMoviesCVs();
        int insertedRatedMovies = mContentResolver.bulkInsert(
                MoviesContract.MovieEntry.CONTENT_URI,
                moviesRatedCVs
        );
        assertEquals("Error: Unexpected number of Popular movies inserted", numRecordsToCreate, insertedPopularMovies);
        assertEquals("Error: Unexpected number of Rated movies inserted", numRecordsToCreate, insertedRatedMovies);
        int movieId = 0; // since in SetUp we delete database... we can be certain that first movieId == 0
        //test reviews bulk insert with 1st movie inserted
        ContentValues[] reviewsCvs = getReviewsCVs(movieId);
        int insertedReviews = mContentResolver.bulkInsert(
                MoviesContract.ReviewEntry.CONTENT_URI,
                reviewsCvs
        );
        assertEquals("Error: Unexpected number of reviews inserted", numRecordsToCreate, insertedReviews);
        //test videos bulk insert with 1st movie inserted
        ContentValues[] videosCvs = getVideosCVs(movieId);
        int insertedVideos = mContentResolver.bulkInsert(
                MoviesContract.VideoEntry.CONTENT_URI,
                videosCvs
        );
        assertEquals("Error: Unexpected number of videos inserted", numRecordsToCreate, insertedVideos);
        //Test fetching stuff
        //** we do not test delete ALL because before bulk inserting we delete everything
        queryStuff();
        deleteAllRecords();

        /******** tests favorites *************/
        ContentValues favoriteMovie = getFavoriteMovieCV();
        Uri newFavMovie = mContentResolver.insert(MoviesContract.MovieEntry.CONTENT_URI, favoriteMovie);
        assertNotNull("Error: Failed to insert favorite movie",newFavMovie);
        int deleledFav = mContentResolver.delete(newFavMovie, null, null);
        assertEquals("Error: Unexpected number of deleted movies", 1, deleledFav);
    }

    public void queryStuff() throws Exception{
        //query Popular Movies
        String selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
        String[] selectionArgs =  new String[]{MoviesContract.SORTING_POPULAR};
        Cursor moviesPopularCursor = mContentResolver.query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
        );
        validateCursor("Error: inserted Popular movie values not as expected"
                , moviesPopularCursor, DBTests.getPopularMovieCV());

        //query HighestRated Movies
        selectionArgs[0] =  MoviesContract.SORTING_RATED;
        Cursor moviesRatedCursor = mContentResolver.query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
        );
        validateCursor("Error: inserted HRated movie values not as expected"
                , moviesRatedCursor, DBTests.getRatedMovieCV());

        //query Reviews
        long movieId = 0; //expected id 0 --> which is a Popular Movie
        Uri reviewsForMovie = MoviesContract.ReviewEntry.buildQueryUri(movieId);
        Cursor reviewsCursor = mContentResolver.query(
                reviewsForMovie,
                null,
                null,
                null,
                null
        );
        validateCursor("Error: inserted review values not as expected"
                , reviewsCursor, DBTests.getReviewCV(movieId));

        //query Videos
        Uri videosForMovie = MoviesContract.VideoEntry.buildQueryUri(movieId);
        Cursor videosCursor = mContentResolver.query(
                videosForMovie,
                null,
                null,
                null,
                null
        );
        validateCursor("Error: inserted video values not as expected"
                , videosCursor, DBTests.getVideoCV(movieId));
    }

    /************** helpers *****************/

    public ContentValues[] getPopularMoviesCVs(){
        ContentValues[] result = new ContentValues[numRecordsToCreate];
        for(int i= 0; i< numRecordsToCreate; i++){
            result[i] = DBTests.getPopularMovieCV();
        }
        return result;
    }

    public ContentValues[] getRatedMoviesCVs(){
        ContentValues[] result = new ContentValues[numRecordsToCreate];
        for(int i= 0; i< numRecordsToCreate; i++){
            result[i] = DBTests.getRatedMovieCV();
        }
        return result;
    }

    public ContentValues getFavoriteMovieCV(){
           return DBTests.getFavoriteMovieCV();
    }


    public ContentValues[] getReviewsCVs(long movieId){
        ContentValues[] result = new ContentValues[numRecordsToCreate];
        for(int i=0; i<numRecordsToCreate; i++){
            result[i] = DBTests.getReviewCV(movieId);
        }
        return result;
    }

    public ContentValues[] getVideosCVs(long movieId){
        ContentValues[] result = new ContentValues[numRecordsToCreate];
        for(int i=0; i<numRecordsToCreate; i++){
            result[i] = DBTests.getVideoCV(movieId);
        }
        return result;
    }

    public void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        String object = "";
        if(error.contains("movie")){
            object = "Movies";
        }else if(error.contains("review")){
            object = "Reviews";
        }else{
            object = "Videos";
        }


        Log.d("validating", object);
        assertTrue("Empty cursor returned in " + object, valueCursor.moveToFirst());
        int i = 0;
        do {
            DBTests.validateCurrentRecord(error, valueCursor, expectedValues);
            i++;
        }while(valueCursor.moveToNext());
        valueCursor.close();
        assertEquals("Fetched "+object+" not equal to inserted ", numRecordsToCreate, i);
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
