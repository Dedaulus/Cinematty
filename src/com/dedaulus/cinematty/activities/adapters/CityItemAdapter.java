package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:44
 */
public class CityItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<City> cities;

    public CityItemAdapter(Context context, ArrayList<City> cities) {
        this.context = context;
        this.cities = cities;
    }

    public int getCount() {
        return cities.size();
    }

    public Object getItem(int i) {
        return cities.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.city_item, parent, false);
    }

    private void bindView(int position, View view) {
        City city = cities.get(position);
        TextView text = (TextView)view.findViewById(R.id.city_caption_in_city_list);
        text.setText(city.getName());
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
}
