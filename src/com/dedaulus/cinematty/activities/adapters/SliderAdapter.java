package com.dedaulus.cinematty.activities.adapters;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.jakewharton.android.viewpagerindicator.TitleProvider;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 19:57
 */
public class SliderAdapter extends PagerAdapter implements TitleProvider {
    private List<SliderPage> mPages;

    public SliderAdapter(List<SliderPage> pages) {
        mPages = pages;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public void startUpdate(View view) {
        int i = 0;
    }

    @Override
    public Object instantiateItem(View collection, int position) {
        View page = mPages.get(position).getView();
        ((ViewPager)collection).addView(page);
        return page;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        SliderPage page = mPages.get(position);
        page.onPause();
        ((ViewPager)collection).removeView((View)view);
    }

    @Override
    public void finishUpdate(View view) {
        int i = 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == ((View)o);
    }

    @Override
    public Parcelable saveState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void restoreState(Parcelable parcelable, ClassLoader classLoader) {
        int i = 0;
    }

    public String getTitle(int pos) {
        return mPages.get(pos).getTitle();
    }
}
