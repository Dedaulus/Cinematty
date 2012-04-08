package com.dedaulus.cinematty.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.Pages.*;
import com.dedaulus.cinematty.activities.adapters.*;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.*;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 12.10.11
 * Time: 10:05
 */
public class CinemaActivity extends SherlockActivity implements ViewPager.OnPageChangeListener {
    private static int DESCRIPTION_PAGE_ID = 0;
    private static int SHOWTIME_PAGE_ID    = 1;

    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private SliderAdapter adapter;
    private List<SliderPage> pages;
    private Integer currentPage = 0;
    int defaultPagePosition;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        ActivityState state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");
        if (state.activityType != ActivityState.MOVIE_LIST_W_CINEMA) {
            throw new RuntimeException("ActivityType error");
        }

        actionBar.setTitle(state.cinema.getName());

        ViewPager slider = (ViewPager)findViewById(R.id.slider);

        pages = new ArrayList<SliderPage>();
        pages.add(new CinemaPage(this, activitiesState, state));
        pages.add(new MoviesWithSchedulePage(this, settings, activitiesState, state, app.getImageRetrievers().getMovieSmallImageRetriever()));
        defaultPagePosition = getIntent().getIntExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_SHOWTIME_PAGE_ID);
        currentPage = defaultPagePosition;

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

        inflater.inflate(R.menu.search_menu, menu);

        pages.get(getCurrentPage()).onCreateOptionsMenu(menu);

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
    public void onPageScrollStateChanged(int position) {}

    public synchronized int getCurrentPage() {
        return currentPage;
    }
}