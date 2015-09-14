package dlv.nanodegree.popularmovies_2.classes;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 * Created by daniellujanvillarreal on 9/8/15.
 */
public class Review {
    /**
     *
     * _ID
     public static final String COLUMN_MOVIE_ID ="movie_id";
     public static final String COLUMN_AUTHOR ="author";
     public static final String COLUMN_CONTENT = "content";
     */
    private long mMovieId;
    private String mAuthor;
    private String mContent;

    private int COLUMN_MOVIE_ID_INDEX = 0;
    private int COLUMN_AUTHOR_INDEX = 1;
    private int COLUMN_CONTENT_INDEX = 2;

    public Review(Cursor cursor){
        mMovieId = cursor.getLong(COLUMN_MOVIE_ID_INDEX);
        mAuthor = cursor.getString(COLUMN_AUTHOR_INDEX);
        mContent = cursor.getString(COLUMN_CONTENT_INDEX);
    }

    public Review(JSONObject json) throws JSONException{
        mMovieId = json.getLong(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID);
        mAuthor = json.getString(MoviesContract.ReviewEntry.COLUMN_AUTHOR);
        mContent = json.getString(MoviesContract.ReviewEntry.COLUMN_CONTENT);
    }

    public ContentValues getCVs(long newMovieID){
        ContentValues result = new ContentValues();
        result.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, newMovieID);
        result.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, mAuthor);
        result.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, mContent);
        return result;
    }

    /********** GETTERS **************/
    public long getMovieId() {
        return mMovieId;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmContent() {
        return mContent;
    }

    /********** SETTERS **************/
    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }
    public void setMovieId(long mId) {
        this.mMovieId = mId;
    }
    public void setmContent(String mContent) {
        this.mContent = mContent;
    }
}
