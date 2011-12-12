package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.*;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 2:15
 */
public class MainActivity extends Activity implements ViewPager.OnPageChangeListener {
    private CinemattyApplication mApp;
    private SliderAdapter mAdapter;
    private List<SliderPage> mPages;
    private Integer mCurrentPage = 0;
    private static final String FAKE_STATE_ID = "fake_state";

    private static final int SLIDERS_COUNT = 6;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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

        mApp.setState(FAKE_STATE_ID, new ActivityState(0, null, null, null, null));

        ViewPager slider = (ViewPager)findViewById(R.id.slider);

        HashMap<Integer, Integer> slideIds = new HashMap<Integer, Integer>();
        slideIds.put(Constants.CATEGORIES_SLIDE, Constants.CATEGORIES_SLIDE);
        slideIds.put(Constants.WHATS_NEW_SLIDE, Constants.WHATS_NEW_SLIDE);
        slideIds.put(Constants.CINEMAS_SLIDE, Constants.CINEMAS_SLIDE);
        slideIds.put(Constants.MOVIES_SLIDE, Constants.MOVIES_SLIDE);
        slideIds.put(Constants.ACTORS_SLIDE, Constants.ACTORS_SLIDE);
        slideIds.put(Constants.GENRES_SLIDE, Constants.GENRES_SLIDE);

        mCurrentPage = slideIds.get(Constants.WHATS_NEW_SLIDE);

        mPages = new ArrayList<SliderPage>(SLIDERS_COUNT);
        mPages.add(new CategoriesPage(this, mApp, slider, slideIds));
        mPages.add(new WhatsNewPage(this, mApp));
        mPages.add(new CinemasPage(this, mApp));
        mPages.add(new MoviesPage(this, mApp));
        mPages.add(new ActorsPage(this, mApp));
        mPages.add(new GenresPage(this, mApp));

        mAdapter = new SliderAdapter(mPages);
        slider.setAdapter(mAdapter);
        slider.setCurrentItem(slideIds.get(Constants.WHATS_NEW_SLIDE));

        PageChangeListenerProxy pageChangeListenerProxy = new PageChangeListenerProxy();
        pageChangeListenerProxy.addListener(this);
        slider.setOnPageChangeListener(pageChangeListenerProxy);

        mApp.showWhatsNewIfNeeded(this);
    }

    @Override
    protected void onResume() {
        for (SliderPage page : mAdapter.getCreatedPages()) {
            page.onResume();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        for (SliderPage page : mAdapter.getCreatedPages()) {
            page.onPause();
        }

        mApp.stopListenLocation();
        super.onPause();
    }

    @Override
    protected void onStop() {
        for (SliderPage page : mAdapter.getCreatedPages()) {
            page.onStop();
        }

        mApp.stopListenLocation();
        mApp.dumpData();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getCurrentPage() == Constants.WHATS_NEW_SLIDE) {
            mApp.removeState(FAKE_STATE_ID);
            super.onBackPressed();
        } else {
            ViewPager slider = (ViewPager)findViewById(R.id.slider);
            slider.setCurrentItem(Constants.WHATS_NEW_SLIDE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        mPages.get(getCurrentPage()).onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_about:
            mApp.showAbout(this);
            return true;

        default:
            return mPages.get(getCurrentPage()).onOptionsItemSelected(item);
        }
    }

    public void onPageScrolled(int i, float v, int i1) {}

    public synchronized void onPageSelected(int i) {
        mCurrentPage = i;
    }

    public void onPageScrollStateChanged(int i) {}

    public synchronized int getCurrentPage() {
        return mCurrentPage;
    }
}