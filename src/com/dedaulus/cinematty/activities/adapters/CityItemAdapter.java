package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:44
 */
public class CityItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<City> mCities;
    private int mCurrentCityId;

    public CityItemAdapter(Context context, List<City> cities, int currentCityId) {
        mContext = context;
        mCities = cities;
        mCurrentCityId = currentCityId;
    }

    public int getCount() {
        return mCities.size();
    }

    public Object getItem(int i) {
        return mCities.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.city_item, parent, false);
    }

    private void bindView(int position, View view) {
        City city = mCities.get(position);

        TextView text = (TextView)view.findViewById(R.id.city_caption_in_city_list);
        text.setText(city.getName());

        if (city.getId() == mCurrentCityId) {
            view.findViewById(R.id.city_checked_icon).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.city_checked_icon).setVisibility(View.GONE);
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
}
