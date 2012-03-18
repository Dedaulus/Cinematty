package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.CityItemAdapter;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.tools.CityHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 13:49
 */
public class CityListActivity extends Activity {
    private CinemattyApplication app;
    private List<City> cities;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_list);

        app = (CinemattyApplication)getApplication();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CityHandler handler = new CityHandler();
            parser.parse(getResources().openRawResource(R.raw.cities), handler);
            cities = handler.getCityList();
            ListView list = (ListView)findViewById(R.id.city_list);
            list.setAdapter(new CityItemAdapter(this, new ArrayList<City>(cities)));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onCityItemClick(adapterView, view, i, l);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*
    @Override
    protected void onResume() {
        City city = app.getCurrentCity();
        int id = cities.indexOf(new City(null, city != null ? city.getId() : 0, null));
        ListView list = (ListView)findViewById(R.id.city_list);
        list.smoothScrollToPosition(id);

        super.onResume();
    }
    */
    private void onCityItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CityItemAdapter adapter = (CityItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        City city = (City)adapter.getItem(i - list.getHeaderViewsCount());

        app.saveCurrentCity(city);

        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }
}