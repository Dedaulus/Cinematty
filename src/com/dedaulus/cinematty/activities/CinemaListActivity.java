package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
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
import java.util.List;
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
    private int mCurrentDay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);

        mApp = (CinemattyApplication)getApplication();
        if (!mApp.isDataActual()) {
            boolean b = false;
            try {
                b = mApp.retrieveData(true);
            } catch (Exception e) {}
            if (!b) {
                mApp.restart();
                finish();
                return;
            }
        }

        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        findViewById(R.id.cinema_list_title).setVisibility(View.VISIBLE);
        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_cinema_list);
        ListView list = (ListView)findViewById(R.id.cinema_list);

        switch (mState.activityType) {
        case ActivityState.CINEMA_LIST:
            movieLabel.setVisibility(View.GONE);

            mCinemaListAdapter = new CinemaItemAdapter(this, new ArrayList<Cinema>(mApp.getCinemas()), mApp.getCurrentLocation());
            list.setAdapter(mCinemaListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onCinemaItemClick(adapterView, view, i, l);
                }
            });
            break;

        case ActivityState.CINEMA_LIST_W_MOVIE:
            movieLabel.setVisibility(View.VISIBLE);
            movieLabel.setText(mState.movie.getCaption());

            setCurrentDay(mApp.getCurrentDay());

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
        switch (mApp.getCurrentDay()) {
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
        mApp.startListenLocation();
        mApp.addLocationClient(this);
        if (mState.activityType == ActivityState.CINEMA_LIST_W_MOVIE && mCurrentDay != mApp.getCurrentDay()) {
            setCurrentDay(mApp.getCurrentDay());
        }
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
    protected void onStop() {
        mApp.dumpData();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.home_menu, menu);

        if (mState.activityType == ActivityState.CINEMA_LIST_W_MOVIE) {
            inflater.inflate(R.menu.select_day_menu, menu);
            if (mCurrentDay == Constants.TODAY_SCHEDULE) {
                menu.findItem(R.id.menu_day).setTitle(R.string.tomorrow);
            } else {
                menu.findItem(R.id.menu_day).setTitle(R.string.today);
            }
        }

        inflater.inflate(R.menu.cinema_sort_menu, menu);
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

        inflater.inflate(R.menu.about_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_home:
            mApp.goHome(this);
            return true;

        case R.id.menu_day:
            setCurrentDay(mCurrentDay == Constants.TODAY_SCHEDULE ? Constants.TOMORROW_SCHEDULE : Constants.TODAY_SCHEDULE);
            mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));
            return true;

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

        case R.id.menu_about:
            mApp.showAbout(this);
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.submenu_select_day_today:
            if (mCurrentDay != Constants.TODAY_SCHEDULE) {
                setCurrentDay(Constants.TODAY_SCHEDULE);
                mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));
            }
            return true;
        case R.id.submenu_select_day_tomorrow:
            if (mCurrentDay != Constants.TOMORROW_SCHEDULE) {
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
                mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));
            }
            return true;
        default:
            return super.onContextItemSelected(item);
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
            state.activityType = ActivityState.MOVIE_LIST_W_CINEMA;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_cinema_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = mState.clone();
            state.cinema = mApp.getCinemas().get(cinemaId);
            state.activityType = ActivityState.MOVIE_LIST_W_CINEMA;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onCinemaFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption;
        if (mState.movie != null) {
            caption = (TextView)parent.findViewById(R.id.cinema_caption_in_cinema_list);
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
        mApp.goHome(this);
    }

    public void onDayButtonClick(View view) {
        registerForContextMenu(view);
        view.showContextMenu();
    }

    private void setCurrentDay(int day) {
        mApp.setCurrentDay(day);
        mCurrentDay = day;

        changeTitleBar();

        List<Cinema> cinemaList = mState.movie.getCinemas(mApp.getCurrentDay());
        if (cinemaList == null) {
            cinemaList = new ArrayList<Cinema>();
            findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_schedule).setVisibility(View.GONE);
        }
        mCinemaListAdapter = new CinemaItemWithScheduleAdapter(this, new ArrayList<Cinema>(cinemaList), mState.movie, mApp.getCurrentDay(), mApp.getCurrentLocation());
        ListView list = (ListView)findViewById(R.id.cinema_list);
        list.setAdapter(mCinemaListAdapter);
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter)mCinemaListAdapter;
        adapter.setCurrentLocation(location);
    }
}