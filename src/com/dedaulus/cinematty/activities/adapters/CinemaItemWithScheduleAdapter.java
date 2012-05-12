package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.location.Location;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.dedaulus.cinematty.framework.tools.DataConverter;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 31.03.11
 * Time: 23:02
 */
public class CinemaItemWithScheduleAdapter extends BaseAdapter implements SortableAdapter<Cinema>, LocationAdapter {
    private static class CinemaViewHolder {
        View favIconRegion;
        ImageView favIcon;
        TextView caption;
        RelativeLayout addressRegion;
        TextView address;
        TextView distance;
        TextView schedule;
        TextView timeLeft;
    }
    
    private Context context;
    private LayoutInflater inflater;
    private IdleDataSetChangeNotifier notifier;
    private ArrayList<Cinema> cinemas;
    private Movie movie;
    private int currentDay;
    private Pair<Calendar, Calendar> timeRange;
    private Location location;
    private final Object locationMutex = new Object();

    public CinemaItemWithScheduleAdapter(Context context, IdleDataSetChangeNotifier notifier, ArrayList<Cinema> cinemas, Movie movie, int day, Pair<Calendar, Calendar> timeRange, Location location) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.notifier = notifier;
        notifier.setAdapter(this);
        this.cinemas = cinemas;
        this.movie = movie;
        currentDay = day;
        this.timeRange = timeRange;
        this.location = location;
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

    private void bindView(int position, CinemaViewHolder viewHolder) {
        final Cinema cinema = cinemas.get(position);
        viewHolder.caption.setText(cinema.getName());

        if (cinema.getFavourite() > 0) {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_off);
        }

        viewHolder.favIconRegion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ImageView imageView = (ImageView)view.findViewById(R.id.fav_icon);
                if (cinema.getFavourite() > 0) {
                    cinema.setFavourite(false);
                    imageView.setImageResource(R.drawable.ic_list_fav_off);
                } else {
                    cinema.setFavourite(true);
                    imageView.setImageResource(R.drawable.ic_list_fav_on);
                }
            }
        });

        String address = cinema.getAddress();
        if (address != null) {
            viewHolder.address.setText(address);
            Coordinate coordinate = cinema.getCoordinate();
            double latitude = 0.0;
            double longitude = 0.0;
            boolean locationValid = false;
            synchronized (locationMutex) {
                if (location != null) {
                    locationValid = true;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
            if (coordinate != null && locationValid) {
                float[] distance = new float[1];
                Location.distanceBetween(coordinate.latitude, coordinate.longitude, latitude, longitude, distance);
                int m = (int)distance[0];
                viewHolder.distance.setText(DataConverter.metersToDistance(context, m));
                viewHolder.distance.setVisibility(View.VISIBLE);
            } else {
                viewHolder.distance.setVisibility(View.GONE);
            }
            viewHolder.addressRegion.setVisibility(View.VISIBLE);
        } else {
            viewHolder.addressRegion.setVisibility(View.GONE);
        }

        List<Calendar> showTimes = cinema.getShowTimes(currentDay).get(movie.getName()).second;
        Calendar now = Calendar.getInstance();
        if (now.after(timeRange.first)) {
            timeRange.first.setTimeInMillis(now.getTimeInMillis());
        }
        String showTimesStr = DataConverter.showTimesToString(showTimes, timeRange);
        if (showTimesStr.length() != 0) {
            viewHolder.schedule.setText(showTimesStr);
            viewHolder.schedule.setVisibility(View.VISIBLE);
        } else {
            viewHolder.schedule.setVisibility(View.GONE);
        }

        if (currentDay == Constants.TODAY_SCHEDULE &&
                now.getTimeInMillis() == timeRange.first.getTimeInMillis() &&
                now.before(timeRange.second)) {
            String timeLeftStr = DataConverter.showTimesToClosestTimeString(context, showTimes);
            viewHolder.timeLeft.setText(timeLeftStr);
            viewHolder.timeLeft.setVisibility(View.VISIBLE);
        } else {
            viewHolder.timeLeft.setVisibility(View.GONE);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CinemaViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cinema_item_w_schedule, null);
            viewHolder = new CinemaViewHolder();
            viewHolder.favIconRegion = convertView.findViewById(R.id.fav_icon_region);
            viewHolder.favIcon = (ImageView)viewHolder.favIconRegion.findViewById(R.id.fav_icon);
            viewHolder.caption = (TextView)convertView.findViewById(R.id.cinema_caption);
            viewHolder.addressRegion = (RelativeLayout)convertView.findViewById(R.id.cinema_address_region);
            viewHolder.address = (TextView)viewHolder.addressRegion.findViewById(R.id.cinema_address);
            viewHolder.distance = (TextView)viewHolder.addressRegion.findViewById(R.id.cinema_distance);
            viewHolder.schedule = (TextView)convertView.findViewById(R.id.movie_schedule);
            viewHolder.timeLeft = (TextView)convertView.findViewById(R.id.time_left);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CinemaViewHolder)convertView.getTag();
        }
        bindView(position, viewHolder);
        return convertView;
    }

    public void sortBy(Comparator<Cinema> cinemaComparator) {
        Collections.sort(cinemas, cinemaComparator);
        notifyDataSetChanged();
    }

    @Override
    public boolean isSorted(Comparator<Cinema> cinemaComparator) {
        if (!cinemas.isEmpty()) {
            Cinema prev = null;
            for (Cinema next : cinemas) {
                if (prev != null) {
                    if (cinemaComparator.compare(prev, next) > 0) return false;
                }
                prev = next;
            }
        }
        return true;
    }

    public void setLocation(Location location) {
        synchronized (locationMutex) {
            this.location = location;
        }
        notifier.askForNotifyDataSetChanged();
    }
}
