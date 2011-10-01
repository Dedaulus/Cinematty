package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.location.Location;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.Constants;
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
    private int mCurrentDay;
    private Location mCurrentLocation;

    public CinemaItemWithScheduleAdapter(Context context, List<Cinema> cinemas, Movie currentMovie, int day, Location location) {
        mContext = context;
        mCinemas = cinemas;
        mCurrentMovie = currentMovie;
        mCurrentDay = day;
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

        ImageView image = (ImageView)view.findViewById(R.id.fav_icon_in_cinema_list);
        if (cinema.getFavourite() > 0) {
            image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            image.setImageResource(android.R.drawable.btn_star_big_off);
        }

        TextView text = (TextView)view.findViewById(R.id.cinema_caption_in_cinema_list);
        text.setText(cinema.getCaption());

        View addressPanel = view.findViewById(R.id.cinema_address_panel);
        String address = cinema.getAddress();
        if (address != null) {
            text = (TextView)addressPanel.findViewById(R.id.cinema_address_in_cinema_list);
            text.setText(address);

            text = (TextView)addressPanel.findViewById(R.id.distance);
            Coordinate coordinate = cinema.getCoordinate();
            if (coordinate != null && mCurrentLocation != null) {
                float[] distance = new float[1];
                Location.distanceBetween(coordinate.latitude, coordinate.longitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), distance);
                int m = (int)distance[0];
                text.setText(DataConverter.metersToDistance(mContext, m));
            }

            addressPanel.setVisibility(View.VISIBLE);
        } else {
            addressPanel.setVisibility(View.GONE);
        }

        TextView scheduleView = (TextView)view.findViewById(R.id.movie_schedule_in_cinema_list);
        TextView timeLeftView = (TextView)view.findViewById(R.id.time_left_in_cinema_list);

        boolean isShowTimeLeft = mCurrentDay == Constants.TODAY_SCHEDULE;

        List<Calendar> showTimes = cinema.getShowTimes(mCurrentDay).get(mCurrentMovie);
        if (showTimes != null) {
            String showTimesStr = DataConverter.showTimesToString(showTimes);
            if (showTimesStr.length() != 0) {
                scheduleView.setText(showTimesStr);
                scheduleView.setVisibility(View.VISIBLE);
            } else {
                scheduleView.setVisibility(View.GONE);
            }
            if (mCurrentDay == Constants.TODAY_SCHEDULE) {
                SpannableString timeLeftString = DataConverter.showTimesToClosestTimeString(mContext, showTimes);
                timeLeftView.setText(timeLeftString);
            } else {
                timeLeftView.setVisibility(View.GONE);
            }
        } else {
            scheduleView.setVisibility(View.GONE);
            timeLeftView.setVisibility(View.GONE);
        }
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView;
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
