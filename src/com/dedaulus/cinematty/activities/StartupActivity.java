package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.tools.CityHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class StartupActivity extends Activity
{
    private static final int GET_CURRENT_CITY = RESULT_FIRST_USER + 1;
    private static final int GET_CITIES_TIMEOUT = 10000;
    private static final int GET_SCHEDULE_TIMEOUT = 15000;

    private CinemattyApplication mApp;
    private String mErrorString;

    private AsyncTask<String, Void, Void> mGetCitiesTask;
    private AsyncTask<Void, Void, Void> mGetScheduleTask;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mApp = (CinemattyApplication)getApplication();

        try {
            FileInputStream is = openFileInput(getString(R.string.cities_file));

            findViewById(R.id.loading_cities).setVisibility(View.GONE);
            findViewById(R.id.loading_schedules).setVisibility(View.VISIBLE);

            City city = getCurrentCity();
            TextView textView = (TextView)findViewById(R.id.current_city);
            textView.setText(city.getName());

            mApp.setCurrentCity(city);
            getSchedule(true);

        } catch (FileNotFoundException e) {
            findViewById(R.id.loading_cities).setVisibility(View.VISIBLE);
            findViewById(R.id.loading_schedules).setVisibility(View.GONE);

            getCitiesList(true);
        }
    }

    public void setErrorString(String errorString) {
        mErrorString = errorString;

        TextView textView = (TextView)findViewById(R.id.error_string);
        textView.setText(mErrorString);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_CURRENT_CITY) {
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0, 0);
            startActivity(intent);
        }
    }

    private City getCurrentCity() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CityHandler handler = new CityHandler();
            try {
                parser.parse(openFileInput(getString(R.string.cities_file)), handler);

                int id = mApp.getCurrentCityId();
                List<City> cities = handler.getCityList();
                for (City city : cities) {
                    if (city.getId() == id) return city;
                }

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException();
    }

    private void getSchedule(boolean download) {
        if (download) {
            (mGetScheduleTask = new AsyncTask<Void, Void, Void>() {
                private String error;
                private boolean success = false;

                @Override
                protected Void doInBackground(Void... nothing) {
                    try {
                        mApp.retrieveData();
                        success = true;
                    } catch (IOException e) {
                        error = e.toString();
                    } catch (ParserConfigurationException e) {
                        error = e.toString();
                    } catch (SAXException e) {
                        error = e.toString();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void nothing) {
                    if (success) getSchedule(false);
                    else setErrorString(error);
                }
            }).execute();
        } else {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);

            finish();
        }
    }

    private void getCitiesList(boolean download) {
        if (download) {
            (mGetCitiesTask = new AsyncTask<String, Void, Void>() {
                private String error;
                private boolean success = false;

                @Override
                protected Void doInBackground(String... url) {
                    try {
                        URL citiesUrl = new URL(url[0]);
                        URLConnection connection = citiesUrl.openConnection();
                        connection.setConnectTimeout(GET_CITIES_TIMEOUT);

                        InputStream is = connection.getInputStream();
                        dumpStream(is);

                        success = true;
                    } catch (MalformedURLException e) {
                        error = e.toString();
                    } catch (IOException e) {
                        error = e.toString();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void nothing) {
                    if (success) getCitiesList(false);
                    else setErrorString(error);
                }
            }).execute(getString(R.string.settings_url) + "/" + getString(R.string.cities_file));
        } else {
            Intent intent = new Intent(this, CityListActivity.class);
            startActivityForResult(intent, GET_CURRENT_CITY);

            finish();
        }
    }

    private void dumpStream(InputStream is) throws IOException {
        InputStream input = new BufferedInputStream(is);
        FileOutputStream output = openFileOutput(getString(R.string.cities_file), MODE_PRIVATE);

        byte data[] = new byte[1024];

        int count = 0;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();
    }
}
