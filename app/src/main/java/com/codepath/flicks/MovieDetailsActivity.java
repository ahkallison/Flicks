package com.codepath.flicks;

import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.codepath.flicks.models.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.codepath.flicks.MovieListActivity.API_BASE_URL;
import static com.codepath.flicks.MovieListActivity.API_KEY_PARAM;

public class MovieDetailsActivity extends YouTubeBaseActivity {
    // the movie to display
    Movie movie;

    // resolve the view objects
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvOverview) TextView tvOverview;
    @BindView(R.id.rbVoteAverage) RatingBar rbVoteAverage;
    @BindView(R.id.releaseDate) TextView releaseDate;
    @BindView(R.id.player) YouTubePlayerView playerView;

    // declare client
    AsyncHttpClient client;

    // key for movie youtube key
    private final static String MOVIE_ID = "MOVIE_ID";

    // tag for logging errors
    private static final String TAG = "DetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // initialize the client
        client = new AsyncHttpClient();

        // applying ButterKnife
        ButterKnife.bind(this);

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // call HTTP GET request
        getVideos();

        // set the title, overview, and release date
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        releaseDate.setText("Release Date: " + movie.getReleased());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
    }

    // get the list of videos from the API
    private void getVideos() {
        // create the url
        String url = API_BASE_URL + "/movie/" + movie.getId() + "/videos" ;
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        // execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into videos list
                try {
                    JSONArray results = response.getJSONArray("results");
                    JSONObject video = results.getJSONObject(0);
                    String youtube_key = video.getString("key");

                    // initialize with API key stored in secrets.xml
                    final String videoId = youtube_key;
                    playerView.initialize(getString(R.string.youtube_api_key), new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                            YouTubePlayer youTubePlayer, boolean b) {
                            // do any work here to cue video, play video, etc.
                            youTubePlayer.cueVideo(videoId);
                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                            YouTubeInitializationResult youTubeInitializationResult) {
                            // log the error
                            Log.e("MovieTrailerActivity", "Error initializing Youtube player");
                        }
                    });
                } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Failed to get data");
            }
        });
    }

}
