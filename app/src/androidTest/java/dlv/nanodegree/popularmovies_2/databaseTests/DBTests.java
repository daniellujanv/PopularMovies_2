package dlv.nanodegree.popularmovies_2.databaseTests;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.data.MoviesDBHelper;

/**
 *
 * Created by daniellujanvillarreal on 9/4/15.
 */
public class DBTests extends AndroidTestCase {


    public DBTests() {
        super();
    }

    public void testDB() throws Throwable{
        //tables we want to look for
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.ReviewEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.VideoEntry.TABLE_NAME);

//        mContext.deleteDatabase(MoviesDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();
        //assert opened
        assertTrue("Error: Could not open Database", db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );
        c.close();

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without the entry tables",
                tableNameHashSet.isEmpty());
        db.close();

        //TEST CRUD (no updates -- we don´t need updates in the app)
        insertStuff();
        queryStuff();
        deleteStuff();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void insertStuff(){

        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();

        //insert movie and get newly created ID in return
        ContentValues movie = getPopularMovieCV();
        long movieId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, movie);
        assertTrue("Error: DB could not create Movie in DB", movieId != -1);

        //insert review... if -1 insert failed
        ContentValues review = getReviewCV(movieId);
        long reviewId = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, review);
        assertTrue("Error: DB could not create Review in DB", reviewId != -1);

        // insert video
        ContentValues video = getVideoCV(movieId);
        long videoId = db.insert(MoviesContract.VideoEntry.TABLE_NAME, null, video);
        assertTrue("Error: DB could not create Video in DB", videoId != -1);

        db.close();
    }

    public void queryStuff(){
        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();

        //query&validate movies
        ContentValues movie = getPopularMovieCV();
        Cursor cMovie = db.query(MoviesContract.MovieEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No records returned from Movie query", cMovie.moveToFirst());
        validateCurrentRecord("Error: Movie query validation failed", cMovie, movie);
        //get movie id for next queries
        long movieId = cMovie.getInt(cMovie.getColumnIndex(MoviesContract.MovieEntry._ID));
        cMovie.close();

        //query&validate review
        ContentValues review = getReviewCV(movieId);
        Cursor cReview = db.query(MoviesContract.ReviewEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No records returned from Review query", cReview.moveToFirst());
        validateCurrentRecord("Error: Review query validation failed", cReview, review);
        cReview.close();

        //query&validate video
        ContentValues video = getVideoCV(movieId);
        Cursor cVideo = db.query(MoviesContract.VideoEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No records returned from Video query", cVideo.moveToFirst());
        validateCurrentRecord("Error: Video query validation failed", cVideo, video);
        cVideo.close();

        db.close();
    }

    public void deleteStuff(){
        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();
        //test delete movies
        int moviesDeleted = db.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null);
        assertTrue("Error: Movies deleted != 1", moviesDeleted == 1);
        //test delete revies

        int reviewsDeleted = db.delete(MoviesContract.ReviewEntry.TABLE_NAME, null, null);
        assertTrue("Error: Reviews deleted != 1", reviewsDeleted == 1);
        //Test delete videos

        int videosDeleted = db.delete(MoviesContract.VideoEntry.TABLE_NAME, null, null);
        assertTrue("Error: Videos deleted != 1", videosDeleted == 1);

        db.close();
    }

    //check that values are as expected
    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static ContentValues getPopularMovieCV(){
        /**
         *
         public static final String COLUMN_SERVER_ID = "server_id";
         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_THUMB_ARRAY = "thumb_array";
         public static final String COLUMN_POSTER_ARRAY = "poster_array";
         public static final String COLUMN_RATING = "rating";
         public static final String COLUMN_DATE = "date";
         public static final String COLUMN_SORTING = "sorting";
         */
        ContentValues movieCv = new ContentValues();
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SERVER_ID, "000001910");

        movieCv.put(MoviesContract.MovieEntry.COLUMN_TITLE, "PARTY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "best movie ever");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY, "THISISATHUMBARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY, "THISISTHEPOSTERARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_RATING, "9.9999");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_DATE, "10-10-10");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SORTING, MoviesContract.SORTING_POPULAR);
        return movieCv;
    }

    public static ContentValues getRatedMovieCV(){
        /**
         *
         public static final String COLUMN_SERVER_ID = "server_id";
         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_THUMB_ARRAY = "thumb_array";
         public static final String COLUMN_POSTER_ARRAY = "poster_array";
         public static final String COLUMN_RATING = "rating";
         public static final String COLUMN_DATE = "date";
         public static final String COLUMN_SORTING = "sorting";
         */
        ContentValues movieCv = new ContentValues();
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SERVER_ID, "000001910");

        movieCv.put(MoviesContract.MovieEntry.COLUMN_TITLE, "PARTY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "best movie ever");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY, "THISISATHUMBARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY, "THISISTHEPOSTERARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_RATING, "9.9999");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_DATE, "10-10-10");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SORTING, MoviesContract.SORTING_RATED);
        return movieCv;
    }

    public static ContentValues getFavoriteMovieCV(){
        /**
         *
         public static final String COLUMN_SERVER_ID = "server_id";
         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_THUMB_ARRAY = "thumb_array";
         public static final String COLUMN_POSTER_ARRAY = "poster_array";
         public static final String COLUMN_RATING = "rating";
         public static final String COLUMN_DATE = "date";
         public static final String COLUMN_SORTING = "sorting";
         */
        ContentValues movieCv = new ContentValues();
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SERVER_ID, "000001910");

        movieCv.put(MoviesContract.MovieEntry.COLUMN_TITLE, "PARTY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "best movie ever");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY, "THISISATHUMBARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY, "THISISTHEPOSTERARRAY");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_RATING, "9.9999");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_DATE, "10-10-10");
        movieCv.put(MoviesContract.MovieEntry.COLUMN_SORTING, MoviesContract.SORTING_FAVORITE);
        return movieCv;
    }

    public static ContentValues getReviewCV(long movieId){
        /**
         *
         public static final String COLUMN_MOVIE_ID ="movie_id";
         public static final String COLUMN_AUTHOR ="author";
         public static final String COLUMN_CONTENT = "content";
         */
        ContentValues reviewCv = new ContentValues();
        reviewCv.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
        reviewCv.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "The guy");
        reviewCv.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "THIS A REVIEW from --- the guy ---");
        return reviewCv;
    }

    public static ContentValues getVideoCV(long movieId){
        /**
         *
         public static final String COLUMN_MOVIE_ID = "movie_id";
         public static final String COLUMN_URL = "url";
         public static final String COLUMN_LANG = "lang";
         public static final String COLUMN_NAME = "name";
         */
        ContentValues videoCv = new ContentValues();
        videoCv.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, movieId);
        videoCv.put(MoviesContract.VideoEntry.COLUMN_URL, "www.party.com");
        videoCv.put(MoviesContract.VideoEntry.COLUMN_LANG, "en");
        videoCv.put(MoviesContract.VideoEntry.COLUMN_NAME, "BEST TRAILER EVER");
        return videoCv;
    }

    public void deleteAllRecords(){
        SQLiteDatabase db = new MoviesDBHelper(mContext).getWritableDatabase();
        db.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null);
        db.delete(MoviesContract.ReviewEntry.TABLE_NAME, null, null);
        db.delete(MoviesContract.VideoEntry.TABLE_NAME, null, null);

        db.close();
    }
}
