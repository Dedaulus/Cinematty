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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 22:11
 */
public class CinemaItemAdapter extends BaseAdapter implements SortableAdapter<Cinema> {
    private Context mContext;
    private List<Cinema> mCinemas;

    public CinemaItemAdapter(Context context, List<Cinema> cinemas) {
        mContext = context;
        mCinemas = cinemas;
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
        return layoutInflater.inflate(R.layout.cinema_item, parent, false);
    }

    private void bindView(int position, View view) {
        Cinema cinema = mCinemas.get(position);

        ImageView image = (ImageView)view.findViewById(R.id.fav_cinema_in_cinema_list);
        if (cinema.getFavourite() > 0) {
            image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            image.setImageResource(android.R.drawable.btn_star_big_off);
        }

        TextView text = (TextView)view.findViewById(R.id.cinema_item_in_list);
        text.setText(cinema.getCaption());

        text = (TextView)view.findViewById(R.id.advanced_data_in_cinema_list);
        String address = cinema.getAddress();
        if (address != null) {
            text.setText(address);
            text.setVisibility(View.VISIBLE);
        }
        else {
            text.setVisibility(View.GONE);
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
