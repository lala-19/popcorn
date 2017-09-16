package com.example.sarah.popcorn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    MovieData movieData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (!MainActivity.isOnline()){
            Toast.makeText(this, "Please check you internet connection", Toast.LENGTH_LONG).show();
        }

        if (savedInstanceState == null) {
            Intent i = this.getIntent();
            movieData = (MovieData)i.getSerializableExtra("movie");
            Bundle arguments = new Bundle();
            arguments.putSerializable("movie",movieData);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, fragment)
                    .commit();

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!MainActivity.isOnline()){
            Toast.makeText(this, "Please check you internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!MainActivity.isOnline()){
            Toast.makeText(this, "Please check you internet connection", Toast.LENGTH_LONG).show();
        }
    }


}
