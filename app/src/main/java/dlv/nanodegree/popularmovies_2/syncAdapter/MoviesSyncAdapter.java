package dlv.nanodegree.popularmovies_2.syncAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.classes.Review;
import dlv.nanodegree.popularmovies_2.classes.Trailer;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.utils.Utils;

/**
 * Handle data transfer between TheMovieDB and this awesome app
 * Created by daniellujanvillarreal on 9/8/15.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private ContentResolver mContentResolver;

    private String TAG = getClass().getSimpleName();
    public final static String SYNC_MODE = "sync_mode";
    public final static String SYNC_POPULAR = "popular";
    public final static String SYNC_RATED = "rated";
    public final static String SYNC_FAVS = "favorites";
    public final static String SYNC_MOVIES = "full";
    public final static String SYNC_REVIEWS_TRAILERS = "reviews_trailes";

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        Log.i(TAG, "creating sync adapter");
    }

    public MoviesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        Log.i(TAG, "creating sync adapter");
    }

    public static void syncImmediately(Context context, String syncMode) {
        Log.i("syncImmediately", "requesting sync");
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putString(SYNC_MODE, syncMode);
        String authority = context.getString(R.string.content_authority);
        Account account = getSyncAccount(context);

        ContentResolver.requestSync(account, authority, extras);
        Log.i("syncImmediately", "sync requested");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority
            , ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync");
        String syncMode = extras.getString(SYNC_MODE);
        String apiKey = DummyAuthenticator.getKey();
        ArrayList<Long> favorites;
        if(syncMode != null) {
            switch (syncMode) {
                case SYNC_POPULAR:
                    Log.i(TAG, "sync popular");
                    favorites = getFavorites();
                    syncPopularMovies(apiKey, favorites);
                    syncReviews(apiKey, SYNC_POPULAR);
                    syncVideos(apiKey, SYNC_POPULAR);
                    break;
                case SYNC_RATED:
                    Log.i(TAG, "sync rated");
                    favorites = getFavorites();
                    syncHighestRatedMovies(apiKey, favorites);
                    syncReviews(apiKey, SYNC_RATED);
                    syncVideos(apiKey, SYNC_RATED);
                    break;
                case SYNC_FAVS:
                    break;
                case SYNC_MOVIES:
                    Log.i(TAG, "sync full");
                    favorites = getFavorites();
                    syncPopularMovies(apiKey, favorites);
                    syncHighestRatedMovies(apiKey, favorites);
                    syncReviews(apiKey, null);
                    syncVideos(apiKey, null);
                    break;
                default:
                    favorites = getFavorites();
                    syncPopularMovies(apiKey, favorites);
                    syncHighestRatedMovies(apiKey, favorites);
                    syncReviews(apiKey, null);
                    syncVideos(apiKey, null);
                    Log.i(TAG, "sync NONE");
                    break;
            }
        }else{
            Log.i(TAG, "automatic sync -- sync full");
            favorites = getFavorites();
            syncPopularMovies(apiKey, favorites);
            syncHighestRatedMovies(apiKey, favorites);
            syncReviews(apiKey, null);
            syncVideos(apiKey, null);
            showToast("Populating Movies... this might take a while...");
        }
        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i(TAG, "end onPerformSync");
    }

    private void syncPopularMovies(String apiKey, ArrayList<Long> favorites) {
        Log.i(TAG, "doing sync popular");

        //fetch popular
        try {
            URL urlPopular = new URL("http://api.themoviedb.org/3/movie/popular?api_key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) urlPopular.openConnection();
//            conn.setReadTimeout(10000 /* milliseconds */);
//            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            InputStream inputStream = conn.getInputStream();
            ArrayList<Movie> alMovies = Utils.stringToMovies(Utils.readIt(inputStream)
                    , MoviesContract.SORTING_POPULAR);

            for (int i = 0; i < alMovies.size(); i++) {
                try {
                    /********** POSTER ******************/
                    //load movie poster
                    URL imageUrl = alMovies.get(i).getmPosterUrl();
                    InputStream inputStreamPoster = null;
                    inputStreamPoster = imageUrl.openStream();
                    Bitmap bitmapPoster = BitmapFactory.decodeStream(inputStreamPoster, null, null);
                    inputStreamPoster.close();
                    //poster
                    ByteArrayOutputStream outputStreamPoster = new ByteArrayOutputStream();
                    bitmapPoster.compress(Bitmap.CompressFormat.PNG, 100, outputStreamPoster);
                    byte[] byteArray = outputStreamPoster.toByteArray();
                    alMovies.get(i).setPosterByteArray(byteArray);

                    /********* THUMB ******************/
                    //load movie thumbnail
                    imageUrl = alMovies.get(i).getThumbnailUrl();
                    InputStream inputStreamThumb = null;
                    inputStreamThumb = imageUrl.openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumb, null, null);
                    inputStreamThumb.close();
                    //SAVE THUMBNAIL
                    ByteArrayOutputStream outputStreamThumb = new ByteArrayOutputStream();
                    bitmapThumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStreamThumb);
                    byteArray = null;
                    byteArray = outputStreamThumb.toByteArray();
                    alMovies.get(i).setThumbnailByteArray(byteArray);
                    outputStreamThumb.close();
                    Log.i(TAG, "movie :: " + i);
                    /********* IS FAVORITE **************/
                    if( favorites.contains(alMovies.get(i).getmServerId())){
                        alMovies.get(i).setmIsFavorite(true);
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            storeMovies(alMovies);
//            syncReviews(alMovies, apiKey);
//            syncVideos(alMovies, apiKey);
        } catch (IOException e) {
            showToast("Error fetching movies. Trying again!!");
            e.printStackTrace();
//            syncPopularMovies(apiKey, favorites);
        }

    }

    private void syncHighestRatedMovies(String apiKey, ArrayList<Long> favorites) {
        Log.i(TAG, "doing sync rated");
        //fetch highest rated
        try {
            URL urlPopular = new URL("http://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) urlPopular.openConnection();
//            conn.setReadTimeout(10000 /* milliseconds */);
//            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            InputStream inputStream = conn.getInputStream();

            // Convert the InputStream into a string
            ArrayList<Movie> alMovies = Utils.stringToMovies(Utils.readIt(inputStream),
                    MoviesContract.SORTING_RATED);

            for (int i = 0; i < alMovies.size(); i++) {
                try {
                    /********** POSTER ******************/
                    //load movie poster
                    URL imageUrl = alMovies.get(i).getmPosterUrl();
                    inputStream = null;
                    inputStream = imageUrl.openStream();
                    Bitmap bitmapPoster = BitmapFactory.decodeStream(inputStream, null, null);
                    //poster
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmapPoster.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    alMovies.get(i).setPosterByteArray(byteArray);

                    /********* THUMB ******************/
                    //load movie thumbnail
                    imageUrl = alMovies.get(i).getThumbnailUrl();
                    inputStream = null;
                    inputStream = imageUrl.openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStream, null, null);
                    inputStream.close();

                    //SAVE THUMBNAIL
                    outputStream.reset();
                    outputStream = new ByteArrayOutputStream();
                    bitmapThumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byteArray = null;
                    byteArray = outputStream.toByteArray();
                    alMovies.get(i).setThumbnailByteArray(byteArray);
                    outputStream.close();

                    /********* IS FAVORITE **************/
                    if( favorites.contains(alMovies.get(i).getmServerId())){
                        alMovies.get(i).setmIsFavorite(true);
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
//            //TODO sync reviews&videos
//            syncReviews(alMovies, apiKey);
//            syncVideos(alMovies, apiKey);
            storeMovies(alMovies);
        } catch (IOException e) {
//            publishProgress("Error fetching movies. Try again later.");
            showToast("Error fetching movies. Trying again!!");
            e.printStackTrace();
//            syncHighestRatedMovies(apiKey, favorites);
//            return null;
        }
    }

    private void syncReviews(String apiKey, String sync_mode){
        Log.i(TAG, "sync reviews");
        ArrayList<Movie> alMovies = getStoredMovies(sync_mode);
        for(int i = 0; i<alMovies.size(); i++){
            long serverId = alMovies.get(i).getmServerId();
            long movieId = alMovies.get(i).getmId();
            try{
                URL urlPopular = new URL(
                        "http://api.themoviedb.org/3/movie/"+serverId+"/reviews?api_key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) urlPopular.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Accept", "application/json");

                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                while(response == 429) {
                    Log.d(TAG, "The response is: " + response);
                    Thread.sleep(2000);
                    response = conn.getResponseCode();
                }
                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                ArrayList<Review> alReviews = Utils.stringToReviews(Utils.readIt(inputStream),
                        movieId);
                storeReviews(alReviews);

                Thread.sleep(2000);  //sleep 2 secs

            } catch (IOException | InterruptedException e) {
//            publishProgress("Error fetching movies. Try again later.");
                e.printStackTrace();
//            return null;
            }
        }
    }

    private void syncVideos(String apiKey, String sync_mode){
        Log.i(TAG, "sync videos");
        ArrayList<Movie> alMovies = getStoredMovies(sync_mode);

        for(int i = 0; i<alMovies.size(); i++){
            long serverId = alMovies.get(i).getmServerId();
            long movieId = alMovies.get(i).getmId();
            try{
                URL urlPopular = new URL(
                        "http://api.themoviedb.org/3/movie/"+serverId+"/videos?api_key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) urlPopular.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Accept", "application/json");

                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                while(response == 429) {
                    Log.d(TAG, "The response is: " + response);
                    Thread.sleep(2000);
                    response = conn.getResponseCode();
                }

                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                ArrayList<Trailer> alTrailers = Utils.stringToVideos(Utils.readIt(inputStream),
                        movieId);
                storeVideos(alTrailers);

                Thread.sleep(2000);  //sleep 2 secs
            } catch (IOException | InterruptedException e) {
//            publishProgress("Error fetching movies. Try again later.");
                e.printStackTrace();
//            return null;
            }

        }
    }

    /************ STORE STUFF ***************************/
    private void storeMovies(ArrayList<Movie> alMovies) {
        if(alMovies != null && !alMovies.isEmpty()){
            ContentValues[] movies = getMovieCVs(alMovies);

            int insertedMovies = mContentResolver
                    .bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, movies);
            Log.d(TAG, "inserted movies :: " + insertedMovies);
        }

    }

    private void storeReviews(ArrayList<Review> alReviews){
        if(alReviews != null && !alReviews.isEmpty()) {
            ContentValues[] reviews = getReviewCVs(alReviews);
            int insertedReviews = mContentResolver
                    .bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, reviews);
            Log.d(TAG, alReviews.get(0).getMovieId()+" ... inserted reviews :: " + insertedReviews);
        }
    }

    private void storeVideos(ArrayList<Trailer> alTrailers){
        if(alTrailers != null && !alTrailers.isEmpty()) {
            ContentValues[] videos = getVideosCVs(alTrailers);
            int insertedTrailers = mContentResolver
                    .bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, videos);
            Log.d(TAG, "movieId :: "+ alTrailers.get(0).getmMovieId() +" ... inserted videos :: " + insertedTrailers);
        }
    }

    /********** MISC **********************/

    private ArrayList<Movie> getStoredMovies(String sync_mode){
        ArrayList<Movie> movies = new ArrayList<>();
        String[] projection = new String[]{
                MoviesContract.MovieEntry.TABLE_NAME+"."+MoviesContract.MovieEntry._ID,
                MoviesContract.MovieEntry.COLUMN_SERVER_ID,
                MoviesContract.MovieEntry.COLUMN_TITLE,
                MoviesContract.MovieEntry.COLUMN_SYNOPSIS,
                MoviesContract.MovieEntry.COLUMN_THUMB_ARRAY,
                MoviesContract.MovieEntry.COLUMN_RATING,
                MoviesContract.MovieEntry.COLUMN_DATE,
                MoviesContract.MovieEntry.COLUMN_SORTING,
                MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY,
                MoviesContract.MovieEntry.COLUMN_IS_FAVORITE
        };

        String selection = "";
        String[] selectionArgs = new String[1];

        Cursor cursor;

        if(sync_mode != null){
            selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
            if(sync_mode.equals(SYNC_POPULAR)){
                selectionArgs[0] = MoviesContract.SORTING_POPULAR;
            }else if(sync_mode.equals(SYNC_RATED)){
                selectionArgs[0] = MoviesContract.SORTING_RATED;
            }
            cursor = getContext().getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI
                    , projection
                    , selection
                    , selectionArgs
                    , null
            );
        }else{
            cursor = getContext().getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI
                    , projection
                    , null
                    , null
                    , null
            );
        }

        if(cursor.moveToFirst()){
            do{
                movies.add(new Movie(cursor, true));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return movies;

    }

    private ArrayList<Long> getFavorites(){
        ArrayList<Long> favorites = new ArrayList<>();
        String selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";
        String[] selectionArgs = new String[]{MoviesContract.SORTING_FAVORITE};
        String[] projection = new String[]{MoviesContract.MovieEntry.COLUMN_SERVER_ID};
        Cursor cursor = getContext().getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI
                , projection
                , selection
                , selectionArgs
                , null
        );
        if(cursor.moveToFirst()){
            do{
                favorites.add(cursor.getLong(0));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return favorites;
    }

    private ContentValues[] getMovieCVs(ArrayList<Movie> alMovies) {
        if (alMovies != null && !alMovies.isEmpty()) {
            ContentValues[] moviesCVs = new ContentValues[alMovies.size()];
            for(int i = 0; i< alMovies.size(); i++) {
                moviesCVs[i] = alMovies.get(i).getCVs();
            }
            return moviesCVs;
        }
        return null;
    }

    private ContentValues[] getReviewCVs(ArrayList<Review> alReviews) {
        if (alReviews != null && !alReviews.isEmpty()) {
            ContentValues[] reviewsCVs = new ContentValues[alReviews.size()];
            for(int i = 0; i< alReviews.size(); i++) {
                Review review = alReviews.get(i);
                /**
                 *
                 public static final String COLUMN_MOVIE_ID ="movie_id";
                 public static final String COLUMN_AUTHOR ="author";
                 public static final String COLUMN_CONTENT = "content";
                 */
                ContentValues reviewCv = new ContentValues();
                reviewCv.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, review.getMovieId());
                reviewCv.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, review.getmAuthor() );
                reviewCv.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, review.getmContent());

                reviewsCVs[i] = reviewCv;
            }
            return reviewsCVs;
        }
        return null;
    }

    private ContentValues[] getVideosCVs(ArrayList<Trailer> alTrailers) {
        if (alTrailers != null && !alTrailers.isEmpty()) {
            ContentValues[] videosCVs = new ContentValues[alTrailers.size()];
            for(int i = 0; i< alTrailers.size(); i++) {
                Trailer trailer = alTrailers.get(i);
                /**
                 *
                 public static final String COLUMN_MOVIE_ID = "movie_id";
                 public static final String COLUMN_URL = "url";
                 public static final String COLUMN_LANG = "lang";
                 public static final String COLUMN_NAME = "name";
                 */
                ContentValues videoCv = new ContentValues();
                videoCv.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, trailer.getmMovieId());
                videoCv.put(MoviesContract.VideoEntry.COLUMN_URL, trailer.getmUrl());
                videoCv.put(MoviesContract.VideoEntry.COLUMN_LANG, trailer.getmLang());
                videoCv.put(MoviesContract.VideoEntry.COLUMN_NAME, trailer.getmName());

                videosCVs[i] = videoCv;
            }
            return videosCVs;
        }
        return null;
    }
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);


        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            ContentResolver.setIsSyncable(newAccount,
                    context.getString(R.string.content_authority), 1);

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.i("getSyncAccount", "Error creating explicity account");
                return null;
            }

        }
        Log.i("getSyncAccount", "returning new account");
        return newAccount;
    }

    public void showToast(String message) {
        Log.i(TAG, message);
        try {
            Toast.makeText(getContext().getApplicationContext()
                    , message, Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
