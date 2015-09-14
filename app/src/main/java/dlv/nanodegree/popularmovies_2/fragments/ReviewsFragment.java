package dlv.nanodegree.popularmovies_2.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.classes.Review;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.syncAdapter.MoviesSyncAdapter;

/**
 * Created by daniellujanvillarreal on 9/11/15.
 */
public class ReviewsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private long mSelectedMovieId;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Review> alReviews;
    private int MOVIE_REVIEWS_LOADER = 2;
    public static String SELECTED_MOVIE_KEY = "selected_movie";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**

         public static final String COLUMN_MOVIE_ID ="movie_id";
         public static final String COLUMN_AUTHOR ="author";
         public static final String COLUMN_CONTENT = "content";


         */
        String[] projection = new String[]{
                MoviesContract.ReviewEntry.COLUMN_MOVIE_ID,
                MoviesContract.ReviewEntry.COLUMN_AUTHOR,
                MoviesContract.ReviewEntry.COLUMN_CONTENT
        };

        Log.i("ReviewsFragment", "Fetching reviews for movie :: " + mSelectedMovieId);
        if (mSelectedMovieId > 0){

            String selection = MoviesContract.ReviewEntry.COLUMN_MOVIE_ID+" = ?";
            String[] selectionArgs = new String[]{Long.toString(mSelectedMovieId)};

            return new CursorLoader(
                    getActivity()
                    , MoviesContract.ReviewEntry.buildQueryUri(mSelectedMovieId)
                    , projection
                    , selection
                    , selectionArgs
                    , null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        alReviews = new ArrayList<>();
        mReviewsContainer.removeAllViews();
        if(data.moveToFirst()) {
            do{
                alReviews.add(new Review(data));
            }while(data.moveToNext());
            mNoContentView.setVisibility(View.INVISIBLE);
        }else{
            mNoContentView.setVisibility(View.VISIBLE);
        }
        for(int i = 0; i< alReviews.size(); i++){
            View reviewLayout = mLayoutInflater.inflate(
                    R.layout.layout_review, mReviewsContainer, false);
            ((TextView) reviewLayout.findViewById(R.id.review_author)).setText(
                    alReviews.get(i).getmAuthor()
            );
            ((TextView) reviewLayout.findViewById(R.id.review_content)).setText(
                    alReviews.get(i).getmContent()
            );
            mReviewsContainer.addView(reviewLayout);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alReviews = new ArrayList<>();
    }

    @Bind(R.id.reviews_container)
    ViewGroup mReviewsContainer;
    @Bind(R.id.reviews_no_content)
    View mNoContentView;

    /**************** FRAGMENT *************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstance) {
        mLayoutInflater = inflater;
        View view = inflater.inflate(R.layout.layout_reviews_container, container, false);

        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();

        if(arguments != null) {
            mSelectedMovieId = arguments.getLong(ReviewsFragment.SELECTED_MOVIE_KEY, -1);
        }else{
            Intent intent = getActivity().getIntent();
            mSelectedMovieId = intent.getLongExtra(ReviewsFragment.SELECTED_MOVIE_KEY, -1);
        }

        getLoaderManager().initLoader(MOVIE_REVIEWS_LOADER, null, this);
        return view;
    }

    public ArrayList<Review> getReviews(){
        return alReviews;
    }
}
