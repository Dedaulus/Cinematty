package com.dedaulus.cinematty.framework.tools;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 03.04.11
 * Time: 17:01
 */
public class UniqueSortedList<T> extends AbstractList<T> {
    private ArrayList<T> mSource;
    private Comparator<T> mComparator;

    public UniqueSortedList(Comparator<T> comparator) {
        mComparator = comparator;
        mSource = new ArrayList<T>();
    }

    public UniqueSortedList(int capacity, Comparator<T> comparator) {
        mComparator = comparator;
        mSource = new ArrayList<T>(capacity);
    }

    public UniqueSortedList(Collection<T> collection, Comparator<T> comparator) {
        mComparator = comparator;
        mSource = new ArrayList<T>(new HashSet<T>(collection));
        Collections.sort(mSource, mComparator);
    }

    @Override
    public T get(int index) {
        return mSource.get(index);
    }

    @Override
    public T remove(int index) {
        return mSource.remove(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object o) {
        if (o == null) return -1;
        int i = Collections.binarySearch(mSource, (T)o, mComparator);
        return i >= 0 ? i : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public void clear() {
        mSource.clear();
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public boolean add(T t) {
        if (t != null) {
            int i = Collections.binarySearch(mSource, t, mComparator);
            if (i < 0) {
                mSource.add(-i - 1, t);
                return true;
            }
        }

        return false;
    }
}
