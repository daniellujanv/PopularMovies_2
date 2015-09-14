package dlv.nanodegree.popularmovies_2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    private int mCurrentPage;
    private String CURRENT_PAGE_KEY = "current_page";
    private int mLastGridItemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCurrentPage = getIntent().getExtras().getInt(CURRENT_PAGE_KEY, 0);
        mLastGridItemPosition = getIntent().getExtras().getInt(MainActivity.ITEM_GRID_POSITON, 0);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_details_container, new DetailFragment())
                    .commit();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.menu_detail, menu);
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.putExtra(CURRENT_PAGE_KEY, mCurrentPage);
            intent.putExtra(MainActivity.ITEM_GRID_POSITON, mLastGridItemPosition);
            NavUtils.navigateUpTo(this, intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
