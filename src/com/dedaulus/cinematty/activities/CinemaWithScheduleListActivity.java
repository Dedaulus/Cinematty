package com.dedaulus.cinematty.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.adapters.CinemaItemWithScheduleAdapter;
import com.dedaulus.cinematty.activities.adapters.LocationAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 21:27
 */
public class CinemaWithScheduleListActivity extends SherlockActivity implements LocationClient {
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private LocationState locationState;
    private SortableAdapter<Cinema> cinemaListAdapter;
    private ActivityState state;
    private String stateId;
    private int currentDay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.cinemas_caption));

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this) != SyncStatus.OK) {
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

        ListView list = (ListView)findViewById(R.id.cinema_list);

        switch (state.activityType) {
        case ActivityState.CINEMA_LIST_W_MOVIE:
            setCurrentDay(settings.getCurrentDay());

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onScheduleItemClick(adapterView, view, i, l);
                }
            });
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }
    }

    @Override
    protected void onResume() {
        locationState.startLocationListening();
        locationState.addLocationClient(this);
        if (currentDay != settings.getCurrentDay()) {
            setCurrentDay(settings.getCurrentDay());
        }
        cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
        super.onResume();
    }

    @Override
    protected void onPause() {
        locationState.removeLocationClient(this);
        locationState.stopLocationListening();
        settings.saveFavouriteCinemas();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getSupportMenuInflater();

        inflater.inflate(R.menu.select_day_menu, menu);
        switch (currentDay) {
            case Constants.TODAY_SCHEDULE:
                menu.findItem(R.id.submenu_select_day_today).setChecked(true);
                break;
            
            case Constants.TOMORROW_SCHEDULE:
                menu.findItem(R.id.submenu_select_day_tomorrow).setChecked(true);
                break;
        }
        
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
        }

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
            
            case R.id.menu_select_day:
                return true;

            case R.id.submenu_select_day_today:
                setCurrentDay(Constants.TODAY_SCHEDULE);
                cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                item.setChecked(true);
                return true;

            case R.id.submenu_select_day_tomorrow:
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
                cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                item.setChecked(true);
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
                app.showAbout(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CinemaItemWithScheduleAdapter adapter = (CinemaItemWithScheduleAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        Cinema cinema = (Cinema)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.cinema = cinema;
        state.activityType = ActivityState.MOVIE_LIST_W_CINEMA;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(this, MovieWithScheduleListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    private void setCurrentDay(int day) {
        settings.setCurrentDay(day);
        currentDay = day;

        Map<String, Cinema> cinemas = state.movie.getCinemas(settings.getCurrentDay());
        if (cinemas.isEmpty()) {
            findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_schedule).setVisibility(View.GONE);
        }
        IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
        cinemaListAdapter = new CinemaItemWithScheduleAdapter(this, notifier, new ArrayList<Cinema>(cinemas.values()), state.movie, settings.getCurrentDay(), locationState.getCurrentLocation());
        ListView list = (ListView)findViewById(R.id.cinema_list);
        list.setAdapter(cinemaListAdapter);
        list.setOnScrollListener(notifier);
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter) cinemaListAdapter;
        adapter.setLocation(location);
    }
}