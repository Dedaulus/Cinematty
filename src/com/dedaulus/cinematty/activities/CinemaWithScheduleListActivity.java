package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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
public class CinemaWithScheduleListActivity extends Activity implements LocationClient {
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

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(CinemattyApplication.getDensityDpi(this)) != SyncStatus.OK) {
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

        findViewById(R.id.cinema_list_title).setVisibility(View.VISIBLE);
        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_cinema_list);
        ListView list = (ListView)findViewById(R.id.cinema_list);

        switch (state.activityType) {
        case ActivityState.CINEMA_LIST_W_MOVIE:
            movieLabel.setVisibility(View.VISIBLE);
            movieLabel.setText(state.movie.getName());

            setCurrentDay(settings.getCurrentDay());

            TextView textView = (TextView)findViewById(R.id.titlebar_caption);
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    registerForContextMenu(view);
                    view.showContextMenu();
                }
            });

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

    private void changeTitleBar() {
        findViewById(R.id.cinema_list_title_day).setVisibility(View.VISIBLE);
        TextView text = (TextView)findViewById(R.id.titlebar_caption);
        switch (settings.getCurrentDay()) {
        case Constants.TODAY_SCHEDULE:
            text.setText(R.string.today);
            break;
        case Constants.TOMORROW_SCHEDULE:
            text.setText(R.string.tomorrow);
            break;
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

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.home_menu, menu);

        inflater.inflate(R.menu.select_day_menu, menu);
        if (currentDay == Constants.TODAY_SCHEDULE) {
            menu.findItem(R.id.menu_day).setTitle(R.string.tomorrow);
        } else {
            menu.findItem(R.id.menu_day).setTitle(R.string.today);
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
        default:
            break;
        }

        inflater.inflate(R.menu.about_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_home:
            app.goHome(this);
            return true;

        case R.id.menu_day:
            setCurrentDay(currentDay == Constants.TODAY_SCHEDULE ? Constants.TOMORROW_SCHEDULE : Constants.TODAY_SCHEDULE);
            cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
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
        case R.id.menu_about:
            app.showAbout(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_day_submenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.submenu_select_day_today:
            if (currentDay != Constants.TODAY_SCHEDULE) {
                setCurrentDay(Constants.TODAY_SCHEDULE);
                cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
            }
            return true;
        case R.id.submenu_select_day_tomorrow:
            if (currentDay != Constants.TOMORROW_SCHEDULE) {
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
                cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
            }
            return true;
        default:
            return super.onContextItemSelected(item);
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

    public void onHomeButtonClick(View view) {
        app.goHome(this);
    }

    public void onDayButtonClick(View view) {
        registerForContextMenu(view);
        view.showContextMenu();
    }

    private void setCurrentDay(int day) {
        settings.setCurrentDay(day);
        currentDay = day;

        changeTitleBar();

        Map<String, Cinema> cinemas = state.movie.getCinemas(settings.getCurrentDay());
        if (cinemas.isEmpty()) {
            findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_schedule).setVisibility(View.GONE);
        }
        
        cinemaListAdapter = new CinemaItemWithScheduleAdapter(this, cinemas, state.movie, settings.getCurrentDay(), locationState.getCurrentLocation());
        ListView list = (ListView)findViewById(R.id.cinema_list);
        list.setAdapter(cinemaListAdapter);
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter) cinemaListAdapter;
        adapter.setCurrentLocation(location);
    }
}