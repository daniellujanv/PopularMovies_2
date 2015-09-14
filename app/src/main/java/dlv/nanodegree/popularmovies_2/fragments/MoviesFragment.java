package dlv.nanodegree.popularmovies_2.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import dlv.nanodegree.popularmovies_2.DetailActivity;
import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.adapters.ImageAdapter;
import dlv.nanodegree.popularmovies_2.adapters.MainViewPagerAdapter;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;
import dlv.nanodegree.popularmovies_2.syncAdapter.MoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private final String MOVIES_KEY = "movies_arraylist";
    private int MOVIES_LOADER = 0;
    private String FIRST_VISIBLE_POSITION_KEY = "first_visible";
    private String TWO_PANE_KEY = "two_pane";
    private String CURRENT_PAGE_KEY = "current_page";


    private ArrayList<Movie> mAlMovies;
    private ImageAdapter mImageAdapter;
    private int mCurrentPage;
    private boolean mTwoPane = false;
    private boolean autoSelectFirst = false;
    private int mStartingPosition = 0;

    //callback interface
    OnGridSelectedItem mCallback;

    //**************** LOADER *******************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.i("loader", "Movies Fragment Loader called");
        String[] projection = new String[]{
                MoviesContract.MovieEntry._ID,
                MoviesContract.MovieEntry.COLUMN_POSTER_ARRAY
        };

        String selection;
        String[] selectionArgs = null;
        selection = MoviesContract.MovieEntry.COLUMN_SORTING + " = ?";

        if(mCurrentPage == MainViewPagerAdapter.POPULAR_INDEX){
            selectionArgs = new String[]{MoviesContract.SORTING_POPULAR};
        }else if(mCurrentPage == MainViewPagerAdapter.RATED_INDEX){
            selectionArgs = new String[]{MoviesContract.SORTING_RATED};
        }else if(mCurrentPage == MainViewPagerAdapter.FAVS_INDEX){
            selectionArgs = new String[]{MoviesContract.SORTING_FAVORITE};
        }

        mProgressBarWrapper.setVisibility(View.VISIBLE);
        Log.d("MovieFragment", "fetching content from :: " +
                 MoviesContract.MovieEntry.CONTENT_URI);

        return new CursorLoader(
                getActivity().getApplicationContext()
                , MoviesContract.MovieEntry.CONTENT_URI
                , projection
                , selection
                , selectionArgs
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Movie> newMovies = getArrayListFromCursor(data);
        mImageAdapter.updateMovies(newMovies);
        if(!newMovies.isEmpty() || mCurrentPage == MainViewPagerAdapter.FAVS_INDEX) {
            mProgressBarWrapper.setVisibility(View.INVISIBLE);
        }
        mGridView.smoothScrollToPosition(mStartingPosition);
        if(autoSelectFirst) {
            mGridView.setSelection(0);
            onItemClick(mGridView, null, 0, -1);
            autoSelectFirst = false; // only in the first run this must be true
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.updateMovies(new ArrayList<Movie>());
        mProgressBarWrapper.setVisibility(View.INVISIBLE);
    }

    private ArrayList<Movie> getArrayListFromCursor(Cursor cursor){
        ArrayList<Movie> result = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                result.add(new Movie(cursor, false));
            }while(cursor.moveToNext());
        }
//        cursor.close();
        if(!result.isEmpty()){
            mNoContentView.setVisibility(View.INVISIBLE);
        }else{
            mNoContentView.setVisibility(View.VISIBLE);
        }
        return result;
    }

    //**************** INTERFACE *******************************/

    public interface OnGridSelectedItem {
        public void onMovieSelected(long selectedMovieId, int currentPage, int itemGridPosition);
    }

    //**************** FRAGMENT *******************************/

    /**
     * Create new instance with arguments
     * @return
     */
    public static MoviesFragment newInstance(int currentPage, boolean twoPane, int startingPos){
        MoviesFragment moviesFragment = new MoviesFragment();
        moviesFragment.mCurrentPage = currentPage;
        moviesFragment.mTwoPane = twoPane;
        moviesFragment.mAlMovies = new ArrayList<>();
        moviesFragment.MOVIES_LOADER = currentPage;
        moviesFragment.mStartingPosition = startingPos;
        return moviesFragment;
    }

    @Bind(R.id.gridview)
    GridView mGridView;
    @Bind(R.id.tv_no_content)
    View mNoContentView;
    @Bind(R.id.progressbar_wrapper)
    View mProgressBarWrapper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Loader loader = getLoaderManager().getLoader(MOVIES_LOADER);
        if ( loader != null && !loader.isReset() ) {
            getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_gridview, container, false);
        ButterKnife.bind(this, view);

        ((TextView) mNoContentView).setText(R.string.no_content);
        mImageAdapter = new ImageAdapter(getContext(), inflater, mNoContentView, mCurrentPage
                , mTwoPane, getFragmentManager());
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);
        if(savedInstanceState != null){
            mStartingPosition = savedInstanceState.getInt(FIRST_VISIBLE_POSITION_KEY, 0);
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE_KEY, 0);
            mTwoPane = savedInstanceState.getBoolean(TWO_PANE_KEY, false);
            mAlMovies = new ArrayList<>();//avoid nullPointerException on onItemClick
        }else{
            //if no instance is saved and first fragment with TwoPanes
            // is showing autoSelect first element in gridview
            autoSelectFirst = (mCurrentPage == 0 && mTwoPane);
        }
//        else{
//            if(isNetworkAvailable()) {
//                refreshContents();
//            }
//        }

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FIRST_VISIBLE_POSITION_KEY,
                mGridView.getFirstVisiblePosition());
        outState.putInt(CURRENT_PAGE_KEY, mCurrentPage);
        outState.putBoolean(TWO_PANE_KEY, mTwoPane);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        Activity activity = (Activity) context;
        try {
            mCallback = (OnGridSelectedItem) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mStartingPosition = position;
//        if(mAlMovies.isEmpty()){
        mAlMovies = mImageAdapter.getMovies();
//        }
        if(!mAlMovies.isEmpty()) {
            Movie movie = mAlMovies.get(position);
            if (id == -1) {
                mCallback.onMovieSelected(movie.getmId(), mCurrentPage, -1);
            } else {
                mCallback.onMovieSelected(movie.getmId(), mCurrentPage, position);
            }
        }
    }

    //Sync fragment content... for when user presses sync action bar button
    public void refreshContents(){
        if(isNetworkAvailable()) {

            mNoContentView.setVisibility(View.INVISIBLE);
            mProgressBarWrapper.setVisibility(View.VISIBLE);

            if(mCurrentPage == MainViewPagerAdapter.POPULAR_INDEX){
                MoviesSyncAdapter.syncImmediately(getContext()
                        , MoviesSyncAdapter.SYNC_POPULAR);
                showToast("Refreshing Popular Movies!");

            }else if(mCurrentPage == MainViewPagerAdapter.RATED_INDEX){
                MoviesSyncAdapter.syncImmediately(getContext()
                        , MoviesSyncAdapter.SYNC_RATED);
            }
            else{
//                MoviesSyncAdapter.syncImmediately(getContext(), MoviesSyncAdapter.SYNC_FAVS);
                mNoContentView.setVisibility(View.VISIBLE);
                mProgressBarWrapper.setVisibility(View.INVISIBLE);
            }

        }else{
            showToast("No connection found... try again later!");
        }
    }

    //Based on a stackoverflow snippet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public ArrayList<Movie> getMovies(){
        return mImageAdapter.getMovies();
    }
}
