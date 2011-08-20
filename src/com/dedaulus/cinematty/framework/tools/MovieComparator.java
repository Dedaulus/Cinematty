package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Movie;

import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 21.08.11
 * Time: 2:18
 */
public class MovieComparator implements Comparator<Movie> {
    private MovieSortOrder mSortOrder;

    public MovieComparator(MovieSortOrder sortOrder) {
        mSortOrder = sortOrder;
    }

    public int compare(Movie o1, Movie o2) {
        switch (mSortOrder) {
        case BY_CAPTION:
            return o1.getCaption().compareTo(o2.getCaption());

        case BY_POPULAR:
            int size1 = o1.getCinemas().size();
            int size2 = o2.getCinemas().size();
            if (size1 == size2) return 0;
            return (size1 < size2) ? 1 : -1;

        default:
            throw new RuntimeException("Sort order not implemented!");
        }
    }
}
