package com.dedaulus.cinematty.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.CinemaOnMapOverlay;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.google.android.maps.*;

/**
 * User: Dedaulus
 * Date: 03.03.12
 * Time: 22:18
 */
public class CinemaMapView extends MapActivity {
    private static final float BALLOON_OFFSET = 0.60f;
    private static final int DEFAULT_MAP_ZOOM = 17;
    
    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private ActivityState state;
    private String stateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_on_map);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        activitiesState = app.getActivitiesState();

        stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");

        MapView mapView = (MapView)findViewById(R.id.map_view);
        mapView.setBuiltInZoomControls(true);
        CinemaOnMapOverlay cinemaOnMapOverlay = new CinemaOnMapOverlay(getResources().getDrawable(R.drawable.ic_map_marker), mapView);
        Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_marker);
        cinemaOnMapOverlay.setBalloonBottomOffset((int)(markerBitmap.getHeight() * BALLOON_OFFSET));

        Coordinate coordinate = state.cinema.getCoordinate();
        GeoPoint point = new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6));
        OverlayItem overlayitem = new OverlayItem(point, state.cinema.getName(), state.cinema.getInto());
        cinemaOnMapOverlay.addOverlay(overlayitem);
        mapView.getOverlays().add(cinemaOnMapOverlay);
        
        MapController mapController = mapView.getController();
        mapController.setCenter(point);
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