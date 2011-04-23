package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 31.03.11
 * Time: 23:02
 */
public class CinemaItemWithScheduleAdapter extends BaseAdapter implements SortableAdapter<Cinema> {
    private Context mContext;
    private List<Cinema> mCinemas;
    private Movie mCurrentMovie;

    public CinemaItemWithScheduleAdapter(Context context, List<Cinema> cinemas, Movie currentMovie) {
        mContext = context;
        mCinemas = cinemas;
        mCurrentMovie = currentMovie;
    }

    public int getCount() {
        return mCinemas.size();
    }

    public Object getItem(int i) {
        return i >= 0 && i < mCinemas.size() ? mCinemas.get(i) : null;
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

        ImageView image = (ImageView)view.findViewById(R.id.fav_cinema_in_schedule_list);
        if (cinema.getFavourite() > 0) {
            image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            image.setImageResource(android.R.drawable.btn_star_big_off);
        }

        TextView text = (TextView)view.findViewById(R.id.cinema_caption_in_schedule_list);
        text.setText(cinema.getCaption());

        text = (TextView)view.findViewById(R.id.schedule_enum_in_schedule_list);
        List<String> showTime = cinema.getShowTimes().get(mCurrentMovie);
        if (showTime != null) {
            StringBuilder times = new StringBuilder();
                for (String str : showTime) {
                    times.append(str + ", ");
                }
                times.delete(times.length() - 2, times.length() - 1);
                text.setText(times.toString());
        }
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = null;

        if (view != null) {
            myView = view;
        }
        else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    public void sortBy(Comparator<Cinema> cinemaComparator) {
        Collections.sort(mCinemas, cinemaComparator);
        notifyDataSetChanged();
    }
}
