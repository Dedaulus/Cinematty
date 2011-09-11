package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.*;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;

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
    private ViewPager mSlider;
    private SliderAdapter mAdapter;
    private List<SliderPage> mPages;
    private HashMap<Integer, Integer> mSlideIds;
    private Integer mCurrentPage = new Integer(0);

    private static final String CURRENT_SCREEN = "current_screen";
    private static final int SLIDERS_COUNT    = 6;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mApp = (CinemattyApplication)getApplication();

        mSlider = (ViewPager)findViewById(R.id.slider);

        mSlideIds = new HashMap<Integer, Integer>();
        mSlideIds.put(Constants.CATEGORIES_SLIDE, Constants.CATEGORIES_SLIDE);
        mSlideIds.put(Constants.WHATS_NEW_SLIDE, Constants.WHATS_NEW_SLIDE);
        mSlideIds.put(Constants.CINEMAS_SLIDE, Constants.CINEMAS_SLIDE);
        mSlideIds.put(Constants.MOVIES_SLIDE, Constants.MOVIES_SLIDE);
        mSlideIds.put(Constants.ACTORS_SLIDE, Constants.ACTORS_SLIDE);
        mSlideIds.put(Constants.GENRES_SLIDE, Constants.GENRES_SLIDE);

        mCurrentPage = mSlideIds.get(Constants.WHATS_NEW_SLIDE);

        mPages = new ArrayList<SliderPage>(SLIDERS_COUNT);
        mPages.add(new CategoriesPage(this, mApp, mSlider, mSlideIds));
        mPages.add(new WhatsNewPage(this, mApp));
        mPages.add(new CinemasPage(this, mApp));
        mPages.add(new MoviesPage(this, mApp));
        mPages.add(new ActorsPage(this, mApp));
        mPages.add(new GenresPage(this, mApp));

        mAdapter = new SliderAdapter(mPages);
        mSlider.setAdapter(mAdapter);
        mSlider.setCurrentItem(mSlideIds.get(Constants.WHATS_NEW_SLIDE));

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mSlider, mSlideIds.get(Constants.WHATS_NEW_SLIDE));

        PageChangeListenerProxy pageChangeListenerProxy = new PageChangeListenerProxy();
        pageChangeListenerProxy.addListener(indicator);
        pageChangeListenerProxy.addListener(this);
        mSlider.setOnPageChangeListener(pageChangeListenerProxy);
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
        mApp.stopListenLocation();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return mPages.get(getCurrentPage()).onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mPages.get(getCurrentPage()).onOptionsItemSelected(item);
    }

    public void onPageScrolled(int i, float v, int i1) {}

    public void onPageSelected(int i) {
        synchronized (mCurrentPage) {
            mCurrentPage = i;
        }
    }

    public void onPageScrollStateChanged(int i) {}

    public int getCurrentPage() {
        synchronized (mCurrentPage) {
            return mCurrentPage.intValue();
        }
    }
}