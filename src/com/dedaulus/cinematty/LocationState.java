package com.dedaulus.cinematty;

import android.location.Location;
import com.dedaulus.cinematty.framework.tools.LocationClient;

/**
 * User: Dedaulus
 * Date: 17.12.11
 * Time: 10:46
 */
public interface LocationState {
    void addLocationClient(LocationClient client);

    void removeLocationClient(LocationClient client);

    Location getCurrentLocation();

    void updateCurrentLocation(Location location);

    void startLocationListening();

    void stopLocationListening();
}
