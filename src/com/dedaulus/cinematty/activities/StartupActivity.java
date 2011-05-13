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

public class StartupActivity extends Activity
{
    private ProgressDialog mDialog;
    private CinemattyApplication mApp;
    private Handler handler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mApp = (CinemattyApplication)getApplication();

        if (!mApp.isUpToDate()) {
            mDialog = ProgressDialog.show(this, getString(R.string.wait_dialog_caption), getString(R.string.wait_dialog_text), true, true);
            new Thread(new Runnable() {
                public void run() {
                    retrieveData();
                }
            }).start();
        }

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

    public void retrieveData() {
        try {
            ((CinemattyApplication)getApplication()).retrieveData();
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
        catch (Exception e) {
            if (mDialog != null) {
                mDialog.setMessage(e.toString());
            }
        }
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
