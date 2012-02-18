package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;

import java.util.Collection;
import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 21.08.11
 * Time: 2:18
 */
public class MovieComparator implements Comparator<Movie> {
    private MovieSortOrder sortOrder;
    private int currentDay;

    public MovieComparator(MovieSortOrder sortOrder, int day) {
        this.sortOrder = sortOrder;
        currentDay = day;
    }

    public int compare(Movie o1, Movie o2) {
        switch (sortOrder) {
        case BY_CAPTION:
            return o1.getName().compareTo(o2.getName());

        case BY_POPULAR:
            Collection<Cinema> l1 = o1.getCinemas(currentDay).values();
            Collection<Cinema> l2 = o2.getCinemas(currentDay).values();
            int size1 = l1 != null ? l1.size() : 0;
            int size2 = l2 != null ? l2.size() : 0;
            if (size1 == size2) return 0;
            return (size1 < size2) ? 1 : -1;

        case BY_RATING:
            float imdb1 = o1.getImdb();
            float imdb2 = o2.getImdb();
            if (imdb1 == imdb2) return 0;
            return imdb1 < imdb2 ? 1 : -1;

        default:
            throw new RuntimeException("Sort order not implemented!");
        }
    }
}
