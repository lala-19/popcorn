package com.example.sarah.popcorn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Sarah on 9/19/2016.
 */
public class FavoriteMoviesDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favoriteMovies";
    SQLiteDatabase favoriteMoviesDB;
    final String delimiter = "ArrayDivider";
    String trailerKey, trailerName, reviewAuthor, reviewContent;

    public FavoriteMoviesDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table favoriteMovies (id text primary key," + "title text not null," + "releaseDate text not null," +
                "poster text not null," + "voteAvg text not null," + "plot text not null," + "trailerName text," +
                "trailerKey text," + "reviewAuthor text," + "reviewContent text);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists favoriteMovies");
        onCreate(db);
    }

    public void addMovie(MovieData movieData) {
        favoriteMoviesDB = getWritableDatabase();

        String[] idArr = {movieData.getId()};

        Cursor c = favoriteMoviesDB.rawQuery("Select title from favoriteMovies where id like ?", idArr);

        if (c.getCount() == 0) {

            ContentValues row = new ContentValues();

            row.put("id", movieData.getId());
            row.put("title", movieData.getTitle());
            row.put("releaseDate", movieData.getReleaseDate());
            row.put("poster", movieData.getPoster());
            row.put("voteAvg", movieData.getVoteAverage());
            row.put("plot", movieData.getPlot());
            trailerName = TextUtils.join(delimiter, movieData.getTrailersName());
            row.put("trailerName", TextUtils.join(delimiter, movieData.getTrailersName()));
            trailerKey = TextUtils.join(delimiter, movieData.getTrailersKey());
            row.put("trailerKey", TextUtils.join(delimiter, movieData.getTrailersKey()));
            reviewAuthor = TextUtils.join(delimiter, movieData.getReviewsAuthor());
            row.put("reviewAuthor", TextUtils.join(delimiter, movieData.getReviewsAuthor()));
            reviewContent = TextUtils.join(delimiter, movieData.getReviewsContent());
            row.put("reviewContent", TextUtils.join(delimiter, movieData.getReviewsContent()));

            favoriteMoviesDB.insert("favoriteMovies", null, row);
        }
        c.close();
        favoriteMoviesDB.close();
    }

    public Boolean isMovieInDB(String id){
        favoriteMoviesDB = getWritableDatabase();

        String[] idArr = {id};

        Cursor c = favoriteMoviesDB.rawQuery("Select title from favoriteMovies where id like ?", idArr);

        if (c.getCount() != 0) {
            c.close();
            return true;
        }
        else{
            c.close();
            return false;
        }
    }

    public MovieData[] fetchAllMovies() {
        favoriteMoviesDB = getReadableDatabase();
        MovieData[] movieData;
        String[] columns = {"id","title","releaseDate","poster","voteAvg","plot",
                "trailerName","trailerKey","reviewAuthor","reviewContent"};
        long numRows = DatabaseUtils.longForQuery(favoriteMoviesDB, "SELECT COUNT(*) FROM favoriteMovies", null);
        movieData = new MovieData[(int)numRows];
        Cursor cursor = favoriteMoviesDB.query("favoriteMovies", columns, null, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        for(int i = 0; i<numRows ;i++) {
            movieData[i] = new MovieData();
            movieData[i].setId(cursor.getString(0));
            movieData[i].setTitle(cursor.getString(1));
            movieData[i].setReleaseDate(cursor.getString(2));
            movieData[i].setPoster(cursor.getString(3));
            movieData[i].setVoteAverage(cursor.getString(4));
            movieData[i].setPlot(cursor.getString(5));
            movieData[i].setTrailersName(cursor.getString(6).split(delimiter));
            movieData[i].setTrailersKey(cursor.getString(7).split(delimiter));
            movieData[i].setReviewsAuthor(cursor.getString(8).split(delimiter));
            movieData[i].setReviewsContent(cursor.getString(9).split(delimiter));
            cursor.moveToNext();
        }
//        Log.d("FAVORITE MOVIE TITLE",movieData[0].getPoster());
        cursor.close();
        favoriteMoviesDB.close();
        return movieData;
    }

    public void deleteAll() {
        favoriteMoviesDB = getWritableDatabase();
        favoriteMoviesDB.execSQL("DELETE FROM favoriteMovies");
        favoriteMoviesDB.close();
    }

}