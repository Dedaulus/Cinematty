package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.SyncStatus;

public class StartupActivity extends Activity
{
    private static final String UPDATE_URL_KEY = "url";
    private static final int GET_CURRENT_CITY = RESULT_FIRST_USER + 1;
    private CinemattyApplication app;
    private static volatile boolean inProgress;
    private static final Object mutex = new Object();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // TODO: uncomment following before release!!!
        BugSenseHandler.setup(this, "97371d41");

        app = (CinemattyApplication)getApplication();
        if (app.getVersionState() == CinemattyApplication.NEW_INSTALLATION) {
            getCitiesList();
        } else {
            synchronized (mutex) {
                if (inProgress) return;
                inProgress = true;
            }
            app.getLocationState().startLocationListening();
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

    private void setErrorString(SyncStatus syncStatus) {
        TextView textView = (TextView)findViewById(R.id.error_message);
        Button button = (Button)findViewById(R.id.error_button);
        String message;
        if (syncStatus == SyncStatus.BAD_RESPONSE) {
            message = getString(R.string.sync_bad_response);
        } else if (syncStatus == SyncStatus.OUT_OF_DATE) {
            message = getString(R.string.sync_out_of_date);
            button.setText(getString(R.string.sync_out_of_date_button));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    app.resetSyncStatus();
                    getCitiesList();
                }
            });
            button.setVisibility(View.VISIBLE);
        } else {
            message = getString(R.string.sync_no_response);
        }
        textView.setText(message);


        findViewById(R.id.loading_schedule_panel).setVisibility(View.INVISIBLE);
        findViewById(R.id.loading_error_panel).setVisibility(View.VISIBLE);
    }

    private void setUpdateString(String updateUrl) {
        final String url;
        if (updateUrl == null || updateUrl.length() == 0) {
            url = "https://play.google.com/store/apps/details?id=com.dedaulus.cinematty";
        } else {
            url = updateUrl;
        }

        TextView textView = (TextView)findViewById(R.id.error_message);
        textView.setText(R.string.sync_update_needed);
        Button button = (Button)findViewById(R.id.error_button);
        button.setText(getString(R.string.sync_update_needed_button));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        button.setVisibility(View.VISIBLE);

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
                syncStatus = app.syncSchedule(activity, false);
                inProgress = false;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (syncStatus == SyncStatus.OK) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (syncStatus == SyncStatus.UPDATE_NEEDED) {
                            setUpdateString(app.getConnect().get(UPDATE_URL_KEY));
                        } else {
                            setErrorString(syncStatus);
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
