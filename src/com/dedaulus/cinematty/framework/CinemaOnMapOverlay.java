package com.dedaulus.cinematty.framework;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 04.03.12
 * Time: 3:14
 */
public class CinemaOnMapOverlay extends BalloonItemizedOverlay {
    private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private Context context;

    public CinemaOnMapOverlay(Drawable drawable, MapView mapView) {
        super(boundCenterBottom(drawable), mapView);
        context = mapView.getContext();
    }

    public void addOverlay(OverlayItem overlay) {
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
        return super.onBalloonTap(index, overlayItem);
    }
}
