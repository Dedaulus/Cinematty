package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;

import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 21.08.11
 * Time: 2:18
 */
public class MovieComparator implements Comparator<Movie> {
    private MovieSortOrder mSortOrder;
    private int mCurrentDay;

    public MovieComparator(MovieSortOrder sortOrder, int day) {
        mSortOrder = sortOrder;
        mCurrentDay = day;
    }

    public int compare(Movie o1, Movie o2) {
        switch (mSortOrder) {
        case BY_CAPTION:
            return o1.getCaption().compareTo(o2.getCaption());

        case BY_POPULAR:
            List<Cinema> l1 = o1.getCinemas(mCurrentDay);
            List<Cinema> l2 = o2.getCinemas(mCurrentDay);
            int size1 = l1 != null ? l1.size() : 0;
            int size2 = l2 != null ? l2.size() : 0;
            if (size1 == size2) return 0;
            return (size1 < size2) ? 1 : -1;
        default:
            throw new RuntimeException("Sort order not implemented!");
        }
    }
}
