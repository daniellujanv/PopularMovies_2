package dlv.nanodegree.popularmovies_2.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import dlv.nanodegree.popularmovies_2.classes.Movie;
import dlv.nanodegree.popularmovies_2.classes.Review;
import dlv.nanodegree.popularmovies_2.classes.Trailer;
import dlv.nanodegree.popularmovies_2.data.MoviesContract;

/**
 *
 * Created by daniellujanvillarreal on 9/8/15.
 */
public class Utils {

    public static ArrayList<Movie> stringToMovies(String response, String sorting){
        ArrayList<Movie> movies = new ArrayList<>();
        try {
            JSONObject jsonArray = new JSONObject(response);
            JSONArray moviesJArray = jsonArray.getJSONArray("results");
            for(int i= 0; i < moviesJArray.length(); i++){
                moviesJArray.getJSONObject(i).put(MoviesContract.MOVIE_SORTING, sorting);
                movies.add(new Movie(moviesJArray.getJSONObject(i)));
            }
        } catch (JSONException | MalformedURLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public static ArrayList<Review> stringToReviews(String response, long movieId){
        ArrayList<Review> review = new ArrayList<>();
        try {
            JSONObject jsonArray = new JSONObject(response);
            JSONArray reviewsJArray = jsonArray.getJSONArray("results");
            for(int i= 0; i < reviewsJArray.length(); i++){
                reviewsJArray.getJSONObject(i)
                        .put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
                review.add(new Review(reviewsJArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return review;
    }

    public static ArrayList<Trailer> stringToVideos(String response, long movieId){
        ArrayList<Trailer> trailer = new ArrayList<>();
        try {
            JSONObject jsonArray = new JSONObject(response);
            JSONArray videosJArray = jsonArray.getJSONArray("results");
            for(int i= 0; i < videosJArray.length(); i++){
                videosJArray.getJSONObject(i)
                        .put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, movieId);
                trailer.add(new Trailer(videosJArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trailer;
    }

    // Reads an InputStream and converts it to a String.
    //got it from http://stackoverflow.com/questions/6511880/how-to-parse-a-json-input-stream
    public static String readIt(InputStream stream) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        return responseStrBuilder.toString();
    }
}
