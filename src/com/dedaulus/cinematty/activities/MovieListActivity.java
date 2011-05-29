package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.activities.adapters.MovieItemWithScheduleAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.CurrentState;
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
    CurrentState mCurrentState;

    @Override
    protected void onResume() {
        mCurrentState = mApp.getCurrentState();

        super.onResume();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);

        mApp = (CinemattyApplication)getApplication();
        mCurrentState = mApp.getCurrentState();

        View captionView = findViewById(R.id.cinema_panel_in_movie_list);
        TextView captionLabel = (TextView)findViewById(R.id.cinema_caption_in_movie_list);
        View iconView = findViewById(R.id.select_cinema_ico);
        ListView list = (ListView)findViewById(R.id.movie_list);

        if (mCurrentState.cinema != null) {
            captionLabel.setText(mCurrentState.cinema.getCaption());
            captionView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onCinemaClick(view);
                }
            });

            mScopeMovies = mCurrentState.cinema.getMovies();
            list.setAdapter(new MovieItemWithScheduleAdapter(this, new ArrayList<Movie>(mScopeMovies), mCurrentState.cinema, mApp.getPictureRetriever()));

            captionView.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);

            if (mCurrentState.actor != null) {
                captionLabel.setText(mCurrentState.actor.getActor());
                mScopeMovies = mCurrentState.actor.getMovies();

                captionView.setVisibility(View.VISIBLE);
            } else if (mCurrentState.genre != null) {
                captionLabel.setText(mCurrentState.genre.getGenre());
                mScopeMovies = mCurrentState.genre.getMovies();

                captionView.setVisibility(View.VISIBLE);
            } else {
                mScopeMovies = mApp.getMovies();

                captionView.setVisibility(View.GONE);
            }

            list.setAdapter(new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies), mApp.getPictureRetriever()));
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(view);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.revertCurrentState();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void onCinemaClick(View view) {
        CurrentState state = mCurrentState.clone();
        mApp.setCurrentState(state);

        Intent intent = new Intent(this, CinemaActivity.class);
        startActivity(intent);
    }

    private void onMovieItemClick(View view) {
        TextView textView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        String caption = textView.getText().toString();
        int movieId = mScopeMovies.indexOf(new Movie(caption));
        if (movieId != -1) {
            CurrentState state = mCurrentState.clone();
            state.movie = mScopeMovies.get(movieId);
            mApp.setCurrentState(state);

            Intent intent = new Intent(this, MovieActivity.class);
            startActivity(intent);
        }
    }
}