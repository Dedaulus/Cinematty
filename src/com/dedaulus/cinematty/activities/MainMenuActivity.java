package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.tools.CurrentState;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 2:15
 */
public class MainMenuActivity extends Activity {
    private ProgressDialog mDialog;
    private CinemattyApplication mApp;
    private Handler handler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        mApp = (CinemattyApplication)getApplication();

        TextView cityLabel = (TextView)findViewById(R.id.city_caption);
        cityLabel.setText(mApp.getCurrentCity().getName());

        TextView cinemasLabel = (TextView)findViewById(R.id.cinemas_label);
        cinemasLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onCinemaClick(view);
            }
        });

        TextView moviesLabel = (TextView)findViewById(R.id.movies_label);
        moviesLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onMoviesClick(view);
            }
        });

        TextView actorsLabel = (TextView)findViewById(R.id.actors_label);
        actorsLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onActorsClick(view);
            }
        });

        TextView genresLabel = (TextView)findViewById(R.id.genres_label);
        genresLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onGenresClick(view);
            }
        });

    }

    public void onCityClick(View view) {
        deleteFile(getString(R.string.cities_file));

        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCinemaClick(View view) {
        mApp.setCurrentState(new CurrentState(null, null, null, null));
        Intent intent = new Intent(this, CinemaListActivity.class);
        startActivity(intent);
    }

    public void onMoviesClick(View view) {
        mApp.setCurrentState(new CurrentState(null, null, null, null));
        Intent intent = new Intent(this, MovieListActivity.class);
        startActivity(intent);
    }

    public void onActorsClick(View view) {
        mApp.setCurrentState(new CurrentState(null, null, null, null));
        Intent intent = new Intent(this, ActorListActivity.class);
        startActivity(intent);
    }


    public void onGenresClick(View view) {
        mApp.setCurrentState(new CurrentState(null, null, null, null));
        Intent intent = new Intent(this, GenreListActivity.class);
        startActivity(intent);
    }
}