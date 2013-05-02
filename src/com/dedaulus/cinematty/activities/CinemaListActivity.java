package com.dedaulus.cinematty.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.adapters.CinemaItemAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * User: dedaulus
 * Date: 02.05.13
 * Time: 19:24
 */
public class CinemaListActivity extends SherlockActivity implements LocationClient {
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private LocationState locationState;
    private CinemaItemAdapter cinemaListAdapter;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
        locationState = app.getLocationState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

        Map<String, Cinema> scopeCinemas;
        switch (state.activityType) {
            case ActivityState.CINEMA_LIST_W_METRO: {
                setContentView(R.layout.cinema_list);
                findViewById(R.id.day_indicator).setVisibility(View.GONE);
                actionBar.setTitle(state.metro.getName());
                scopeCinemas = state.metro.getCinemas();
            }
            break;

            default:
                throw new RuntimeException("ActivityType error");
        }

        IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
        cinemaListAdapter = new CinemaItemAdapter(
                this,
                notifier,
                new ArrayList<Cinema>(scopeCinemas.values()),
                locationState.getCurrentLocation());
        GridView grid = (GridView)findViewById(R.id.cinema_list);
        grid.setAdapter(cinemaListAdapter);
        grid.setOnScrollListener(notifier);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onCinemaItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationState.startLocationListening();
        locationState.addLocationClient(this);
        cinemaListAdapter.sortBy(
                new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationState.removeLocationClient(this);
        locationState.stopLocationListening();
        settings.saveFavouriteCinemas();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getSupportMenuInflater();

        inflater.inflate(R.menu.search_menu, menu);

        if (settings != null) {
            inflater.inflate(R.menu.cinema_sort_menu, menu);
            switch (settings.getCinemaSortOrder()) {
                case BY_CAPTION:
                    menu.findItem(R.id.submenu_cinema_sort_by_caption).setChecked(true);
                    break;

                case BY_FAVOURITE:
                    menu.findItem(R.id.submenu_cinema_sort_by_favourite).setChecked(true);
                    break;

                case BY_DISTANCE:
                    menu.findItem(R.id.submenu_cinema_sort_by_distance).setChecked(true);
                    break;

                default:
                    break;
            }
        }

        inflater.inflate(R.menu.preferences_menu, menu);

        inflater.inflate(R.menu.problem_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                app.goHome(this);
                return true;

            case R.id.menu_cinema_sort:
                return true;

            case R.id.submenu_cinema_sort_by_caption:
                cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_CAPTION, null));
                settings.saveCinemaSortOrder(CinemaSortOrder.BY_CAPTION);
                item.setChecked(true);
                return true;

            case R.id.submenu_cinema_sort_by_favourite:
                cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_FAVOURITE, null));
                settings.saveCinemaSortOrder(CinemaSortOrder.BY_FAVOURITE);
                item.setChecked(true);
                return true;

            case R.id.submenu_cinema_sort_by_distance:
                cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_DISTANCE, locationState.getCurrentLocation()));
                settings.saveCinemaSortOrder(CinemaSortOrder.BY_DISTANCE);
                item.setChecked(true);
                return true;

            case R.id.menu_search:
                onSearchRequested();
                return true;

            case R.id.menu_preferences:
                app.showPreferences(this);
                return true;

            case R.id.menu_problem:
                app.showProblemResolver(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CinemaItemAdapter adapter = (CinemaItemAdapter)adapterView.getAdapter();
        Cinema cinema = (Cinema)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.activityType = ActivityState.MOVIE_LIST_W_CINEMA;
        state.cinema = cinema;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(this, CinemaActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_SHOWTIME_PAGE_ID);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        cinemaListAdapter.setLocation(location);
    }
}