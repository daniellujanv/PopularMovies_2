package dlv.nanodegree.popularmovies_2.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.classes.Review;
import dlv.nanodegree.popularmovies_2.classes.Trailer;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.syncAdapter.MoviesSyncAdapter;

/**
 *
 * Created by daniellujanvillarreal on 9/2/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private String TAG = getClass().getSimpleName();

    private int MOVIE_DETAILS_LOADER = 1;
    private String REVIEWS_FRAGMENT_TAG = "reviews_fragment_tag";
    private String VIDEOS_FRAGMENT_TAG = "videos_fragment_tag";

    private Movie mMovie;
    private long mSelectedMovieId;
    private ReviewsFragment mReviesFragment;
    private TrailersFragment mTrailersFragment;

    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.tv_date)
    TextView mTvDate;
    @Bind(R.id.tv_rating)
    TextView mTvRating;
    @Bind(R.id.tv_synopsis)
    TextView mTvSynopsis;
    @Bind(R.id.iv_details_image)
    ImageView mIvThumbnail;
    @Bind(R.id.details_no_content)
    View mNoContentView;
    @Bind(R.id.progressbar_wrapper)
    View mProgressBarWrapper;
    @Bind(R.id.btn_favorite)
    ImageButton btnFavorite;
    @Bind(R.id.container_fragment_details)
    View containerFragmentDetails;

    /**************** LOADER ****************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**
         public static final String COLUMN_SERVER_ID = "server_id";
         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_THUMB_ARRAY = "thumb_array";
         public static final String COLUMN_RATING = "rating";
         public static final String COLUMN_DATE = "date";
         //Sorting of movie (popular, rating, favs)
         public static final String COLUMN_SORTING = "sorting";
         */
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

