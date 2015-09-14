package dlv.nanodegree.popularmovies_2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * Created by daniellujanvillarreal on 9/4/15.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "movies.db";

    private static final String TEXT_TYPE = " TEXT NOT NULL";
    private static final String INTEGER_TYPE = " INTEGER NOT NULL";
    private static final String COMMA_SEP = ",";
    private static final String CASCADE_DELETE = " ON DELETE CASCADE";

    /**************************/
    /****** CREATES     *******/
    /**************************/
    private static final String SQL_CREATE_MOVIES =
            "CREATE TABLE " + MoviesContract.MovieEntry.TABLE_NAME + " (" +
                    MoviesContract.MovieEntry._ID + INTEGER_TYPE +" PRIMARY KEY," +
                    MoviesContract.MovieEntry.COLUMN_SERVER_ID + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_SYNOPSIS + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_RATING + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_DATE + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_SORTING + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.MovieEntry.COLUMN_IS_FAVORITE + INTEGER_TYPE +
                    " )";

    private static final String SQL_CREATE_REVIEWS =
            "CREATE TABLE " + MoviesContract.ReviewEntry.TABLE_NAME + " (" +
                    MoviesContract.ReviewEntry._ID + INTEGER_TYPE +" PRIMARY KEY," +
                    MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + INTEGER_TYPE + COMMA_SEP +
                    MoviesContract.ReviewEntry.COLUMN_AUTHOR + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.ReviewEntry.COLUMN_CONTENT + TEXT_TYPE + COMMA_SEP +
                    " FOREIGN KEY (" + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                    MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry._ID
                    + ")" + CASCADE_DELETE +
                    " )";

    private static final String SQL_CREATE_VIDEOS =
            "CREATE TABLE " + MoviesContract.VideoEntry.TABLE_NAME + " (" +
                    MoviesContract.VideoEntry._ID + INTEGER_TYPE +" PRIMARY KEY," +
                    MoviesContract.VideoEntry.COLUMN_MOVIE_ID + INTEGER_TYPE + COMMA_SEP +
                    MoviesContract.VideoEntry.COLUMN_URL + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.VideoEntry.COLUMN_LANG + TEXT_TYPE + COMMA_SEP +
                    MoviesContract.VideoEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    " FOREIGN KEY (" + MoviesContract.VideoEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                    MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry._ID
                    + ")" + CASCADE_DELETE +
                    " )";

    /**************************/
    /****** DELETES     *******/
    /**************************/
    private static final String SQL_DELETE_MOVIES =
            "DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.TABLE_NAME;

    private static final String SQL_DELETE_REVIEWS =
            "DROP TABLE IF EXISTS " + MoviesContract.ReviewEntry.TABLE_NAME;

    private static final String SQL_DELETE_VIDEOS =
            "DROP TABLE IF EXISTS " + MoviesContract.VideoEntry.TABLE_NAME;

    /**************************/
    /****** METHODS     *******/
    /**************************/
    public MoviesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MoviesDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES);
        db.execSQL(SQL_CREATE_REVIEWS);
        db.execSQL(SQL_CREATE_VIDEOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_MOVIES);
        db.execSQL(SQL_DELETE_REVIEWS);
        db.execSQL(SQL_DELETE_VIDEOS);

        onCreate(db);
    }
}
