package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.CityItemAdapter;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.tools.CityHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 13:49
 */
public class CityListActivity extends Activity {
    private CinemattyApplication mApp;
    private List<City> mCities;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_list);

        mApp = (CinemattyApplication)getApplication();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CityHandler handler = new CityHandler();
            try {
                parser.parse(openFileInput(getString(R.string.cities_file)), handler);

                mCities = handler.getCityList();

                ListView list = (ListView)findViewById(R.id.city_list);

                list.setAdapter(new CityItemAdapter(this, mCities, mApp.getCurrentCityId()));

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        onCityItemClick(adapterView, view, i, l);
                    }
                });
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void onCityItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.city_caption_in_city_list);
        String caption = textView.getText().toString();
        for (City city : mCities) {
            if (city.getName().equals(caption)) {
                mApp.saveCurrentCityId(city.getId());

                Intent intent = new Intent(this, StartupActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}