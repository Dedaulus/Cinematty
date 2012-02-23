package com.dedaulus.cinematty.activities.adapters;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.dedaulus.cinematty.activities.Pages.SliderPage;
import com.viewpagerindicator.TitleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 19:57
 */
public class SliderAdapter extends PagerAdapter implements TitleProvider {
    private List<SliderPage> pages;
    private List<SliderPage> createdPages;

    {
        createdPages = new ArrayList<SliderPage>();
    }

    public SliderAdapter(List<SliderPage> pages) {
        this.pages = pages;
    }

    public List<SliderPage> getCreatedPages() {
        return createdPages;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public void startUpdate(View view) {}

    @Override
    public Object instantiateItem(View collection, int position) {
        SliderPage page = pages.get(position);
        View view = page.getView();
        ((ViewPager)collection).addView(view);
        createdPages.add(page);
        return view;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        SliderPage page = pages.get(position);
        page.onPause();
        page.onStop();
        ((ViewPager)collection).removeView((View) view);
        createdPages.remove(page);
    }

    @Override
    public void finishUpdate(View view) {}

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable parcelable, ClassLoader classLoader) {}

    public String getTitle(int pos) {
        return pages.get(pos).getTitle();
    }
}
