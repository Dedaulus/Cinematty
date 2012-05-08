package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.dedaulus.cinematty.framework.tools.DataConverter;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 22:11
 */
public class CinemaItemAdapter extends BaseAdapter implements SortableAdapter<Cinema>, LocationAdapter {
    private static class CinemaViewHolder {
        View favIconRegion;
        ImageView favIcon;
        TextView caption;
        RelativeLayout addressRegion;
        TextView address;
        TextView distance;
    }

    private Context context;
    private LayoutInflater inflater;
    IdleDataSetChangeNotifier notifier;
    private ArrayList<Cinema> cinemas;
    private Location location;
    private final Object locationMutex = new Object();

    public CinemaItemAdapter(Context context, IdleDataSetChangeNotifier notifier, ArrayList<Cinema> cinemas, Location location) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.notifier = notifier;
        notifier.setAdapter(this);
        this.cinemas = cinemas;
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
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CinemaViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cinema_item, null);
            viewHolder = new CinemaViewHolder();
            viewHolder.favIconRegion = convertView.findViewById(R.id.fav_icon_region);
            viewHolder.favIcon = (ImageView)viewHolder.favIconRegion.findViewById(R.id.fav_icon);
            viewHolder.caption = (TextView)convertView.findViewById(R.id.cinema_caption);
            viewHolder.addressRegion = (RelativeLayout)convertView.findViewById(R.id.cinema_address_region);
            viewHolder.address = (TextView)viewHolder.addressRegion.findViewById(R.id.cinema_address);
            viewHolder.distance = (TextView)viewHolder.addressRegion.findViewById(R.id.cinema_distance);
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
            Cinema prev = cinemas.get(0);
            for (Cinema next : cinemas) {
                if (prev == null) continue;
                if (cinemaComparator.compare(prev, next) > 0) return false;
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
