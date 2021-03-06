package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.Pages.*;
import com.dedaulus.cinematty.activities.adapters.PageChangeListenerProxy;
import com.dedaulus.cinematty.activities.adapters.SliderAdapter;
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
public class MainActivity extends SherlockActivity implements ViewPager.OnPageChangeListener {
    private static final String FAKE_STATE_ID = "fake_state";

    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private LocationState locationState;
    private SliderAdapter adapter;
    private List<SliderPage> pages;
    private Integer currentPage = 0;

    {
        pages = new ArrayList<SliderPage>();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        ApplicationSettings settings = app.getSettings();
        activitiesState = app.getActivitiesState();
        locationState = app.getLocationState();

        activitiesState.setState(FAKE_STATE_ID, new ActivityState(0, null, null, null, null, null, null));

        ViewPager slider = (ViewPager)findViewById(R.id.slider);

        HashMap<Integer, Integer> slideIds = new HashMap<Integer, Integer>();
        slideIds.put(Constants.CINEMAS_SLIDE, Constants.CINEMAS_SLIDE);
        slideIds.put(Constants.WHATS_NEW_SLIDE, Constants.WHATS_NEW_SLIDE);
        slideIds.put(Constants.MOVIES_SLIDE, Constants.MOVIES_SLIDE);
        slideIds.put(Constants.GENRES_SLIDE, Constants.GENRES_SLIDE);
        slideIds.put(Constants.DIRECTORS_SLIDE, Constants.DIRECTORS_SLIDE);
        slideIds.put(Constants.ACTORS_SLIDE, Constants.ACTORS_SLIDE);

        pages.add(new CinemasPage(this, app));
        pages.add(new WhatsNewPage(this, app));
        pages.add(new MoviesPage(this, settings, activitiesState, app.getImageRetrievers().getMovieSmallImageRetriever()));
        pages.add(new GenresPage(this, app));
        pages.add(new DirectorsPage(this, app));
        pages.add(new ActorsPage(this, app));

        currentPage = slideIds.get(Constants.WHATS_NEW_SLIDE);

        adapter = new SliderAdapter(pages);
        slider.setAdapter(adapter);
        slider.setCurrentItem(slideIds.get(Constants.WHATS_NEW_SLIDE));
        pages.get(currentPage).setVisible(true);

        PageChangeListenerProxy pageChangeListenerProxy = new PageChangeListenerProxy();

        if (findViewById(R.id.titles) != null) {
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
        app.showRateUsIfNeeded(this);
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
            app.resetSyncStatus();
            super.onBackPressed();
        } else {
            ViewPager slider = (ViewPager)findViewById(R.id.slider);
            slider.setCurrentItem(Constants.WHATS_NEW_SLIDE);
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

    public void onPageScrolled(int i, float v, int i1) {}

    public void onPageSelected(int i) {
        currentPage = i;
        int j = 0;
        for (SliderPage page : adapter.getCreatedPages()) {
            page.setVisible(j == currentPage);
            ++j;
        }
        invalidateOptionsMenu();
    }

    public void onPageScrollStateChanged(int i) {}

    public int getCurrentPage() {
        return currentPage;
    }
}
