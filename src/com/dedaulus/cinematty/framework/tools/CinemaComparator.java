package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;

import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 24.04.11
 * Time: 0:58
 */
public class CinemaComparator implements Comparator<Cinema> {
    private CinemaSortOrder mSortOrder;

    public CinemaComparator(CinemaSortOrder sortOrder) {
        mSortOrder = sortOrder;
    }

    public int compare(Cinema o1, Cinema o2) {
        switch (mSortOrder) {
        case BY_CAPTION:
            return o1.getCaption().compareTo(o2.getCaption());

        case BY_FAVOURITE:
            if (o1.getFavourite() == o2.getFavourite()) {
                return 0;
            }

            return (o1.getFavourite() < o2.getFavourite()) ? 1 : -1;

        case BY_DISTANCE:
            throw new RuntimeException("Sort order not implemented!");

        default:
            throw new RuntimeException("Sort order not implemented!");
        }
    }
}
