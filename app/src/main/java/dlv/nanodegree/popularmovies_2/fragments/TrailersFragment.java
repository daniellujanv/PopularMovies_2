package dlv.nanodegree.popularmovies_2.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import dlv.nanodegree.popularmovies_2.R;
import dlv.nanodegree.popularmovies_2.classes.Trailer;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 * Created by daniellujanvillarreal on 9/11/15.
 */
public class TrailersFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private long mSelectedMovieId;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Trailer> alTrailers;
    private int MOVIE_TRAILERS_LOADER = 3;
    public static String SELECTED_MOVIE_KEY = "selected_movie";

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
                MoviesContract.VideoEntry.COLUMN_MOVIE_ID,
                MoviesContract.VideoEntry.COLUMN_LANG,
                MoviesContract.VideoEntry.COLUMN_NAME,
                MoviesContract.VideoEntry.COLUMN_URL
        };

//        String selection = MoviesContract.MovieEntry._ID+" = ?";
//        String[] selectionArgs = new String[]{Long.toString(mSelectedMovieId)};
        if (mSelectedMovieId > 0){
            return new CursorLoader(
                    getActivity()
                    , MoviesContract.VideoEntry.buildQueryUri(mSelectedMovieId)
                    , projection
                    , null
                    , null
                    , null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        alTrailers = new ArrayList<>();
        mTrailersContainer.removeAllViews();

        if(data.moveToFirst()) {
            do{
                alTrailers.add(new Trailer(data));
            }while(data.moveToNext());
            mNoContentView.setVisibility(View.INVISIBLE);
        }else{
            mNoContentView.setVisibility(View.VISIBLE);
        }
        for(int i = 0; i< alTrailers.size(); i++){
            View videoLayout = mLayoutInflater.inflate(
                    R.layout.layout_trailer, mTrailersContainer, false);
//            ((TextView) videoLayout.findViewById(R.id.video_lang)).setText(
//                    alTrailers.get(i).getmLang()
//            );
            ((TextView) videoLayout.findViewById(R.id.video_name)).setText(
                    alTrailers.get(i).getmName()
            );

            ImageButton playButton = (ImageButton) videoLayout.findViewById(R.id.trailer_play_button);
            final String url = alTrailers.get(i).getmUrl();
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + url)));
                }
            });


            ImageButton shareButton = (ImageButton) videoLayout.findViewById(R.id.trailer_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fullUrl = "http://www.youtube.com/watch?v=" + url;

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, fullUrl);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            });
//            ((TextView) videoLayout.findViewById(R.id.video_url)).setText(
//                    alTrailers.get(i).getmUrl()
//            );
            mTrailersContainer.addView(videoLayout);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alTrailers = new ArrayList<>();
    }

    @Bind(R.id.trailers_container)
    ViewGroup mTrailersContainer;
    @Bind(R.id.trailers_no_content)
    View mNoContentView;

    /**************** FRAGMENT *************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstance) {
        mLayoutInflater = inflater;
        View view = inflater.inflate(R.layout.layout_trailers_container, container, false);

        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();

        if(arguments != null) {
            mSelectedMovieId = arguments.getLong(TrailersFragment.SELECTED_MOVIE_KEY, -1);
        }else{
            Intent intent = getActivity().getIntent();
            mSelectedMovieId = intent.getLongExtra(TrailersFragment.SELECTED_MOVIE_KEY, -1);
        }

        getLoaderManager().initLoader(MOVIE_TRAILERS_LOADER, null, this);
        return view;
    }

    public ArrayList<Trailer> getTrailers(){
        return alTrailers;
    }
}
