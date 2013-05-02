package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Context;
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
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.UUID;

public class StartupActivity extends Activity
{
    private static final String UPDATE_URL_KEY = "url";
    private static final int GET_CURRENT_CITY = RESULT_FIRST_USER + 1;
    private CinemattyApplication app;
    private static volatile boolean inProgress;
    private static final Object mutex = new Object();
    private String sharedPageUrl;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // TODO: uncomment following before release!!!
        BugSenseHandler.initAndStartSession(this, "97371d41");

        setContentView(R.layout.splash_screen);

        DataConverter.SharedPageContent sharedPageContent = null;
        Uri data = getIntent().getData();
        if (data != null) {
            sharedPageUrl = data.toString();
            sharedPageContent = DataConverter.getSharedPageContent(sharedPageUrl);
        }

        app = (CinemattyApplication)getApplication();
        if (app.getVersionState() == CinemattyApplication.NEW_INSTALLATION) {
            getCitiesList();
        } else {
            synchronized (mutex) {
                if (inProgress) return;
                inProgress = true;
            }
            app.getLocationState().startLocationListening();
            getSchedule(sharedPageContent);
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

    private void getSchedule(DataConverter.SharedPageContent sharedPageContent) {
        final Activity activity = this;
        final DataConverter.SharedPageContent sharedPageContent1 = sharedPageContent;
        
        new Thread(new Runnable() {
            private SyncStatus syncStatus;
            public void run() {
                syncStatus = app.syncSchedule(activity, sharedPageContent1, false);
                inProgress = false;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (syncStatus == SyncStatus.OK) {
                            launchNormal(activity);
                            finish();
                        } else if (syncStatus == SyncStatus.UPDATE_NEEDED) {
                            setUpdateString(app.getConnect().get(UPDATE_URL_KEY));
                        } else if (syncStatus == SyncStatus.SHARED_PAGE_IN_WEBVIEW) {
                            launchWebView(activity, sharedPageUrl);
                            finish();
                        } else if (syncStatus == SyncStatus.SHARED_PAGE) {
                            launchMovieActivity(activity, syncStatus);
                            finish();
                        } else {
                            setErrorString(syncStatus);
                        }
                    }
                });
            }
        }).start();
    }

    private void launchNormal(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void launchWebView(Context context, String url) {
        Intent intent = new Intent(context, SharedPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SharedPageActivity.URL_ID, url);
        startActivity(intent);
    }

    private void launchMovieActivity(Context context, SyncStatus status) {
        if (status.cinema == null) {
            String cookie = UUID.randomUUID().toString();

            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_INFO,
                    null,
                    status.movie,
                    null,
                    null,
                    null);

            app.getActivitiesState().setState(cookie, state);

            Intent intent = new Intent(context, MovieActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        } else {
            String cookie = UUID.randomUUID().toString();

            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_INFO_W_SCHEDULE,
                    status.cinema,
                    status.movie,
                    null,
                    null,
                    null);

            app.getActivitiesState().setState(cookie, state);

            Intent intent = new Intent(context, MovieActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            intent.putExtra(MovieActivity.DAY_ID, status.day);
            startActivity(intent);
        }
    }

    private void getCitiesList() {
        Intent intent = new Intent(this, CityListActivity.class);
        startActivityForResult(intent, GET_CURRENT_CITY);
        finish();
    }
}
