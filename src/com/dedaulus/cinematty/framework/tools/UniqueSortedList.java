package com.dedaulus.cinematty.framework.tools;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 03.04.11
 * Time: 17:01
 */
public class UniqueSortedList<T> extends AbstractList<T> {
    private ArrayList<T> source;
    private Comparator<T> comparator;

    public UniqueSortedList(Comparator<T> comparator) {
        this.comparator = comparator;
        source = new ArrayList<T>();
    }

    public UniqueSortedList(int capacity, Comparator<T> comparator) {
        this.comparator = comparator;
        source = new ArrayList<T>(capacity);
    }

    public UniqueSortedList(Collection<T> collection, Comparator<T> comparator) {
        this.comparator = comparator;
        source = new ArrayList<T>(new HashSet<T>(collection));
        Collections.sort(source, this.comparator);
    }

    @Override
    public T get(int index) {
        return source.get(index);
    }

    @Override
    public T remove(int index) {
        return source.remove(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object o) {
        if (o == null) return -1;
        int i = Collections.binarySearch(source, (T)o, comparator);
        return i >= 0 ? i : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public void clear() {
        source.clear();
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public boolean add(T t) {
        if (t != null) {
            int i = Collections.binarySearch(source, t, comparator);
            if (i < 0) {
                source.add(-i - 1, t);
                return true;
            }
        }

        return false;
    }
}