//        String selection = MoviesContract.MovieEntry._ID+" = ?";
//        String[] selectionArgs = new String[]{Long.toString(mSelectedMovieId)};
        if(mSelectedMovieId > 0) {
            return new CursorLoader(
                    getActivity()
                    , MoviesContract.MovieEntry.buildMovieUri(mSelectedMovieId)
                    , projection
                    , null
                    , null
                    , null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            mMovie = new Movie(data, true);
        }else{
            mMovie = null;
        }
        displayMovieDetails();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovie = null;
        displayMovieDetails();
    }

    /**************** FRAGMENT *************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstance) {
        View view = inflater.inflate(R.layout.layout_fragment_detail, container, false);
        ButterKnife.bind(this, view);

        containerFragmentDetails.setVisibility(View.INVISIBLE);
        mNoContentView.setVisibility(View.INVISIBLE);
        mProgressBarWrapper.setVisibility(View.VISIBLE);

        Bundle arguments = getArguments();

        if(arguments != null) {
            mSelectedMovieId = arguments.getLong(Movie.MOVIE_KEY, -1);
        }else{
            Intent intent = getActivity().getIntent();
            mSelectedMovieId = intent.getLongExtra(Movie.MOVIE_KEY, -1);
        }

        if(mSelectedMovieId == -1) {
            mNoContentView.setVisibility(View.VISIBLE);
            mProgressBarWrapper.setVisibility(View.INVISIBLE);

            containerFragmentDetails.setVisibility(View.INVISIBLE);
            showToast("Error while loading selected movie");
        }

        getLoaderManager().initLoader(MOVIE_DETAILS_LOADER, null, this);

        //replace.reviews_fragment
        mReviesFragment = new ReviewsFragment();
        Bundle extras = new Bundle();
        //we use -1 as default value in DetailFragment.... so -2 means --> no movie on purpose
        extras.putLong(ReviewsFragment.SELECTED_MOVIE_KEY, mSelectedMovieId);
        mReviesFragment.setArguments(extras);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.reviews_fragment,
                        mReviesFragment, REVIEWS_FRAGMENT_TAG).commit();

        //replace.videos_fragment
        mTrailersFragment = new TrailersFragment();
        Bundle extrasVideos = new Bundle();
        //we use -1 as default value in DetailFragment.... so -2 means --> no movie on purpose
        extrasVideos.putLong(ReviewsFragment.SELECTED_MOVIE_KEY, mSelectedMovieId);
        mTrailersFragment.setArguments(extrasVideos);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.trailers_fragment,
                        mTrailersFragment, VIDEOS_FRAGMENT_TAG).commit();

        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @OnClick(R.id.btn_favorite)
    public void toggleFavMovie(){
        if(mMovie != null) {
            if(mMovie.getSorting().equals(MoviesContract.SORTING_FAVORITE)
                    || btnFavorite.isSelected()
                    ){
                long serverId = mMovie.getmServerId();
                long movieId = mMovie.getmId();
                //Remove movie from favs
                btnFavorite.setSelected(false);
                showToast("Removing movie from favorites");
                getContext().getContentResolver().delete(
                        MoviesContract.MovieEntry.buildMovieUri(mMovie.getmId()),
                        null,
                        null
                );

                ContentValues isFav = new ContentValues();
                isFav.put(MoviesContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
                getContext().getContentResolver().update(
                        MoviesContract.MovieEntry.buildMovieUri(serverId),
                        isFav,
                        null,
                        null
                );
                mMovie = null;
                displayMovieDetails();
            }else{
                //Add movie to favs
                btnFavorite.setSelected(true);
                showToast("Adding movie to favorites");
                Movie newFav = mMovie;
                newFav.setSorting(MoviesContract.SORTING_FAVORITE);
                newFav.setmIsFavorite(true);
                Uri newMovieUri = getContext().getContentResolver().insert(
                        MoviesContract.MovieEntry.CONTENT_URI,
                        newFav.getCVs()
                );

                ContentValues isFav = new ContentValues();
                isFav.put(MoviesContract.MovieEntry.COLUMN_IS_FAVORITE, 1);
                getContext().getContentResolver().update(
                        MoviesContract.MovieEntry.buildMovieUri(mMovie.getmServerId()),
                        isFav,
                        null,
                        null
                );

                long newId = MoviesContract.MovieEntry.getIdFromUri(newMovieUri);

                ArrayList<Review> reviews = mReviesFragment.getReviews();
                storeReviews(newId, reviews);

                ArrayList<Trailer> trailers = mTrailersFragment.getTrailers();
                storeTrailers(newId, trailers);
            }
        }
    }

    private void displayMovieDetails(){
        if(mMovie != null) {
            mNoContentView.setVisibility(View.INVISIBLE);
            containerFragmentDetails.setVisibility(View.VISIBLE);
            mTvTitle.setText(mMovie.getTitle());
            mTvDate.setText(mMovie.getReleaseDate());
            mTvRating.setText("" + mMovie.getRating() + "/10");
            mTvSynopsis.setText(mMovie.getSynopsis());

            if(mMovie.ismIsFavorite()){
                btnFavorite.setSelected(true);
            }

            byte[] imageBytes = mMovie.getThumbnailByteArray();
            if (imageBytes != null) {
                //no need to fetch movie thumbnail
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                mIvThumbnail.setImageBitmap(bitmap);
            }
        } else {
            mNoContentView.setVisibility(View.VISIBLE);
            containerFragmentDetails.setVisibility(View.INVISIBLE);
//            getActivity().onOptionsItemSelected(menuItemHome);
        }
        if(!isSyncAdapterRunning()) {

            mProgressBarWrapper.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private boolean isSyncAdapterRunning(){
        for(SyncInfo syncInfo : ContentResolver.getCurrentSyncs())
        {
            if(syncInfo.account.equals(MoviesSyncAdapter.getSyncAccount(getContext())) &&
                    syncInfo.authority.equals(getString(R.string.content_authority)))
            {
                return true;
            }
        }
        return false;
    }


    private void storeReviews(long newId, ArrayList<Review> reviews){
        if(reviews != null && !reviews.isEmpty()){
            ContentValues[] values = new ContentValues[reviews.size()];
            for(int i = 0; i< reviews.size(); i++){
                values[i] = reviews.get(i).getCVs(newId);
            }

            int insertedReviews = getContext().getContentResolver()
                    .bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, values);
            Log.d(TAG, reviews.get(0).getMovieId() + " ... inserted reviews :: " + insertedReviews);

        }

    }

    private void storeTrailers(long newId, ArrayList<Trailer> alTrailers){
        if(alTrailers != null && !alTrailers.isEmpty()){
            ContentValues[] values = new ContentValues[alTrailers.size()];
            for(int i = 0; i< alTrailers.size(); i++){
                values[i] = alTrailers.get(i).getCVs(newId);
            }
            int insertedReviews = getContext().getContentResolver()
                    .bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, values);
            Log.d(TAG, alTrailers.get(0).getmMovieId() + " ... inserted trailers :: " + insertedReviews);
        }
    }
}
