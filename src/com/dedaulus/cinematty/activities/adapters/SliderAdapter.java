package com.dedaulus.cinematty.activities.adapters;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.jakewharton.android.viewpagerindicator.TitleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 19:57
 */
public class SliderAdapter extends PagerAdapter implements TitleProvider {
    private List<SliderPage> mPages;
    private List<SliderPage> mCreatedPages = new ArrayList<SliderPage>();

    public SliderAdapter(List<SliderPage> pages) {
        mPages = pages;
    }

    public List<SliderPage> getCreatedPages() {
        return mCreatedPages;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public void startUpdate(View view) {}

    @Override
    public Object instantiateItem(View collection, int position) {
        SliderPage page = mPages.get(position);
        View view = page.getView();
        ((ViewPager)collection).addView(view);
        mCreatedPages.add(page);
        return view;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        SliderPage page = mPages.get(position);
        page.onPause();
        page.onStop();
        ((ViewPager)collection).removeView((View) view);
        mCreatedPages.remove(page);
    }

    @Override
    public void finishUpdate(View view) {}

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == ((View)o);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable parcelable, ClassLoader classLoader) {}

    public String getTitle(int pos) {
        return mPages.get(pos).getTitle();
    }
}
