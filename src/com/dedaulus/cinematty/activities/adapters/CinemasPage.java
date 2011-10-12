package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieWithScheduleListActivity;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:17
 */
public class CinemasPage implements SliderPage, LocationClient {
    private Context mContext;
    private CinemattyApplication mApp;
    private SortableAdapter<Cinema> mCinemaListAdapter;
    private boolean mBinded = false;

    public CinemasPage(Context context, CinemattyApplication app) {
        mContext = context;
        mApp = app;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.cinema_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return mContext.getString(R.string.cinemas_caption);
    }

    public void onResume() {
        if (mBinded) {
            mApp.startListenLocation();
            mApp.addLocationClient(this);
            mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));
        }
    }

    public void onPause() {
        mApp.removeLocationClient(this);
        mApp.stopListenLocation();
        mApp.saveFavouriteCinemas();
    }

    public void onStop() {}

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((Activity)mContext).getMenuInflater();
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

        return true;
    }

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
            return true;
        }
    }

    private View bindView(View view) {
        ListView list = (ListView)view.findViewById(R.id.cinema_list);
        mCinemaListAdapter = new CinemaItemAdapter(mContext, new ArrayList<Cinema>(mApp.getCinemas()), mApp.getCurrentLocation());
        list.setAdapter(mCinemaListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onCinemaItemClick(adapterView, view, i, l);
            }
        });
        mBinded = true;
        onResume();

        return view;
    }

    private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CinemaItemAdapter adapter = (CinemaItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        Cinema cinema = (Cinema)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_CINEMA, cinema, null, null, null);
        mApp.setState(cookie, state);

        Intent intent = new Intent(mContext, MovieWithScheduleListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        mContext.startActivity(intent);
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter)mCinemaListAdapter;
        adapter.setCurrentLocation(location);
    }
}
