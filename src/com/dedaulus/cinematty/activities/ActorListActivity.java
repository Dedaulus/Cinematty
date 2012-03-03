package com.dedaulus.cinematty.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.adapters.ActorItemAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.Comparator;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:28
 */
public class ActorListActivity extends FragmentActivity {
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private SortableAdapter<MovieActor> actorListAdapter;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actor_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.actors_caption));

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(CinemattyApplication.getDensityDpi(this)) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_actor_list);
        ListView list = (ListView)findViewById(R.id.actor_list);

        switch (state.activityType) {
        case ActivityState.ACTOR_LIST:
            movieLabel.setVisibility(View.GONE);
            actorListAdapter = new ActorItemAdapter(this, settings.getActors());
            break;

        case ActivityState.ACTOR_LIST_W_MOVIE:
            movieLabel.setText(state.movie.getName());
            movieLabel.setVisibility(View.VISIBLE);
            actorListAdapter = new ActorItemAdapter(this, state.movie.getActors());
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }

        list.setAdapter(actorListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onActorItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
        actorListAdapter.sortBy(new Comparator<MovieActor>() {
            public int compare(MovieActor a1, MovieActor a2) {
                if (a1.getFavourite() == a2.getFavourite()) {
                    return a1.getName().compareTo(a2.getName());
                } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
            }
        });

        super.onResume();
    }

    @Override
    protected void onPause() {
        settings.saveFavouriteActors();

        super.onPause();
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
        MenuInflater inflater = getMenuInflater();

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
                app.showAbout(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ActorItemAdapter adapter = (ActorItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        MovieActor actor = (MovieActor)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.actor = actor;
        state.activityType = ActivityState.MOVIE_LIST_W_ACTOR;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(this, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }
}