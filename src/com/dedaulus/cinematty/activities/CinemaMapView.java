package com.dedaulus.cinematty.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.CinemaOnMapOverlay;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Dedaulus
 * Date: 03.03.12
 * Time: 22:18
 */
public class CinemaMapView extends MapActivity {
    private static final float BALLOON_OFFSET = 0.60f;
    private static final int DEFAULT_MAP_ZOOM = 17;
    
    private ActivitiesState activitiesState;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_on_map);

        CinemattyApplication app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        ActivityState state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

        MapView mapView = (MapView)findViewById(R.id.map_view);
        mapView.setBuiltInZoomControls(true);
        // CRUNCH!
        String cityName = app.getCurrentCity().getName();
        if (cityName.equalsIgnoreCase("мурманск") ||
                cityName.equalsIgnoreCase("тюмень") ||
                cityName.equalsIgnoreCase("иркуткс")) {
            mapView.setSatellite(true);
        } else {
            mapView.setSatellite(false);
        }

        // adding other cinemas
        CinemaOnMapOverlay otherCinemasOnMapOverlay = new CinemaOnMapOverlay(getResources().getDrawable(R.drawable.ic_map_marker_other), mapView, activitiesState);
        Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_marker_other);
        otherCinemasOnMapOverlay.setBalloonBottomOffset((int)(markerBitmap.getHeight() * BALLOON_OFFSET));
        for (Cinema cinema : app.getSettings().getCinemas().values()) {
            if (!cinema.equals(state.cinema) && cinema.getAddress() != null) {
                otherCinemasOnMapOverlay.addOverlay(new CinemaOnMapOverlay.CinemaOverlayItem(cinema));
            }
        }
        mapView.getOverlays().add(otherCinemasOnMapOverlay);

        // adding current cinema
        CinemaOnMapOverlay cinemaOnMapOverlay = new CinemaOnMapOverlay(getResources().getDrawable(R.drawable.ic_map_marker), mapView, activitiesState);
        markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_marker);
        cinemaOnMapOverlay.setBalloonBottomOffset((int)(markerBitmap.getHeight() * BALLOON_OFFSET));
        cinemaOnMapOverlay.addOverlay(new CinemaOnMapOverlay.CinemaOverlayItem(state.cinema));
        mapView.getOverlays().add(cinemaOnMapOverlay);

        MapController mapController = mapView.getController();
        mapController.setCenter(CinemaOnMapOverlay.CinemaOverlayItem.toGeoPoint(state.cinema.getCoordinate()));
        mapController.setZoom(DEFAULT_MAP_ZOOM);
    }

    @Override
    public void onBackPressed() {
        activitiesState.removeState(stateId);
        super.onBackPressed();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
