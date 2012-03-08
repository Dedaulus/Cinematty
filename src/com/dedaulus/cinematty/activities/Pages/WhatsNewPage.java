package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieActivity;
import com.dedaulus.cinematty.activities.adapters.PosterItemAdapter;
import com.dedaulus.cinematty.framework.MoviePoster;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:17
 */
public class WhatsNewPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private PosterItemAdapter posterItemAdapter;
    private Boolean binded = false;

    public WhatsNewPage(Context context, CinemattyApplication app) {
        this.context = context;
        this.app = app;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.whats_new, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.whats_new_caption);
    }

    public void onResume() {
        if (binded) {
            posterItemAdapter.onResume();
        }
    }

    public void onPause() {}

    public void onStop() {
        posterItemAdapter.onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        GridView whatsNewGrid = (GridView)view.findViewById(R.id.whats_new_grid);
        posterItemAdapter = new PosterItemAdapter(context, new ArrayList<MoviePoster>(settings.getPosters()), app.getImageRetrievers().getPosterImageRetriever());
        whatsNewGrid.setAdapter(posterItemAdapter);
        whatsNewGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onPosterItemClick(adapterView, view, i, l);
            }
        });
        binded = true;
        onResume();

        return view;
    }

    private void onPosterItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PosterItemAdapter adapter = (PosterItemAdapter)adapterView.getAdapter();
        MoviePoster poster = (MoviePoster)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, poster.getMovie(), null, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
