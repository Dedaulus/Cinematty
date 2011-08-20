package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.CinemaItemAdapter;
import com.dedaulus.cinematty.activities.adapters.CinemaItemWithScheduleAdapter;
import com.dedaulus.cinematty.activities.adapters.LocationAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 21:27
 */
public class CinemaListActivity extends Activity implements LocationClient {
    private CinemattyApplication mApp;
    private SortableAdapter<Cinema> mCinemaListAdapter;
    private ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);

        findViewById(R.id.cinema_list_title_arrow_left).setVisibility(View.GONE);
        findViewById(R.id.cinema_list_title_arrow_right).setVisibility(View.GONE);
        findViewById(R.id.cinema_list_title_home).setVisibility(View.VISIBLE);

        mApp = (CinemattyApplication)getApplication();
        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_cinema_list);
        ListView list = (ListView)findViewById(R.id.cinema_list);

        switch (mState.activityType) {
        case CINEMA_LIST:
            movieLabel.setVisibility(View.GONE);

            mCinemaListAdapter = new CinemaItemAdapter(this, new ArrayList<Cinema>(mApp.getCinemas()), mApp.getCurrentLocation());
            list.setAdapter(mCinemaListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onCinemaItemClick(adapterView, view, i, l);
                }
            });
            break;

        case CINEMA_LIST_W_MOVIE:
            movieLabel.setVisibility(View.VISIBLE);
            movieLabel.setText(mState.movie.getCaption());

            mCinemaListAdapter = new CinemaItemWithScheduleAdapter(this, new ArrayList<Cinema>(mState.movie.getCinemas()), mState.movie, mApp.getCurrentLocation());
            list.setAdapter(mCinemaListAdapter);
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
        mApp.startListenLocation();
        mApp.addLocationClient(this);
        mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));

        super.onResume();
    }

    @Override
    protected void onPause() {
        mApp.removeLocationClient(this);
        mApp.stopListenLocation();
        mApp.saveFavouriteCinemas();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cinema_list_menu, menu);

        switch (mApp.getCinemaSortOrder()) {
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_cinema_sort:
            return true;

        case R.id.submenu_cinema_sort_by_caption:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_CAPTION, null));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_cinema_sort_by_favourite:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_FAVOURITE, null));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_FAVOURITE);
            item.setChecked(true);
            return true;

        case R.id.submenu_cinema_sort_by_distance:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_DISTANCE, mApp.getCurrentLocation()));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_DISTANCE);
            item.setChecked(true);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_cinema_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = mState.clone();
            state.cinema = mApp.getCinemas().get(cinemaId);
            state.activityType = ActivityState.ActivityType.MOVIE_LIST_W_CINEMA;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_schedule_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = mState.clone();
            state.cinema = mApp.getCinemas().get(cinemaId);
            state.activityType = ActivityState.ActivityType.MOVIE_LIST_W_CINEMA;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onCinemaFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = null;

        if (mState.movie != null) {
            caption = (TextView)parent.findViewById(R.id.cinema_caption_in_schedule_list);
        } else {
            caption = (TextView)parent.findViewById(R.id.cinema_caption_in_cinema_list);
        }

        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption.getText().toString()));
        if (cinemaId != -1) {
            Cinema cinema = mApp.getCinemas().get(cinemaId);

            if (cinema.getFavourite() > 0) {
                cinema.setFavourite(false);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
            } else {
                cinema.setFavourite(true);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
            }
        }
    }

    public void onHomeButtonClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter)mCinemaListAdapter;
        adapter.setCurrentLocation(location);
    }
}