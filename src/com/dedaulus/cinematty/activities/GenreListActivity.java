package com.dedaulus.cinematty.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.GenreItemAdapter;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 5:12
 */
public class GenreListActivity extends SherlockActivity {
    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.genres_caption));

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_genre_list);
        ListView list = (ListView)findViewById(R.id.genre_list);

        switch (state.activityType) {
        case ActivityState.GENRE_LIST:
            movieLabel.setVisibility(View.GONE);
            list.setAdapter(new GenreItemAdapter(this, new ArrayList<MovieGenre>(app.getSettings().getGenres().values())));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onGenreItemClick(adapterView, view, i, l);
                }
            });
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }
    }

    @Override
    protected void onStop() {
        activitiesState.dump();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        activitiesState.removeState(stateId);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();

        inflater.inflate(R.menu.search_menu, menu);

        inflater.inflate(R.menu.preferences_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                app.goHome(this);
                return true;

            case R.id.menu_search:
                onSearchRequested();
                return true;

            case R.id.menu_preferences:
                app.showPreferences(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onGenreItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GenreItemAdapter adapter = (GenreItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        MovieGenre genre = (MovieGenre)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.genre = genre;
        state.activityType = ActivityState.MOVIE_LIST_W_GENRE;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(this, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }
}