package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.os.Bundle;
import com.dedaulus.cinematty.R;
import com.google.android.maps.MapActivity;

/**
 * User: Dedaulus
 * Date: 03.03.12
 * Time: 22:18
 */
public class CinemaMapView extends MapActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_on_map);


    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}