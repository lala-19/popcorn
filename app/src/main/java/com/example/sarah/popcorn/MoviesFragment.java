package com.example.sarah.popcorn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Sarah on 8/13/2016.
 */
public class MoviesFragment extends Fragment {

    MoviesAdapter mMoviesAdapter;
    MovieData[] movies;
    FavoriteMoviesDB favoriteMoviesDB;

    public static final String state = "state";

    String statusVal;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    GridView gridView;

    public interface Callback {

         //DetailFragmentCallback for when an item has been selected.

         void onItemSelected(MovieData movieData);
    }


    public MoviesFragment(){
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sharedPrefs = getActivity().getSharedPreferences("myPref", getActivity().MODE_PRIVATE);
        if(savedInstanceState == null || !savedInstanceState.containsKey(state)) {
            if(sharedPrefs.getString(state,statusVal)!=null)
                statusVal = sharedPrefs.getString(state,statusVal);
            else
                statusVal = "popular";
        }
        else {
            statusVal = savedInstanceState.getString(state);
        }

       // getActivity().onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        editor = sharedPrefs.edit();

        if (id == R.id.favorites) {
            statusVal = "favorites";
            editor.putString(state, statusVal);
            editor.apply();
            movies = favoriteMoviesDB.fetchAllMovies();
            mMoviesAdapter.imagesStringArray(movies);
            gridView.setAdapter(mMoviesAdapter);
            return true;
        }
        if (id == R.id.sort_highest_rated) {
            statusVal = "top_rated";
            editor.putString(state, statusVal);
            editor.commit();
            new FetchMoviesTask().execute(statusVal);
            return true;
        }
        if (id == R.id.sort_most_popular) {
            statusVal = "popular";
            editor.putString(state, statusVal);
            editor.commit();
            new FetchMoviesTask().execute(statusVal);
            return true;
        }
        if (id == R.id.sign_out) {
            AuthUI.getInstance().signOut(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMoviesAdapter = new MoviesAdapter(getActivity());
        favoriteMoviesDB = new FavoriteMoviesDB(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        gridView = rootView.findViewById(R.id.movies_grid_view);

        if(statusVal.equals("favorites")){
            movies = favoriteMoviesDB.fetchAllMovies();
            mMoviesAdapter.imagesStringArray(movies);
            gridView.setAdapter(mMoviesAdapter);
        }
        else
            new FetchMoviesTask().execute(statusVal);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) getActivity()).onItemSelected(movies[position]);

            }
        });
        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(state,statusVal);
        super.onSaveInstanceState(outState);
    }


    public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();


        private MovieData[] getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            //JSON objects that need to be extracted from themoviedb.
            final String RESULTS = "results";
            final String TITLE = "title";
            final String RELEASE_DATE = "release_date";
            final String POSTER = "poster_path";
            final String VOTE_AVG = "vote_average";
            final String PLOT = "overview";
            final String ID = "id";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(RESULTS);

            movies = new MovieData[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                movies[i] = new MovieData();
                JSONObject movieInfo = moviesArray.getJSONObject(i);

                movies[i].setTitle(movieInfo.getString(TITLE));
                movies[i].setReleaseDate(movieInfo.getString(RELEASE_DATE));
                movies[i].makePosterURL();
                movies[i].setPoster(movieInfo.getString(POSTER));
                movies[i].setVoteAverage(movieInfo.getString(VOTE_AVG));
                movies[i].setPlot(movieInfo.getString(PLOT));
                movies[i].setId(movieInfo.getString(ID));

            }

            return movies;
        }


        @Override
        protected Void doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            try {
                // Construct the URL

                final String BASE_URL ="http://api.themoviedb.org/3/movie/";

                Uri builtUri;

                builtUri = Uri.parse(BASE_URL+params[0]).buildUpon()
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
                moviesJsonStr = buffer.toString();
                Log.d("movies string: ", moviesJsonStr);

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
                getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }
            // This will only happen if there was an error getting or parsing the data.
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(movies!=null) {
                mMoviesAdapter.imagesStringArray(movies);
                gridView.setAdapter(mMoviesAdapter);

            }

        }
    }
    }




