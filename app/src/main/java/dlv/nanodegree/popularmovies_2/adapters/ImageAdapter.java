package dlv.nanodegree.popularmovies_2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import dlv.nanodegree.popularmovies_2.DetailActivity;
import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.fragments.DetailFragment;


/**
 *
 * Created by DanielLujanApps on Monday10/08/15.
 */
public class ImageAdapter extends BaseAdapter {

//    private String DETAIL_FRAGMENT_TAG = "fragment_movie_details";

    private String TAG = getClass().getSimpleName();
    public ArrayList<Movie> alMovies;
//    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View mNoContentView;
//    private int mCurrentPage;
//    private String CURRENT_PAGE_KEY = "current_page";
//    private final boolean mTwoPane;
//    private FragmentManager mFragmentManager;

    public ImageAdapter(Context context, LayoutInflater layoutInflater
            , View noContentView, int currentPage, boolean twoPane
            , android.support.v4.app.FragmentManager fragmentManager){
//        mContext = context;
        alMovies = new ArrayList<>();
        mLayoutInflater = layoutInflater;
        mNoContentView = noContentView;
//        mCurrentPage = currentPage;
//        mTwoPane = twoPane;
//        mFragmentManager = fragmentManager;
    }

    public void updateMovies(ArrayList<Movie> movies){
        alMovies = movies;
        notifyDataSetChanged();
        if(movies != null && movies.size() > 0) {
            Log.i(TAG, "updating #movies:: " + movies.size());
            mNoContentView.setVisibility(View.INVISIBLE);
        }else{
            mNoContentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getCount() {
//        Log.i(TAG, "getCount #movies:: " + alMovies.size());
        return alMovies.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Bind(R.id.imageview_gv_item)
    ImageView mImageView;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        if(convertView == null){
            //not recycled
            view = mLayoutInflater.inflate(R.layout.layout_gridview_item, parent, false);
            ButterKnife.bind(this, view);

            mImageView.setAdjustViewBounds(true);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else{
            view = convertView;
            ButterKnife.bind(this, view);
        }

        Movie movie = alMovies.get(position);

        byte[] array = movie.getPosterByteArray();
        if(array != null) {
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(array, 0, array.length));
        }

        return view;
    }

    public ArrayList<Movie> getMovies(){
        return alMovies;
    }
}
