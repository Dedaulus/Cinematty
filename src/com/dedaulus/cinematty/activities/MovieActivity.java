package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.Calendar;
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

        setCaption();

        setLength();

        setGenre();

        setActors();

        setDescription();

        setSchedule();

        Button btn = (Button)findViewById(R.id.show_schedules_button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onSchedulesBtnClick(view);
            }
        });

        if (mApp.getCurrentCinema() == null) {
            btn.setText(getString(R.string.look_for_schedule));
        }
        else {
            btn.setText(getString(R.string.look_for_all_schedule));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.setCurrentMovie(null);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setCaption() {
        TextView text = (TextView)findViewById(R.id.movie_caption);
        text.setText(mApp.getCurrentMovie().getCaption());
    }

    private void setSchedule() {
        TextView text = (TextView)findViewById(R.id.schedule_enum_for_one_cinema);

        if (mApp.getCurrentCinema() != null) {
            List<Calendar> showTimes = mApp.getCurrentCinema().getShowTimes().get(mApp.getCurrentMovie());
            if (showTimes != null) {
                text.setText(DataConverter.showTimesToSpannableString(showTimes));
            }

            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.GONE);
        }
    }

    private void setLength() {
        TextView text = (TextView)findViewById(R.id.movie_length);
        if (mApp.getCurrentMovie().getLengthInMinutes() != 0) {
            text.setText(mApp.getCurrentMovie().getLengthInMinutes() + " " + getString(R.string.minute));

            findViewById(R.id.movie_length_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_length_panel).setVisibility(View.GONE);
        }
    }

    private void setGenre() {
        TextView text = (TextView)findViewById(R.id.movie_genre);
        if (mApp.getCurrentMovie().getGenres().size() != 0) {
            text.setText(DataConverter.genresToString(mApp.getCurrentMovie().getGenres()));

            findViewById(R.id.movie_genre_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_genre_panel).setVisibility(View.GONE);
        }
    }

    private void setActors() {
        TextView text = (TextView)findViewById(R.id.movie_actors);
        if (mApp.getCurrentMovie().getActors().size() != 0) {
            text.setText(DataConverter.actorsToString(mApp.getCurrentMovie().getActors()));

            findViewById(R.id.movie_actors_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_actors_panel).setVisibility(View.GONE);
        }
    }

    private void setDescription() {
        TextView text = (TextView)findViewById(R.id.movie_description);
        if (mApp.getCurrentMovie().getDescription().length() != 0) {
            text.setText(mApp.getCurrentMovie().getDescription());

            findViewById(R.id.movie_description_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_description_panel).setVisibility(View.GONE);
        }
    }

    private void onSchedulesBtnClick(View view) {
        Intent intent = new Intent(this, CinemaListActivity.class);
        startActivity(intent);
    }
}