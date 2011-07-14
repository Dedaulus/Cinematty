package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.UUID;

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
    }

    @Override
    protected void onPause() {
        mApp.stopListenLocation();

        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.stopListenLocation();
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onCityClick(View view) {
        deleteFile(getString(R.string.cities_file));

        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCinemaClick(View view) {
        String cookie = UUID.randomUUID().toString();
        mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.CINEMA_LIST, null, null, null, null));
        Intent intent = new Intent(this, CinemaListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onMoviesClick(View view) {
        String cookie = UUID.randomUUID().toString();
        mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.MOVIE_LIST, null, null, null, null));
        Intent intent = new Intent(this, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onActorsClick(View view) {
        String cookie = UUID.randomUUID().toString();
        mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.ACTOR_LIST, null, null, null, null));
        Intent intent = new Intent(this, ActorListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }


    public void onGenresClick(View view) {
        String cookie = UUID.randomUUID().toString();
        mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.GENRE_LIST, null, null, null, null));
        Intent intent = new Intent(this, GenreListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }
}