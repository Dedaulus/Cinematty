package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 31.03.11
 * Time: 23:02
 */
public class CinemaItemWithScheduleAdapter extends BaseAdapter implements SortableAdapter<Cinema>, LocationAdapter {
    private Context mContext;
    private List<Cinema> mCinemas;
    private Movie mCurrentMovie;
    private Location mCurrentLocation;

    public CinemaItemWithScheduleAdapter(Context context, List<Cinema> cinemas, Movie currentMovie, Location location) {
        mContext = context;
        mCinemas = cinemas;
        mCurrentMovie = currentMovie;
        mCurrentLocation = location;
    }

    public int getCount() {
        return mCinemas.size();
    }

    public Object getItem(int i) {
        return mCinemas.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.cinema_item_with_schedule, parent, false);
    }

    private void bindView(int position, View view) {
        Cinema cinema = mCinemas.get(position);

        ImageView image = (ImageView)view.findViewById(R.id.fav_icon_in_schedule_list);
        if (cinema.getFavourite() > 0) {
            image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            image.setImageResource(android.R.drawable.btn_star_big_off);
        }

        TextView text = (TextView)view.findViewById(R.id.cinema_caption_in_schedule_list);
        text.setText(cinema.getCaption());

        text = (TextView)view.findViewById(R.id.distance);
        Coordinate coordinate = cinema.getCoordinate();
        if (coordinate != null && mCurrentLocation != null) {
            float[] distance = new float[1];
            Location.distanceBetween(coordinate.latitude, coordinate.longitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), distance);
            int m = (int)distance[0];
            text.setText(DataConverter.metersToDistance(mContext, m));
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }

        text = (TextView)view.findViewById(R.id.schedule_enum_in_schedule_list);
        List<Calendar> showTimes = cinema.getShowTimes().get(mCurrentMovie);
        if (showTimes != null) {
            text.setText(DataConverter.showTimesToSpannableString(showTimes));
        }
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = null;

        if (view != null) {
            myView = view;
        } else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    public void sortBy(Comparator<Cinema> cinemaComparator) {
        Collections.sort(mCinemas, cinemaComparator);
        notifyDataSetChanged();
    }

    public void setCurrentLocation(Location location) {
        mCurrentLocation = location;
        notifyDataSetChanged();
    }
}
