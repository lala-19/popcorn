package com.example.sarah.popcorn;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sarah on 9/10/2016.
 */
public class DetailFragment extends Fragment{
    MovieData movieData;
    FavoriteMoviesDB favoriteMoviesDB;
    String title,
            releaseDate,
            poster,
            voteAverage,
            plot;
    Button favoriteBtn,deleteBtn;
    TextView titleTxt;
    ImageView posterImg;
    TextView releaseDateTxt;
    TextView voteTxt;
    TextView plotTxt;
    ScrollView parentLayout;
    ListView trailersList;
    ListView reviewsList;
    TextView reviewTxt;
    ArrayAdapter<String> trailersListAdapter;
    ArrayAdapter<String> reviewsListAdapter;

    String[] trailersName,
            trailersKey,
            reviewsAuthor,
            reviewsContent;
    Boolean isTrailers;


    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        parentLayout= (ScrollView) rootView.findViewById(R.id.scrollView);
        titleTxt = (TextView) rootView.findViewById(R.id.title_txt_view);
        releaseDateTxt = (TextView) rootView.findViewById(R.id.date_txt_view);
        voteTxt = (TextView) rootView.findViewById(R.id.vote_txt_view);
        plotTxt = (TextView) rootView.findViewById(R.id.overview_txt_view);
        posterImg = (ImageView) rootView.findViewById(R.id.poster_img_view);
        favoriteBtn = (Button) rootView.findViewById(R.id.favorite_btn);
        deleteBtn = (Button) rootView.findViewById(R.id.delete_btn);
        trailersList = (ListView) rootView.findViewById(R.id.trailers_list);
        reviewTxt = (TextView) rootView.findViewById(R.id.reviews_txt_view);
        reviewsList = (ListView) rootView.findViewById(R.id.reviews_list);
        trailersListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_trailer,R.id.trailer_num_txt_view);
        reviewsListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_review,R.id.review_txt);

        trailersList.setAdapter(trailersListAdapter);
        reviewsList.setAdapter(reviewsListAdapter);

        favoriteMoviesDB = new FavoriteMoviesDB(getActivity());



        //This condition is to display the content of detail fragment only when there's an item selected,
        // otherwise it will be empty
        Bundle arguments = getArguments();
        if (arguments != null) {
            movieData = (MovieData) arguments.getSerializable("movie");
            //This condition checks if it doesn't have a trailer name
            // then it's not a favorite and not stored in the DB favorite, we need to fetch trailers and reviews from server
            //if it has then we don't need to fetch data it's already stored
            if(movieData.getTrailersName()==null){
                new FetchTrailersReviews().execute("videos");
            }
            else{
                fillUI();
            }

        } else if (!MainActivity.twoPane) {
            Intent i = getActivity().getIntent();
            movieData = (MovieData) i.getSerializableExtra("movie");
            if(movieData.getTrailersName()==null){
                new FetchTrailersReviews().execute("videos");
            }
            else{
                fillUI();
            }
        }

        return rootView;
    }


    public void fillUI(){

        //If fetching trailers and reviews is completed successfully,we fill the ui
        // if not it might be a connection error
        if(movieData.getTrailersName()!=null) {
            parentLayout.setVisibility(parentLayout.VISIBLE);

            if(favoriteMoviesDB.isMovieInDB(movieData.getId()))
                favoriteBtn.setText("It's A Favorite");

            poster = movieData.getPoster();
            releaseDate = movieData.getReleaseDate();
            title = movieData.getTitle();
            voteAverage = movieData.getVoteAverage();
            plot = movieData.getPlot();
            titleTxt.setText(title);
            releaseDateTxt.setText(releaseDate);
            voteTxt.setText(voteAverage);
            plotTxt.setText(plot);
            Picasso.with(getActivity())
                    .load(poster)
                    .into(posterImg);


            for (int i = 0; i < movieData.getTrailersName().length; i++) {
                if (movieData.getTrailersName()[i] != null)
                    trailersListAdapter.add(movieData.getTrailersName()[i]);

            }

            for (int i = 0; i < movieData.getReviewsAuthor().length; i++) {
                if (movieData.getReviewsAuthor()[i] != null && movieData.getReviewsContent()[i] != null)

                    reviewsListAdapter.add(System.getProperty("line.separator") + movieData.getReviewsAuthor()[i] + ": "
                            + System.getProperty("line.separator") + System.getProperty("line.separator")
                            + movieData.getReviewsContent()[i]
                            + System.getProperty("line.separator"));
            }
        }
        else{
            Toast.makeText(getActivity(),"Please check you internet connection",Toast.LENGTH_LONG).show();
        }


        trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + movieData.getTrailersKey()[position]);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteMoviesDB.addMovie(movieData);
                favoriteBtn.setText("It's A Favorite");

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteMoviesDB.deleteAll();
            }
        });


    }



    public class FetchTrailersReviews extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchTrailersReviews.class.getSimpleName();
        private MovieData getDataFromJson(String jsonStr)
                throws JSONException {

            // Names of the JSON objects that need to be extracted from themoviedb.
            final String RESULTS = "results";
            final String TRAILER_NAME = "name";
            final String TRAILER_KEY = "key";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";

            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(RESULTS);

            if(isTrailers){

                trailersName = new String[moviesArray.length()];
                trailersKey = new String[moviesArray.length()];

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieInfo = moviesArray.getJSONObject(i);
                    trailersName[i] = movieInfo.getString(TRAILER_NAME);
                    trailersKey[i] = movieInfo.getString(TRAILER_KEY);
                }

                movieData.setTrailersName(trailersName);
                movieData.setTrailersKey(trailersKey);

            }

            else {

                reviewsAuthor = new String[moviesArray.length()];
                reviewsContent = new String[moviesArray.length()];

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieInfo = moviesArray.getJSONObject(i);
                    reviewsAuthor[i] = movieInfo.getString(REVIEW_AUTHOR);
                    reviewsContent[i] = movieInfo.getString(REVIEW_CONTENT);
                }

                movieData.setReviewsAuthor(reviewsAuthor);
                movieData.setReviewsContent(reviewsContent);
            }

            return movieData;
        }


        @Override
        protected Void doInBackground(String... params) {

            if(params[0].equals("videos")){
                isTrailers = true;
            }
            else {
                isTrailers = false;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                // Construct the URL

                final String BASE_URL ="http://api.themoviedb.org/3/movie/";

                Uri builtUri;

                builtUri = Uri.parse(BASE_URL+movieData.getId()+"/"+params[0]).buildUpon()
                        .appendQueryParameter("api_key", BuildConfig.MOVIESDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                Log.d("json string: ", jsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG,"Error ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
       protected void onPostExecute(Void v) {
            //If we're fetching trailers then we call execute("reviews") to fetch them after,
            //else we're fetching reviews(that means we have reviews and trailers), we fill the ui
            if(isTrailers){
                new FetchTrailersReviews().execute("reviews");
            }
            else if(!isTrailers) {
                fillUI();
            }

        }
    }
}

