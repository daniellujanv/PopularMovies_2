package dlv.nanodegree.popularmovies_2.databaseTests;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import dlv.nanodegree.popularmovies_2.data.MoviesContentProvider;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 * Created by daniellujanvillarreal on 9/7/15.
 */
public class UriMatcherTests extends AndroidTestCase{

    // content://auth/movies"
    private static final Uri TEST_MOVIE_DIR = MoviesContract.MovieEntry.CONTENT_URI;
    // content://auth/reviews"
    private static final Uri TEST_REVIEW_DIR = MoviesContract.ReviewEntry.CONTENT_URI;
    // content://auth/reviews/0"
    private static final Uri TEST_REVIEW_WITH_ID_DIR = MoviesContract.ReviewEntry.buildQueryUri(0);
    // content://auth/videos"
    private static final Uri TEST_VIDEO_DIR = MoviesContract.VideoEntry.CONTENT_URI;
    // content://auth/videos/0"
    private static final Uri TEST_VIDEO_WITH_ID_DIR = MoviesContract.VideoEntry.buildQueryUri(0);


    public void testUriMatcher() {
        UriMatcher testMatcher = MoviesContentProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MoviesContentProvider.MOVIES);
        assertEquals("Error: The REVIEWS URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_DIR), MoviesContentProvider.REVIEWS);
        assertEquals("Error: The REVIEWS_WITH_ID URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_WITH_ID_DIR), MoviesContentProvider.REVIEWS_WITH_MOVIE);
        assertEquals("Error: The VIDEOS URI was matched incorrectly.",
                testMatcher.match(TEST_VIDEO_DIR), MoviesContentProvider.VIDEOS);
        assertEquals("Error: The VIDEOS_WITH_ID URI was matched incorrectly.",
                testMatcher.match(TEST_VIDEO_WITH_ID_DIR), MoviesContentProvider.VIDEOS_WITH_MOVIE);
    }
}
