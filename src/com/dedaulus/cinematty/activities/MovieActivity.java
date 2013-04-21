package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.Pages.CinemasWithSchedulePage;
import com.dedaulus.cinematty.activities.Pages.FramesPage;
import com.dedaulus.cinematty.activities.Pages.MoviePage;
import com.dedaulus.cinematty.activities.Pages.SliderPage;
import com.dedaulus.cinematty.activities.adapters.PageChangeListenerProxy;
import com.dedaulus.cinematty.activities.adapters.SliderAdapter;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends SherlockActivity implements ViewPager.OnPageChangeListener {
    public static final String DAY_ID = "day_id";

    private static final int DESCRIPTION_PAGE_ID = 1;
    
    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private SliderAdapter adapter;
    private List<SliderPage> pages;
    private Integer currentPage = 0;
    int defaultPagePosition;
    private String stateId;

    {
        pages = new ArrayList<SliderPage>();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        ApplicationSettings settings = app.getSettings();
        activitiesState = app.getActivitiesState();
        LocationState locationState = app.getLocationState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        ActivityState state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");
        if (state.activityType != ActivityState.MOVIE_INFO && state.activityType != ActivityState.MOVIE_INFO_W_SCHED) {
            throw new RuntimeException("ActivityType error");
        }

        actionBar.setTitle(state.movie.getName());

        ViewPager slider = (ViewPager)findViewById(R.id.slider);

        pages = new ArrayList<SliderPage>();
        defaultPagePosition = DESCRIPTION_PAGE_ID;
        MovieFrameIdsStore frameIdsStore = state.movie.getFrameIdsStore();
        if (state.movie.getFrameIdsStore() != null) {
            pages.add(new FramesPage(this, app, frameIdsStore, state));
        } else {
            defaultPagePosition -= 1;
        }
        currentPage = defaultPagePosition;

        int currentDay = getIntent().getIntExtra(DAY_ID, 0);

        pages.add(new MoviePage(this, app, state, currentDay));
        pages.add(new CinemasWithSchedulePage(this, settings, activitiesState, locationState, state));

        adapter = new SliderAdapter(pages);
        slider.setAdapter(adapter);
        slider.setCurrentItem(defaultPagePosition);
        pages.get(currentPage).setVisible(true);

        PageChangeListenerProxy pageChangeListenerProxy = new PageChangeListenerProxy();
        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.titles);
        indicator.setViewPager(slider, defaultPagePosition);
        pageChangeListenerProxy.addListener(indicator);

        pageChangeListenerProxy.addListener(this);
        slider.setOnPageChangeListener(pageChangeListenerProxy);
    }

    @Override
    protected void onResume() {
        for (SliderPage page : adapter.getCreatedPages()) {
            page.onResume();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        for (SliderPage page : adapter.getCreatedPages()) {
            page.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        for (SliderPage page : adapter.getCreatedPages()) {
            page.onStop();
        }

        activitiesState.dump();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getCurrentPage() == defaultPagePosition) {
            activitiesState.removeState(stateId);
            super.onBackPressed();
        } else {
            ViewPager slider = (ViewPager)findViewById(R.id.slider);
            slider.setCurrentItem(defaultPagePosition);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getSupportMenuInflater();

        inflater.inflate(R.menu.search_menu, menu);

        if (!pages.isEmpty()) {
            pages.get(getCurrentPage()).onCreateOptionsMenu(menu);
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
                return pages.get(getCurrentPage()).onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public synchronized void onPageSelected(int position) {
        currentPage = position;
        int j = 0;
        for (SliderPage page : adapter.getCreatedPages()) {
            page.setVisible(j == currentPage);
            ++j;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public synchronized int getCurrentPage() {
        return currentPage;
    }
}
