package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 18.06.11
 * Time: 22:35
 *
 * Ported from python code from http://gis-lab.info/qa/great-circles.html
 */
public class DistanceCalc {
    private static int radius = 6372795;

    public static int getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = latitude1 * Math.PI / 180;
        double lat2 = latitude2 * Math.PI / 180;
        double long1 = longitude1 * Math.PI / 180;
        double long2 = longitude2 * Math.PI / 180;

        double cosl1 = Math.cos(lat1);
        double cosl2 = Math.cos(lat2);
        double sinl1 = Math.sin(lat1);
        double sinl2 = Math.sin(lat2);

        double delta = long2 - long1;
        double cosdelta = Math.cos(delta);
        double sindelta = Math.sin(delta);

        double y = Math.sqrt(Math.pow(cosl2 * sindelta, 2) + Math.pow(cosl1 * sinl2 - sinl1 * cosl2 * cosdelta, 2));
        double x = sinl1 * sinl2 + cosl1 * cosl2 * cosdelta;
        double ad = Math.atan2(y, x);
        double dist = ad * radius;

        return (int)dist;
    }
}
