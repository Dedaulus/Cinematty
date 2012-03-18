package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.SyncStatus;
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
    private CinemattyApplication app;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // TODO: uncomment following before release!!!
        //BugSenseHandler.setup(this, "97371d41");

        app = (CinemattyApplication)getApplication();
        if (app.getVersionState() == CinemattyApplication.NEW_INSTALLATION) {
            getCitiesList();
        } else {
            getSchedule();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            app.getLocationState().stopLocationListening();
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

    private void setErrorString(String message) {
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
            private SyncStatus syncStatus;
            public void run() {
                syncStatus = app.syncSchedule(activity);
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (syncStatus == SyncStatus.OK) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (syncStatus == SyncStatus.UPDATE_NEEDED) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://market.android.com/details?id=com.dedaulus.cinematty"));
                            startActivity(intent);
                            finish();
                        } else if (syncStatus == SyncStatus.BAD_RESPONSE) {
                            setErrorString(getString(R.string.bad_response));
                        } else {
                            setErrorString(getString(R.string.no_response));
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
