package dlv.nanodegree.popularmovies_2.classes;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 * Created by daniellujanvillarreal on 9/8/15.
 */
public class Trailer {

    /***
     *
     public static final String COLUMN_MOVIE_ID = "movie_id";
     public static final String COLUMN_URL = "url";
     public static final String COLUMN_LANG = "lang";
     public static final String COLUMN_NAME = "name";
     */
    private long mMovieId;
    private String mUrl;
    private String mLang;
    private String mName;


    private int COLUMN_MOVIE_ID_INDEX = 0;
    private int COLUMN_LAN_INDEX = 1;
    private int COLUMN_NAME_INDEX = 2;
    private int COLUMN_URL_INDEX = 3;

    public Trailer(Cursor cursor){
        mMovieId = cursor.getLong(COLUMN_MOVIE_ID_INDEX);
        mLang = cursor.getString(COLUMN_LAN_INDEX);
        mName = cursor.getString(COLUMN_NAME_INDEX);
        mUrl = cursor.getString(COLUMN_URL_INDEX);
    }

    public Trailer(JSONObject json) throws JSONException{
        mMovieId = json.getLong(MoviesContract.VideoEntry.COLUMN_MOVIE_ID);
        mUrl = json.getString("key");//https://www.youtube.com/watch?v=FRDdRto_3SA
        mLang = json.getString("iso_639_1");
        mName = json.getString(MoviesContract.VideoEntry.COLUMN_NAME);
    }//iso_639_1

    public ContentValues getCVs(long newMovieId){
        ContentValues result = new ContentValues();
        result.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, newMovieId);
        result.put(MoviesContract.VideoEntry.COLUMN_URL, mUrl);
        result.put(MoviesContract.VideoEntry.COLUMN_LANG, mLang);
        result.put(MoviesContract.VideoEntry.COLUMN_NAME, mName);
        return result;
    }

    public long getmMovieId() {
        return mMovieId;
    }

    public void setmMovieId(long mMovieId) {
        this.mMovieId = mMovieId;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getmLang() {
        return mLang;
    }

    public void setmLang(String mLang) {
        this.mLang = mLang;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
