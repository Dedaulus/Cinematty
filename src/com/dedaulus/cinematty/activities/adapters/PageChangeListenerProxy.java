package com.dedaulus.cinematty.activities.adapters;

import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 09.09.11
 * Time: 0:24
 */
public class PageChangeListenerProxy implements ViewPager.OnPageChangeListener {
    private List<ViewPager.OnPageChangeListener> listeners = new ArrayList<ViewPager.OnPageChangeListener>();

    public void addListener(ViewPager.OnPageChangeListener listener) {
        listeners.add(listener);
    }

    public void onPageScrolled(int i, float v, int i1) {
        for (ViewPager.OnPageChangeListener listener : listeners) {
            listener.onPageScrolled(i, v, i1);
        }
    }

    public void onPageSelected(int i) {
        for (ViewPager.OnPageChangeListener listener : listeners) {
            listener.onPageSelected(i);
        }
    }

    public void onPageScrollStateChanged(int i) {
        for (ViewPager.OnPageChangeListener listener : listeners) {
            listener.onPageScrollStateChanged(i);
        }
    }
}
