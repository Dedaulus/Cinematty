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
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.activities.adapters.StoppableAndResumable;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:37
 */
public class MovieListActivity extends FragmentActivity {
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private SortableAdapter<Movie> movieListAdapter;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.movies_caption));

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

        View captionView = findViewById(R.id.cinema_panel_in_movie_list);
        TextView captionLabel = (TextView)findViewById(R.id.cinema_caption_in_movie_list);
        ListView list = (ListView)findViewById(R.id.movie_list);
        Map<String, Movie> scopeMovies;

        switch (state.activityType) {
        case ActivityState.MOVIE_LIST_W_ACTOR:
            captionLabel.setText(state.actor.getName());
            scopeMovies = state.actor.getMovies();
            break;

        case ActivityState.MOVIE_LIST_W_GENRE:
            captionLabel.setText(state.genre.getName());
            scopeMovies = state.genre.getMovies();
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }

        captionView.setVisibility(View.VISIBLE);
        movieListAdapter = new MovieItemAdapter(this, new ArrayList<Movie>(scopeMovies.values()), app.getImageRetrievers().getMovieSmallImageRetriever());
        list.setAdapter(movieListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
        ((StoppableAndResumable)movieListAdapter).onResume();
        movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), Constants.TODAY_SCHEDULE));

        super.onResume();
    }

    @Override
    protected void onStop() {
        ((StoppableAndResumable)movieListAdapter).onStop();
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

        inflater.inflate(R.menu.movie_sort_menu, menu);
        switch (settings.getMovieSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
            break;
        case BY_POPULAR:
            menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
            break;
        case BY_RATING:
            menu.findItem(R.id.submenu_movie_sort_by_rating).setChecked(true);
            break;
        default:
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

            case R.id.menu_movie_sort:
                return true;

            case R.id.submenu_movie_sort_by_caption:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION, settings.getCurrentDay()));
                settings.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_popular:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, settings.getCurrentDay()));
                settings.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_rating:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_RATING, settings.getCurrentDay()));
                settings.saveMovieSortOrder(MovieSortOrder.BY_RATING);
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

    private void onMovieItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        MovieItemAdapter adapter = (MovieItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        Movie movie = (Movie)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.movie = movie;
        state.activityType = ActivityState.MOVIE_INFO;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onHomeButtonClick(View view) {
        app.goHome(this);
    }
}