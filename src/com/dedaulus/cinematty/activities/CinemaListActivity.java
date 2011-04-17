package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.CinemaItemAdapter;
import com.dedaulus.cinematty.activities.adapters.CinemaItemWithScheduleAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;

import java.util.ArrayList;
import java.util.Collections;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 21:27
 */
public class CinemaListActivity extends Activity {
    private CinemattyApplication mApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);

        mApp = (CinemattyApplication)getApplication();
        Movie movie = mApp.getCurrentMovie();
        TextView movieLabel = (TextView)findViewById(R.id.movie_label_in_cinema_list);
        ListView list = (ListView)findViewById(R.id.cinema_list);

        if (movie == null) {
            movieLabel.setVisibility(View.GONE);

            list.setAdapter(new CinemaItemAdapter(this, new ArrayList<Cinema>(mApp.getCinemas())));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onCinemaItemClick(adapterView, view, i, l);
                }
            });
        }
        else {
            movieLabel.setVisibility(View.VISIBLE);
            movieLabel.setText(movie.getCaption());

            list.setAdapter(new CinemaItemWithScheduleAdapter(this, new ArrayList<Cinema>(movie.getCinemas()), movie));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onScheduleItemClick(adapterView, view, i, l);
                }
            });
        }
    }

    private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_item_in_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            mApp.setCurrentCinema(mApp.getCinemas().get(cinemaId));
            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }
    }

    private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_schedule_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            mApp.setCurrentCinema(mApp.getCinemas().get(cinemaId));
            Intent intent = new Intent(this, CinemaActivity.class);
            startActivity(intent);
        }
    }

    public void onFavIconClick(View view) {
        //((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
    }
}