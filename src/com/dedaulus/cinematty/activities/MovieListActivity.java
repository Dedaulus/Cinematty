package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:37
 */
public class MovieListActivity extends Activity {
    CinemattyApplication mApp;
    UniqueSortedList<Movie> mScopeMovies;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);

        mApp = (CinemattyApplication)getApplication();

        Cinema cinema = mApp.getCurrentCinema();
        MovieActor actor = mApp.getCurrentActor();
        MovieGenre genre = mApp.getCurrentGenre();

        TextView captionLabel = (TextView)findViewById(R.id.caption_label_in_movie_list);
        ListView list = (ListView)findViewById(R.id.movie_list);

        if (cinema != null) {
            captionLabel.setVisibility(View.VISIBLE);
            captionLabel.setText(cinema.getCaption());
            captionLabel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onCinemaClick(view);
                }
            });
            mScopeMovies = cinema.getMovies();
        }
        else if (actor != null) {
            captionLabel.setVisibility(View.VISIBLE);
            captionLabel.setText(actor.getActor());
            mScopeMovies = actor.getMovies();
        }
        else if (genre != null) {
            captionLabel.setVisibility(View.VISIBLE);
            captionLabel.setText(genre.getGenre());
            mScopeMovies = genre.getMovies();
        }
        else {
            captionLabel.setVisibility(View.GONE);
            mScopeMovies = mApp.getMovies();
        }

        list.setAdapter(new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies)));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(view);
            }
        });
    }

    private void onCinemaClick(View view) {
        Intent intent = new Intent(this, CinemaActivity.class);
        startActivity(intent);
    }

    private void onMovieItemClick(View view) {
        TextView textView = (TextView)view.findViewById(R.id.movie_item_in_list);
        String caption = textView.getText().toString();
        int movieId = mScopeMovies.indexOf(new Movie(caption));
        if (movieId != -1) {
            mApp.setCurrentMovie(mScopeMovies.get(movieId));
            Intent intent = new Intent(this, MovieActivity.class);
            startActivity(intent);
        }
    }
}