package com.dedaulus.cinematty.framework.tools;

import android.location.Location;

/**
 * User: Dedaulus
 * Date: 18.06.11
 * Time: 23:31
 */
public interface LocationClient {
    void onLocationChanged(Location location);
}