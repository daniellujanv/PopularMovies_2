package dlv.nanodegree.popularmovies_2;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import dlv.nanodegree.popularmovies_2.syncAdapter.MoviesSyncAdapter;
import dlv.nanodegree.popularmovies_2.adapters.MainViewPagerAdapter;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.fragments.DetailFragment;
import dlv.nanodegree.popularmovies_2.fragments.MoviesFragment;

public class MainActivity extends AppCompatActivity
        implements MoviesFragment.OnGridSelectedItem{

    private boolean mTwoPane = false;
    private String DETAIL_FRAGMENT_TAG = "fragment_movie_details";
    private final String CURRENT_PAGE_KEY = "current_page";
    public static final String ITEM_GRID_POSITON = "grid_position";
    public static String MOVIES_PREFS = "popular_movies_prefs";
    public static String FIRST_TIME = "first_time";

    private int mCurrentPos;
    private int mLastGridItemPos;


    @Bind(R.id.main_viewpager)
    ViewPager mViewPager;
    MainViewPagerAdapter mMainViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(savedInstanceState == null){
            ContentResolver.setSyncAutomatically(
                    MoviesSyncAdapter.getSyncAccount(getApplicationContext())
                    , getString(R.string.content_authority), true);
        }

        //check if there are two panes in layout
        if(findViewById(R.id.fragment_details_container) != null){
            //we have two panes
            mTwoPane = true;
            //if savedInstance == null we replace fragment.. otherwise let android do it

            if(savedInstanceState == null){
                Bundle extras = new Bundle();
                //we use -1 as default value in DetailFragment.... so -2 means --> no movie on purpose
                extras.putLong(Movie.MOVIE_KEY, -2L);
                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setArguments(extras);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_details_container,
                                detailFragment, DETAIL_FRAGMENT_TAG).commit();
            }
        }else{
            mTwoPane = false;
        }

        //left side of view is a ViewPager
        mMainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), mTwoPane);
        mViewPager.setAdapter(mMainViewPagerAdapter);
        //increase offscreen limit so fragments are not destroyed
        // ... also to avoid losing movies when there is no connection
        mViewPager.setOffscreenPageLimit(2);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mCurrentPos = extras.getInt(CURRENT_PAGE_KEY, 0);
            mLastGridItemPos = extras.getInt(ITEM_GRID_POSITON, 0);
            mViewPager.setCurrentItem(mCurrentPos);
            mMainViewPagerAdapter.setGridPosition(mCurrentPos, mLastGridItemPos);
        }
//        SharedPreferences sharedPreferences =
//                getApplicationContext().getSharedPreferences(MOVIES_PREFS, MODE_PRIVATE);
//        //If this is the first time app is opened .. full sync
//        if(sharedPreferences.getBoolean(FIRST_TIME, true)) {
//            MoviesSyncAdapter.syncImmediately(getApplicationContext(), MoviesSyncAdapter.SYNC_MOVIES);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(FIRST_TIME, false);
//            editor.apply();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//         Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            if(mMainViewPagerAdapter != null) {
                mMainViewPagerAdapter.updateFragment(mViewPager.getCurrentItem());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //fragment interface
    @Override
    public void onMovieSelected(long movieId, int currentPage, int itemGridPosition) {

        Log.i("GridView", "OnClickListener - fullScreenImage :: id "+movieId);
        if(!mTwoPane) {

            Intent intent = new Intent(
                    getApplicationContext(), DetailActivity.class);
            intent.putExtra(Movie.MOVIE_KEY, movieId);
            intent.putExtra(CURRENT_PAGE_KEY, currentPage);
            intent.putExtra(ITEM_GRID_POSITON, itemGridPosition);
            startActivity(intent);
        }else{

            Bundle extras = new Bundle();
            extras.putLong(Movie.MOVIE_KEY, movieId);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(extras);

            if(itemGridPosition == -1) {
                // used only on first load of movie... this onMovieSelected is called
                // inside of onLoadFinished and we can safely use commitAllowingStateLoss
                // otherwise use the normal commit
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_details_container,
                                detailFragment, DETAIL_FRAGMENT_TAG).commitAllowingStateLoss();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_details_container,
                                detailFragment, DETAIL_FRAGMENT_TAG).commit();
            }

        }

    }
}
