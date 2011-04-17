package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends Activity {
    CinemattyApplication mApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        mApp = (CinemattyApplication)getApplication();
        Movie movie = mApp.getCurrentMovie();

        TextView text = (TextView)findViewById(R.id.movie_caption);
        text.setText(movie.getCaption());

        text = (TextView)findViewById(R.id.movie_length);
        String movieLength = movie.getLengthInMinutes() == 0 ? getString(R.string.unknown) : movie.getLengthInMinutes() + " " + getString(R.string.minutes);
        text.setText(getString(R.string.movie_length) + ": " + movieLength);

        text = (TextView)findViewById(R.id.movie_genre);
        if (movie.getGenres().size() > 0) {
            StringBuilder genres = new StringBuilder(getString(R.string.genre) + ": ");
            for (MovieGenre genre : movie.getGenres()) {
                genres.append(genre.getGenre() + ", ");
            }
            genres.delete(genres.length() - 2, genres.length() - 1);
            text.setText(genres.toString());
        }
        else {
            text.setVisibility(View.GONE);
        }

        text = (TextView)findViewById(R.id.movie_actors);
        if (movie.getActors().size() > 0) {
            StringBuilder actors = new StringBuilder(getString(R.string.actors) + ": ");
            for (MovieActor actor : movie.getActors()) {
                actors.append(actor.getActor() + ", ");
            }
            actors.delete(actors.length() - 2, actors.length() - 1);
            text.setText(actors.toString());
        }
        else {
            text.setVisibility(View.GONE);
        }

        text = (TextView)findViewById(R.id.movie_description);
        text.setText(movie.getDescription());

        Button btn = (Button)findViewById(R.id.show_schedules_button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onSchedulesBtnClick(view);
            }
        });
        if (mApp.getCurrentCinema() == null) {
            btn.setText(getString(R.string.look_for_schedule));
            //findViewById(R.id.advanced_movie_panel).setVisibility(View.VISIBLE);
            findViewById(R.id.schedule_enum_for_one_cinema).setVisibility(View.GONE);

            /*
            Button btn = (Button)findViewById(R.id.show_schedules_button);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onSchedulesBtnClick(view);
                }
            });
            */
        }
        else {
            btn.setText(getString(R.string.look_for_all_schedule));
            //findViewById(R.id.advanced_movie_panel).setVisibility(View.GONE);
            text = (TextView)findViewById(R.id.schedule_enum_for_one_cinema);
            text.setVisibility(View.VISIBLE);

            List<String> showTime = mApp.getCurrentCinema().getShowTimes().get(movie);
            if (showTime != null) {
                StringBuilder times = new StringBuilder();
                    for (String str : showTime) {
                        times.append(str + ", ");
                    }
                    times.delete(times.length() - 2, times.length() - 1);
                    text.setText(times.toString());
            }

        }
    }

    private void onSchedulesBtnClick(View view) {
        Intent intent = new Intent(this, CinemaListActivity.class);
        startActivity(intent);
    }
}