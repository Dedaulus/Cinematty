package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.MenuInflater;
import android.view.Surface;
import android.view.WindowManager;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.LocationState;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.*;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 2:15
 */
public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {
    private static final String FAKE_STATE_ID = "fake_state";
    private static final int SLIDERS_COUNT = 6;

    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private LocationState locationState;
    private SliderAdapter adapter;
    private List<SliderPage> pages;
    private Integer currentPage = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(CinemattyApplication.getDensityDpi(this)) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        activitiesState = app.getActivitiesState();
        locationState = app.getLocationState();

        activitiesState.setState(FAKE_STATE_ID, new ActivityState(0, null, null, null, null));

        ViewPager slider = (ViewPager)findViewById(R.id.slider);

        HashMap<Integer, Integer> slideIds = new HashMap<Integer, Integer>();
        slideIds.put(Constants.CATEGORIES_SLIDE, Constants.CATEGORIES_SLIDE);
        slideIds.put(Constants.WHATS_NEW_SLIDE, Constants.WHATS_NEW_SLIDE);
        slideIds.put(Constants.CINEMAS_SLIDE, Constants.CINEMAS_SLIDE);
        slideIds.put(Constants.MOVIES_SLIDE, Constants.MOVIES_SLIDE);
        slideIds.put(Constants.ACTORS_SLIDE, Constants.ACTORS_SLIDE);
        slideIds.put(Constants.GENRES_SLIDE, Constants.GENRES_SLIDE);

        currentPage = slideIds.get(Constants.WHATS_NEW_SLIDE);

        pages = new ArrayList<SliderPage>(SLIDERS_COUNT);
        pages.add(new CategoriesPage(this, app, slider, slideIds));
        pages.add(new WhatsNewPage(this, app));
        pages.add(new CinemasPage(this, app));
        pages.add(new MoviesPage(this, app));
        pages.add(new ActorsPage(this, app));
        pages.add(new GenresPage(this, app));

        adapter = new SliderAdapter(pages);
        slider.setAdapter(adapter);
        slider.setCurrentItem(slideIds.get(Constants.WHATS_NEW_SLIDE));

        PageChangeListenerProxy pageChangeListenerProxy = new PageChangeListenerProxy();

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.titles);
            indicator.setViewPager(slider, slideIds.get(Constants.WHATS_NEW_SLIDE));
            pageChangeListenerProxy.addListener(indicator);
        } else {
            TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.tabs);
            indicator.setViewPager(slider, slideIds.get(Constants.WHATS_NEW_SLIDE));
            pageChangeListenerProxy.addListener(indicator);
        }

        pageChangeListenerProxy.addListener(this);
        slider.setOnPageChangeListener(pageChangeListenerProxy);

        //app.showWhatsNewIfNeeded(this);
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

        locationState.stopLocationListening();
        super.onPause();
    }

    @Override
    protected void onStop() {
        for (SliderPage page : adapter.getCreatedPages()) {
            page.onStop();
        }

        locationState.stopLocationListening();
        activitiesState.dump();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getCurrentPage() == Constants.WHATS_NEW_SLIDE) {
            activitiesState.removeState(FAKE_STATE_ID);
            super.onBackPressed();
        } else {
            ViewPager slider = (ViewPager)findViewById(R.id.slider);
            slider.setCurrentItem(Constants.WHATS_NEW_SLIDE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();

        pages.get(getCurrentPage()).onCreateOptionsMenu(menu);

        inflater.inflate(R.menu.search_menu, menu);

        inflater.inflate(R.menu.preferences_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_preferences:
            app.showAbout(this);
            return true;

        default:
            return pages.get(getCurrentPage()).onOptionsItemSelected(item);
        }
    }

    public void onPageScrolled(int i, float v, int i1) {}

    public synchronized void onPageSelected(int i) {
        currentPage = i;
    }

    public void onPageScrollStateChanged(int i) {}

    public synchronized int getCurrentPage() {
        return currentPage;
    }
}