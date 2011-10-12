package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.activities.adapters.StoppableAndResumable;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:37
 */
public class MovieListActivity extends Activity {
    private CinemattyApplication mApp;
    private SortableAdapter<Movie> mMovieListAdapter;
    private UniqueSortedList<Movie> mScopeMovies;
    private ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);

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

        findViewById(R.id.movie_list_title).setVisibility(View.VISIBLE);
        View captionView = findViewById(R.id.cinema_panel_in_movie_list);
        TextView captionLabel = (TextView)findViewById(R.id.cinema_caption_in_movie_list);
        ListView list = (ListView)findViewById(R.id.movie_list);

        switch (mState.activityType) {
        case ActivityState.MOVIE_LIST_W_ACTOR:
            captionLabel.setText(mState.actor.getActor());
            mScopeMovies = mState.actor.getMovies();
            break;

        case ActivityState.MOVIE_LIST_W_GENRE:
            captionLabel.setText(mState.genre.getGenre());
            mScopeMovies = mState.genre.getMovies();
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }

        captionView.setVisibility(View.VISIBLE);
        mMovieListAdapter = new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies), mApp.getPictureRetriever());
        list.setAdapter(mMovieListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
        ((StoppableAndResumable)mMovieListAdapter).onResume();
        mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder(), Constants.TODAY_SCHEDULE));

        super.onResume();
    }

    @Override
    protected void onStop() {
        ((StoppableAndResumable)mMovieListAdapter).onStop();
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

        inflater.inflate(R.menu.movie_sort_menu, menu);
        switch (mApp.getMovieSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
            break;

        case BY_POPULAR:
            menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
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

        case R.id.menu_movie_sort:
            return true;

        case R.id.submenu_movie_sort_by_caption:
            mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION, mApp.getCurrentDay()));
            mApp.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_movie_sort_by_popular:
            mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, mApp.getCurrentDay()));
            mApp.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
            item.setChecked(true);
            return true;

        case R.id.menu_about:
            mApp.showAbout(this);
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

        ActivityState state = mState.clone();
        state.movie = movie;
        state.activityType = ActivityState.MOVIE_INFO;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onHomeButtonClick(View view) {
        mApp.goHome(this);
    }
}