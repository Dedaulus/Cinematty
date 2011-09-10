package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class StartupActivity extends Activity
{
    private static final int GET_CURRENT_CITY = RESULT_FIRST_USER + 1;
    private static final int GET_CITIES_TIMEOUT = 5000;
    private static final int GET_SCHEDULE_TIMEOUT = 10000;

    private CinemattyApplication mApp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        mApp = (CinemattyApplication)getApplication();
        mApp.startListenLocation();

        City city = getCurrentCity();
        if (city != null) {
            //TextView textView = (TextView)findViewById(R.id.current_city);
            //textView.setText(city.getName());
            mApp.setCurrentCity(city);
            getSchedule();
        } else {
            getCitiesList();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // This is need due to frozen internet connection
            mApp.stopListenLocation();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onTryAgainClick(View view) {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void setErrorString(String errorString, String errorMessage) {
        TextView textView = (TextView)findViewById(R.id.error_string);
        textView.setText(errorString);

        textView = (TextView)findViewById(R.id.error_message);
        textView.setText(errorMessage);

        findViewById(R.id.loading_schedule_panel).setVisibility(View.INVISIBLE);
        findViewById(R.id.loading_error_panel).setVisibility(View.VISIBLE);
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
            parser.parse(getResources().openRawResource(R.raw.cities), handler);
            int id = mApp.getCurrentCityId();
            List<City> cities = handler.getCityList();
            for (City city : cities) {
                if (city.getId() == id) return city;
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getSchedule() {
        final Activity activity = this;
        new Thread(new Runnable() {
            private String error;
            private String message = getString(R.string.unknown_error);
            private boolean success = false;
            public void run() {
                try {
                    mApp.retrieveData();
                    success = true;
                } catch (UnknownHostException e) {
                    error = e.toString();
                    message = getString(R.string.connect_error);
                } catch (SocketException e) {
                    error = e.toString();
                    message = getString(R.string.connect_error);
                } catch (IOException e) {
                    error = e.toString();
                } catch (ParserConfigurationException e) {
                    error = e.toString();
                } catch (SAXException e) {
                    error = e.toString();
                }

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (success) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            setErrorString(error, message);
                        }
                    }
                });
            }
        }).start();
    }
    /*
    private void getSchedule(boolean download) {
        if (download) {
            new AsyncTask<Void, Void, Void>() {
                private String error;
                private String message = getString(R.string.unknown_error);
                private boolean success = false;

                @Override
                protected Void doInBackground(Void... nothing) {
                    try {
                        mApp.retrieveData();
                        success = true;
                    } catch (UnknownHostException e) {
                        error = e.toString();
                        message = getString(R.string.connect_error);
                    } catch (SocketException e) {
                        error = e.toString();
                        message = getString(R.string.connect_error);
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
                    else setErrorString(error, message);
                }
            }.execute();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();
        }
    }
    */
    private void getCitiesList() {
        Intent intent = new Intent(this, CityListActivity.class);
        startActivityForResult(intent, GET_CURRENT_CITY);
        finish();
    }
}
