package com.dedaulus.cinematty.framework;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ListView;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.activities.MovieWithScheduleListActivity;
import com.dedaulus.cinematty.activities.adapters.CinemaItemAdapter;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.03.12
 * Time: 3:14
 */
public class CinemaOnMapOverlay extends BalloonItemizedOverlay {
    public static class CinemaOverlayItem extends OverlayItem {
        private Cinema cinema;

        public CinemaOverlayItem(Cinema cinema) {
            super(toGeoPoint(cinema.getCoordinate()), cinema.getName(), cinema.getInto());
            this.cinema = cinema;
        }

        public Cinema getCinema() {
            return cinema;
        }

        public static GeoPoint toGeoPoint(Coordinate coordinate) {
            return new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6));
        }
    }

    private ArrayList<CinemaOverlayItem> overlays;
    private Context context;
    private ActivitiesState activitiesState;

    {
        overlays = new ArrayList<CinemaOverlayItem>();
    }

    public CinemaOnMapOverlay(Drawable drawable, MapView mapView, ActivitiesState activitiesState) {
        super(boundCenterBottom(drawable), mapView);
        context = mapView.getContext();
        this.activitiesState = activitiesState;
    }

    public void addOverlay(CinemaOverlayItem overlay) {
        overlays.add(overlay);
        populate();
    }

    @Override
    public int size() {
        return overlays.size();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return overlays.get(i);
    }

    @Override
    protected boolean onBalloonTap(int index, OverlayItem overlayItem) {
        if (overlayItem instanceof CinemaOverlayItem) {
            Cinema cinema = ((CinemaOverlayItem)overlayItem).getCinema();

            String cookie = UUID.randomUUID().toString();

            ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_CINEMA, cinema, null, null, null);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(context, MovieWithScheduleListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_DESCRIPTION_PAGE_ID);
            context.startActivity(intent);

            return true;
        }

        return super.onBalloonTap(index, overlayItem);
    }
}
