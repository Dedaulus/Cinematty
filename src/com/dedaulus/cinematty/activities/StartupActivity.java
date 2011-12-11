package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.tools.Constants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class StartupActivity extends Activity
{
    private static final int GET_CURRENT_CITY = RESULT_FIRST_USER + 1;
    private CinemattyApplication mApp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        BugSenseHandler.setup(this, "97371d41");

        mApp = (CinemattyApplication)getApplication();
        mApp.setCurrentDay(Constants.TODAY_SCHEDULE);
        mApp.startListenLocation();

        City city = mApp.getCurrentCity();
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
            mApp.stopListenLocation();
            // This is need due to frozen internet connection
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

    private void setErrorString(String error, String message) {
        if (error != null) {
            TextView textView = (TextView)findViewById(R.id.error_string);
            textView.setText(error);
        }

        if (message != null) {
            TextView textView = (TextView)findViewById(R.id.error_message);
            textView.setText(message);
        }

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

    private void getSchedule() {
        final Activity activity = this;
        new Thread(new Runnable() {
            private String error;
            private String message = getString(R.string.unknown_error);
            private boolean success = false;
            public void run() {
                try {
                    success = mApp.retrieveData(false);
                } catch (UnknownHostException e) {
                    error = e.toString();
                    message = getString(R.string.connect_error);
                } catch (SocketException e) {
                    error = e.toString();
                    message = getString(R.string.connect_error);
                } catch (FileNotFoundException e) {
                    error = e.toString();
                    message = getString(R.string.connect_error);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://market.android.com/details?id=com.dedaulus.cinematty"));
                    startActivity(intent);
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

    private void getCitiesList() {
        Intent intent = new Intent(this, CityListActivity.class);
        startActivityForResult(intent, GET_CURRENT_CITY);
        finish();
    }
}
