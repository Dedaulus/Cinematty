package com.dedaulus.cinematty.framework.tools;

import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 03.04.11
 * Time: 19:51
 */
public class DefaultComparator<T extends Comparable<T>> implements Comparator<T> {
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }
}
