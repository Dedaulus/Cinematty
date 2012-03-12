package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.location.Location;
import android.text.SpannableString;
import android.util.Pair;
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

import java.util.*;

/**
 * User: Dedaulus
 * Date: 31.03.11
 * Time: 23:02
 */
public class CinemaItemWithScheduleAdapter extends BaseAdapter implements SortableAdapter<Cinema>, LocationAdapter {
    private Context context;
    private Map<String, Cinema> cinemaEntries;
    private ArrayList<Cinema> cinemas;
    private Movie currentMovie;
    private int currentDay;
    private Location currentLocation;

    public CinemaItemWithScheduleAdapter(Context context, Map<String, Cinema> cinemaEntries, Movie currentMovie, int day, Location location) {
        this.context = context;
        this.cinemaEntries = cinemaEntries;
        cinemas = new ArrayList<Cinema>(cinemaEntries.values());
        this.currentMovie = currentMovie;
        currentDay = day;
        currentLocation = location;
    }

    public int getCount() {
        return cinemas.size();
    }

    public Object getItem(int i) {
        return cinemas.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.cinema_item_w_schedule, parent, false);
    }

    private void bindView(int position, View view) {
        Cinema cinema = cinemas.get(position);

        ImageView image = (ImageView)view.findViewById(R.id.fav_icon_in_cinema_list);
        if (cinema.getFavourite() > 0) {
            image.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            image.setImageResource(R.drawable.ic_list_fav_off);
        }

        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onCinemaFavIconClick(view);
            }
        });

        TextView text = (TextView)view.findViewById(R.id.cinema_caption_in_cinema_list);
        text.setText(cinema.getName());

        View addressPanel = view.findViewById(R.id.cinema_address_panel);
        String address = cinema.getAddress();
        if (address != null) {
            text = (TextView)addressPanel.findViewById(R.id.cinema_address_in_cinema_list);
            text.setText(address);

            text = (TextView)addressPanel.findViewById(R.id.distance);
            Coordinate coordinate = cinema.getCoordinate();
            if (coordinate != null && currentLocation != null) {
                float[] distance = new float[1];
                Location.distanceBetween(coordinate.latitude, coordinate.longitude, currentLocation.getLatitude(), currentLocation.getLongitude(), distance);
                int m = (int)distance[0];
                text.setText(DataConverter.metersToDistance(context, m));
            }

            addressPanel.setVisibility(View.VISIBLE);
        } else {
            addressPanel.setVisibility(View.GONE);
        }

        TextView scheduleView = (TextView)view.findViewById(R.id.movie_schedule_in_cinema_list);
        TextView timeLeftView = (TextView)view.findViewById(R.id.time_left_in_cinema_list);

        Map<String, Pair<Movie, List<Calendar>>> showTimesEntries = cinema.getShowTimes(currentDay);
        if (!showTimesEntries.isEmpty()) {
            List<Calendar> showTimes = showTimesEntries.get(currentMovie.getName()).second;
            String showTimesStr = DataConverter.showTimesToString(showTimes);
            if (showTimesStr.length() != 0) {
                scheduleView.setText(showTimesStr);
                scheduleView.setVisibility(View.VISIBLE);
            } else {
                scheduleView.setVisibility(View.GONE);
            }
            if (currentDay == Constants.TODAY_SCHEDULE) {
                SpannableString timeLeftString = DataConverter.showTimesToClosestTimeString(context, showTimes);
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
            myView = newView(context, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    public void sortBy(Comparator<Cinema> cinemaComparator) {
        Collections.sort(cinemas, cinemaComparator);
        notifyDataSetChanged();
    }

    public void setCurrentLocation(Location location) {
        currentLocation = location;
        notifyDataSetChanged();
    }

    private void onCinemaFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.cinema_caption_in_cinema_list);
        Cinema cinema = cinemaEntries.get(caption.getText().toString());
        if (cinema.getFavourite() > 0) {
            cinema.setFavourite(false);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_off);
        } else {
            cinema.setFavourite(true);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_on);
        }
    }
}
