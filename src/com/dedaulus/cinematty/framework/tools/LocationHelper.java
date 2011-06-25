package com.dedaulus.cinematty.framework.tools;

import android.location.Location;

/**
 * User: Dedaulus
 * Date: 19.06.11
 * Time: 18:17
 */
public class LocationHelper {
    public static final int TIME_LISTEN_TIMEOUT = 30000;
    public static final int LISTEN_DISTANCE = 10;
    public static final int MAX_DISTANCE = 50000;
    public static final int FINE_DISTANCE = 20;

    private static final int FINE_LOCATION_TIME_ADVANTAGE = 300000;

    public static Location selectBetterLocation(Location location1, Location location2) {
        long time1 = location1.getTime();
        long time2 = location2.getTime();

        if (isFineLocation(location1) && !isFineLocation(location2)) {
            time1 += FINE_LOCATION_TIME_ADVANTAGE;
        } else if (!isFineLocation(location1) && isFineLocation(location2)) {
            time2 += FINE_LOCATION_TIME_ADVANTAGE;
        }

        if (time1 > time2) return location1;
        else return location2;
    }

    public static boolean isFineLocation(Location location) {
        return location.getProvider().equalsIgnoreCase("gps");
    }
}
