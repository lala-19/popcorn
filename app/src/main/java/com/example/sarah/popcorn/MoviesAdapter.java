package com.example.sarah.popcorn;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Sarah on 8/13/2016.
 */
public class MoviesAdapter extends BaseAdapter {
    private Context context;
    String[] imagesURL;

    public MoviesAdapter(Context context) {
        this.context=context;
    }

    @Override
    public int getCount() { return imagesURL.length; }

    @Override
    public String getItem(int position) { return imagesURL[position]; }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridItem;

        if (convertView == null) {
            gridItem = inflater.inflate(R.layout.grid_item_movie, null);

        }
        else {
            gridItem = convertView;
        }
        ImageView imageView = (ImageView)gridItem.findViewById(R.id.movie_image_view);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Picasso.with(context)
                .load(imagesURL[position])
                .into(imageView);
        Log.d("Image URL",imagesURL[position]);
        return gridItem;
    }

    public String[] imagesStringArray(MovieData[] movie){

        imagesURL = new String[movie.length];

        for(int i=0;i<movie.length;i++){
            imagesURL[i]=movie[i].getPoster();
        }

        return imagesURL;
    }

}
