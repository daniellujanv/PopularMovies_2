package dlv.nanodegree.popularmovies_2.classes;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 *  Movie Object
 * Created by DanielLujanApps on Monday10/08/15.
 */
public class Movie {

    public static String MOVIE_KEY = "movie";

    private long mId;
    private URL mThumbUrl;
    private URL mPosterUrl;
    private String mTitle;
    private double mRating;
    private String mSynopsis;
    private String mStringReleaseDate;
    public static String dateFormat = "dd MMM yyyy";
    private String dateFormatServer = "yyyy-MM-dd";
    private byte[] thumbnailByteArray;
    private byte[] posterByteArray;
    private boolean loadedYet = false;
    private String mSorting;
    private long mServerId;
    private boolean mIsFavorite;

    public Movie(JSONObject json) throws JSONException, MalformedURLException {
        mThumbUrl = new URL("http://image.tmdb.org/t/p/w185/"+json.get("backdrop_path"));
        mPosterUrl = new URL("http://image.tmdb.org/t/p/w185/"+json.get("poster_path"));
        mServerId = json.getLong("id");
        mTitle = json.getString("original_title");
        mRating = json.getDouble("vote_average");
        mSynopsis = json.getString("overview");
        mStringReleaseDate = json.getString("release_date");
        mSorting = json.getString(MoviesContract.MOVIE_SORTING);
        mIsFavorite = false;
    }

    public Movie(ContentValues contentValues) {
        thumbnailByteArray = contentValues.getAsByteArray(MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY);
        posterByteArray = contentValues.getAsByteArray(MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY);
        mServerId = contentValues.getAsLong("id");
        mTitle = contentValues.getAsString("original_title");
        mRating = contentValues.getAsDouble("vote_average");
        mSynopsis = contentValues.getAsString("overview");
        mStringReleaseDate = contentValues.getAsString("release_date");
        mSorting = contentValues.getAsString(MoviesContract.MOVIE_SORTING);
        mIsFavorite = false;
    }

    public Movie(Cursor cursor, boolean inDetails){
        if (inDetails){
            int ID_INDEX = 0;
            int COLUMN_SERVER_ID_INDEX = 1;
            int COLUMN_TITLE_INDEX = 2;
            int COLUMN_SYNOPSIS_INDEX = 3;
            int COLUMN_THUMB_ARRAY_INDEX = 4;
            int COLUMN_RATING_INDEX = 5;
            int COLUMN_DATE_INDEX = 6;
            int COLUMN_SORTING_INDEX = 7;
            int COLUMN_POSTER_ARRAY_INDEX = 8;
            int COLUMN_IS_FAVORITE = 9;

            mId = cursor.getLong(ID_INDEX);
            mServerId = cursor.getLong(COLUMN_SERVER_ID_INDEX);
            mTitle = cursor.getString(COLUMN_TITLE_INDEX);
            mSynopsis = cursor.getString(COLUMN_SYNOPSIS_INDEX);
            thumbnailByteArray = cursor.getBlob(COLUMN_THUMB_ARRAY_INDEX);
            mRating = cursor.getDouble(COLUMN_RATING_INDEX);
            mStringReleaseDate = cursor.getString(COLUMN_DATE_INDEX);
            mSorting = cursor.getString(COLUMN_SORTING_INDEX);
            posterByteArray = cursor.getBlob(COLUMN_POSTER_ARRAY_INDEX);
            mIsFavorite = (cursor.getInt(COLUMN_IS_FAVORITE) == 1);
        }else{
            int COLUMN_ID_INDEX = 0;
            int COLUMN_ID_POSTER = 1;
            mId = cursor.getLong(COLUMN_ID_INDEX);
            posterByteArray = cursor.getBlob(COLUMN_ID_POSTER);
        }
    }

    /*************** GETTERS ********************/



    public ContentValues getCVs(){
        ContentValues result = new ContentValues();
/**
 *
 public static final String COLUMN_SERVER_ID = "server_id";
 public static final String COLUMN_TITLE = "title";
 public static final String COLUMN_SYNOPSIS = "synopsis";
 public static final String COLUMN_THUMB_ARRAY = "thumb_array";
 public static final String COLUMN_POSTER_ARRAY = "poster_array";
 public static final String COLUMN_RATING = "rating";
 public static final String COLUMN_DATE = "date";
 //Sorting of movie (popular, rating, favs)
 public static final String COLUMN_SORTING = "sorting";
 */
        result.put(MoviesContract.MovieEntry.COLUMN_SERVER_ID, mServerId);
        result.put(MoviesContract.MovieEntry.COLUMN_TITLE, mTitle);
        result.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, mSynopsis);
        result.put(MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY, thumbnailByteArray);
        result.put(MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY, posterByteArray);
        result.put(MoviesContract.MovieEntry.COLUMN_RATING, mRating);
        result.put(MoviesContract.MovieEntry.COLUMN_DATE, mStringReleaseDate);
        result.put(MoviesContract.MovieEntry.COLUMN_SORTING, mSorting);
        result.put(MoviesContract.MovieEntry.COLUMN_IS_FAVORITE, mIsFavorite);

        return result;
    }

    public URL getThumbnailUrl(){
        return mThumbUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public double getRating() {
        return mRating;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getFormatedReleaseDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatServer);
        Date releaseDate = simpleDateFormat.parse(mStringReleaseDate, new ParsePosition(0));
        simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(releaseDate);
    }

    public String getReleaseDate() {
        return mStringReleaseDate;
    }


    public URL getmPosterUrl() {
        return mPosterUrl;
    }

    public byte[] getThumbnailByteArray(){
        return thumbnailByteArray;
    }

    public byte[] getPosterByteArray(){
        return posterByteArray;
    }

    public String getSorting(){
        return mSorting;
    }

    public long getmServerId() {
        return mServerId;
    }

    public long getmId() {
        return mId;
    }

    /*************** SETTERS *************************/
    public void setThumbnailByteArray(byte[] array) {
        thumbnailByteArray = array;
    }

    public void setPosterByteArray(byte[] array) {
        posterByteArray = array;
    }

    public void setSorting(String sorting){
        mSorting = sorting;
    }

    public boolean ismIsFavorite() {
        return mIsFavorite;
    }

    public void setmIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }
}
