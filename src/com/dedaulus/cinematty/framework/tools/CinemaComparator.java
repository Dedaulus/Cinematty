package com.dedaulus.cinematty.framework.tools;

import android.location.Location;
import com.dedaulus.cinematty.framework.Cinema;

import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 24.04.11
 * Time: 0:58
 */
public class CinemaComparator implements Comparator<Cinema> {
    private CinemaSortOrder mSortOrder;
    private Location mLocation;
    private float[] mDistance1;
    private float[] mDistance2;

    public CinemaComparator(CinemaSortOrder sortOrder, Object data) {
        mSortOrder = sortOrder;
        switch (mSortOrder) {
        case BY_DISTANCE:
            mLocation = (Location)data;
            mDistance1 = new float[1];
            mDistance2 = new float[1];
            break;
        }
    }

    public int compare(Cinema o1, Cinema o2) {
        switch (mSortOrder) {
        case BY_CAPTION:
            return o1.getCaption().compareTo(o2.getCaption());

        case BY_FAVOURITE:
            if (o1.getFavourite() == o2.getFavourite()) return 0;
            return (o1.getFavourite() < o2.getFavourite()) ? 1 : -1;

        case BY_DISTANCE:
            if (mLocation != null && o1.getCoordinate() != null && o2.getCoordinate() != null) {
                Location.distanceBetween(
                        mLocation.getLatitude(), mLocation.getLongitude(),
                        o1.getCoordinate().latitude, o1.getCoordinate().longitude,
                        mDistance1);
                Location.distanceBetween(
                        mLocation.getLatitude(), mLocation.getLongitude(),
                        o2.getCoordinate().latitude, o2.getCoordinate().longitude,
                        mDistance2);

                if (mDistance1[0] == mDistance2[0]) return 0;
                return (mDistance1[0] < mDistance2[0]) ? -1 : 1;

            } else if (mLocation != null) {
                if (o1.getCoordinate() != null) return 1;
                else if (o2.getCoordinate() != null) return -1;
            }

            return o1.getCaption().compareTo(o2.getCaption());

        default:
            throw new RuntimeException("Sort order not implemented!");
        }
    }
}
