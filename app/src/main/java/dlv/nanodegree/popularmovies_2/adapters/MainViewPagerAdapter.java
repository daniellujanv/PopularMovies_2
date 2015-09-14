package dlv.nanodegree.popularmovies_2.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.fragments.MoviesFragment;

/**
 *
 * Created by daniellujanvillarreal on 9/1/15.
 */
public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<String> mTitles;
    private final String POPULAR = "Popular";
    private final String HIGHEST_RATED = "Highest Rated";
    private final String FAVS = "My Favs";
    public final static int POPULAR_INDEX = 0;
    public final static int RATED_INDEX = 1;
    public final static int FAVS_INDEX = 2;

    private int mFragmentToScroll = 0;
    private int mPositionToScroll = 0;

    private MoviesFragment mMoviesFragment;
    private FragmentManager mFragmentManager;
    private boolean mTwoPane;

    public MainViewPagerAdapter(FragmentManager fm, boolean twoPane) {
        super(fm);

        mFragmentManager = fm;
        mTitles = new ArrayList<>();
        mTitles.add(POPULAR);
        mTitles.add(HIGHEST_RATED);
        mTitles.add(FAVS);
        mTwoPane = twoPane;
    }

    @Override
    public Fragment getItem(int position) {
        if(mFragmentToScroll == position) {
            mMoviesFragment = MoviesFragment.newInstance(position, mTwoPane, mPositionToScroll);
        }else{
            mMoviesFragment = MoviesFragment.newInstance(position, mTwoPane, 0);
        }
        return mMoviesFragment;
//        String fragmentName = mTitles.get(position);
//        switch(fragmentName){
//            case POPULAR:
//                mMoviesFragment = MoviesFragment.newInstance(position, mTwoPane);
//                return mMoviesFragment;
//
//            case HIGHEST_RATED:
//                mRatedFragment = RatedFragment.newInstance(position, mTwoPane);
//                return mRatedFragment;
//
//            case FAVS:
//                mFavoritesFragment = FavoritesFragment.newInstance();
//                return mFavoritesFragment;
//
//            default:
//                mMoviesFragment =  MoviesFragment.newInstance(position, mTwoPane);
//                return mMoviesFragment;
//        }
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        return mTitles.get(position);
    }


    //sync action bar button pressed
    public void updateFragment(int position){
        //find currently visible fragment ... otherwise last used fragment will always be returned
        mMoviesFragment = (MoviesFragment) findFragmentByPosition(position);
        mMoviesFragment.refreshContents();
    }

    public void setGridPosition(int currentView, int itemPosition){
        mFragmentToScroll = currentView;
        mPositionToScroll = itemPosition;
    }

    /**
     * Snippet from http://stackoverflow.com/questions/6976027/reusing-fragments-in-a-fragmentpageradapter
     * @param position current fragment
     * @return Fragment
     */
    public Fragment findFragmentByPosition(int position) {
        return mFragmentManager.findFragmentByTag(
                "android:switcher:" + R.id.main_viewpager + ":"
                        + getItemId(position));
    }


}
