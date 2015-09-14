package dlv.nanodegree.popularmovies_2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daniellujanvillarreal on 9/4/15.
 */
public final class MoviesContract {

    //content authority for provider
    public static final String CONTENT_AUTHORITY = "dlv.nanodegree.popularmovies_2.provider";
    //base content uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    //paths for each entry == table names
    public static final String MOVIES_PATH = "movies";
    public static final String REVIEWS_PATH = "reviews";
    public static final String VIDEOS_PATH =  "videos";

    //sorting values
    public static final String MOVIE_SORTING = "movie_sorting";
    public static final String SORTING_POPULAR = "popular";
    public static final String SORTING_RATED = "rated";
    public static final String SORTING_FAVORITE = "favorite";

    public MoviesContract(){}

    public static abstract class MovieEntry implements BaseColumns {
        /********* CONTENT PROVIDER ***************/
        //content://auth//movies
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(MOVIES_PATH).build();
        // dir.auth.movies
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIES_PATH;
        // item.auth.movies
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIES_PATH;

        //for single movie query
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //get Id from Uri
        public static long getIdFromUri(Uri uri){
            //0==movies/1==movie_id
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        /********* DB ***************/
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_THUMB_ARRAY = "thumb_array";
        public static final String COLUMN_POSTER_ARRAY = "poster_array";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_DATE = "date";
        //Sorting of movie (popular, rating, favs)
        public static final String COLUMN_SORTING = "sorting";
        public static final String COLUMN_IS_FAVORITE = "favorite";


    }

    public static abstract class ReviewEntry implements BaseColumns {

        /********* CONTENT PROVIDER ***************/
        //content://auth//movies/reviews/
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(REVIEWS_PATH).build();
        // dir.auth.movies
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + REVIEWS_PATH;
        // item.auth.movies
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + REVIEWS_PATH;

        //for single movie query
        public static Uri buildQueryUri(long movieId) {
            //content://auth//reviews/movie_id
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        //get Id from Uri
        public static long getIdFromUri(Uri uri){
            //0==reviews/1==movie_id
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        /********* DB ***************/
        public static final String TABLE_NAME = "review";
        public static final String COLUMN_MOVIE_ID ="movie_id";
        public static final String COLUMN_AUTHOR ="author";
        public static final String COLUMN_CONTENT = "content";
    }

    public static abstract class VideoEntry implements BaseColumns {

        /********* CONTENT PROVIDER ***************/
        //content://auth//movies/videos/
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(VIDEOS_PATH).build();
        // dir.auth.movies
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + VIDEOS_PATH;
        // item.auth.movies
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + VIDEOS_PATH;

        //for single movie query
        public static Uri buildQueryUri(long movieId) {
            //content://auth//videos/movie_id
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        //get Id from Uri
        public static long getIdFromUri(Uri uri){
            //0==videos/1==movie_id
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        /********* DB ***************/

        public static final String TABLE_NAME = "video";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_LANG = "lang";
        public static final String COLUMN_NAME = "name";
    }
}
