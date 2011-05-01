package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
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
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.CinemaComparator;
import com.dedaulus.cinematty.framework.tools.CinemaSortOrder;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 21:27
 */
public class CinemaListActivity extends Activity {
    private CinemattyApplication mApp;
    private SortableAdapter<Cinema> mCinemaListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_list);

        mApp = (CinemattyApplication)getApplication();
        Movie movie = mApp.getCurrentMovie();
        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_cinema_list);
        ListView list = (ListView)findViewById(R.id.cinema_list);

        if (movie == null) {
            movieLabel.setVisibility(View.GONE);

            mCinemaListAdapter = new CinemaItemAdapter(this, new ArrayList<Cinema>(mApp.getCinemas()));
            list.setAdapter(mCinemaListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onCinemaItemClick(adapterView, view, i, l);
                }
            });
        } else {
            movieLabel.setVisibility(View.VISIBLE);
            movieLabel.setText(movie.getCaption());

            mCinemaListAdapter = new CinemaItemWithScheduleAdapter(this, new ArrayList<Cinema>(movie.getCinemas()), movie);
            list.setAdapter(mCinemaListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onScheduleItemClick(adapterView, view, i, l);
                }
            });
        }

        mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder()));
    }

    @Override
    protected void onPause() {
        mApp.saveFavouriteCinemas();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cinema_list_menu, menu);

        switch (mApp.getCinemaSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_sort_by_caption).setChecked(true);
            break;

        case BY_FAVOURITE:
            menu.findItem(R.id.submenu_sort_by_favourite).setChecked(true);
            break;

        case BY_DISTANCE:
            break;

        default:
            break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_cinema_list_sort:
            return true;

        case R.id.submenu_sort_by_caption:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_CAPTION));
            mApp.saveCinemasSortOrder(CinemaSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_sort_by_favourite:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_FAVOURITE));
            mApp.saveCinemasSortOrder(CinemaSortOrder.BY_FAVOURITE);
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
            mApp.setCurrentCinema(mApp.getCinemas().get(cinemaId));
            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }
    }

    private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_schedule_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            mApp.setCurrentCinema(mApp.getCinemas().get(cinemaId));
            Intent intent = new Intent(this, CinemaActivity.class);
            startActivity(intent);
        }
    }

    public void onFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = null;

        if (mApp.getCurrentMovie() != null) {
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
}