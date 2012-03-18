package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.Pages.CinemasWithSchedulePage;
import com.dedaulus.cinematty.activities.Pages.FramesPage;
import com.dedaulus.cinematty.activities.Pages.MoviePage;
import com.dedaulus.cinematty.activities.Pages.SliderPage;
import com.dedaulus.cinematty.activities.adapters.PageChangeListenerProxy;
import com.dedaulus.cinematty.activities.adapters.SliderAdapter;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.*;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends SherlockActivity implements ViewPager.OnPageChangeListener {
    private static int FRAMES_PAGE_ID      = 0;
    private static int DESCRIPTION_PAGE_ID = 1;
    private static int SHOWTIME_PAGE_ID    = 2;
    
    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private SliderAdapter adapter;
    private List<SliderPage> pages;
    private Integer currentPage = 0;
    int defaultPagePosition;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.movie_caption));

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

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

        pages.add(new MoviePage(this, app, state));
        pages.add(new CinemasWithSchedulePage(this, app, state));

        adapter = new SliderAdapter(pages);
        slider.setAdapter(adapter);
        slider.setCurrentItem(defaultPagePosition);

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

        pages.get(getCurrentPage()).onCreateOptionsMenu(menu);

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

            case R.id.menu_search:
                onSearchRequested();
                return true;

            case R.id.menu_preferences:
                app.showPreferences(this);
                return true;

            default:
                return pages.get(getCurrentPage()).onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public synchronized int getCurrentPage() {
        return currentPage;
    }
}