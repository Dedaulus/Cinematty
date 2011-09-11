package com.dedaulus.cinematty.activities.adapters;

import android.widget.ListAdapter;

import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 23.04.11
 * Time: 18:47
 */
public interface SortableAdapter<T> extends ListAdapter {
    void sortBy(Comparator<T> comparator);
}
