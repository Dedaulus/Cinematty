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
import com.dedaulus.cinematty.activities.MovieListActivity;
import com.dedaulus.cinematty.activities.adapters.DirectorItemAdapter;
import com.dedaulus.cinematty.framework.MovieDirector;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * User: dedaulus
 * Date: 02.05.13
 * Time: 4:27
 */
public class DirectorsPage implements SliderPage {
    private Context context;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private DirectorItemAdapter directorItemAdapter;
    private boolean binded = false;

    public DirectorsPage(Context context, CinemattyApplication app) {
        this.context = context;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }

    @Override
    public View getView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.director_list, null, false);

        return bindView(view);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.directors_caption);
    }

    @Override
    public void onResume() {
        if (binded) {
            directorItemAdapter.sortBy(new Comparator<MovieDirector>() {
                public int compare(MovieDirector a1, MovieDirector a2) {
                    if (a1.getFavourite() == a2.getFavourite()) {
                        return a1.getName().compareTo(a2.getName());
                    } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
                }
            });
        }
    }

    @Override
    public void onPause() {
        settings.saveFavouriteDirectors();
    }

    @Override
    public void onStop() {}

    @Override
    public void setVisible(boolean visible) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        GridView grid = (GridView)view.findViewById(R.id.actor_list);
        directorItemAdapter = new DirectorItemAdapter(
                context, new ArrayList<MovieDirector>(settings.getDirectors().values()));
        grid.setAdapter(directorItemAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onDirectorItemClick(adapterView, view, i, l);
            }
        });

        binded = true;
        onResume();

        return view;
    }

    private void onDirectorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DirectorItemAdapter adapter = (DirectorItemAdapter)adapterView.getAdapter();
        MovieDirector director = (MovieDirector)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();

        ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_DIRECTOR, null, null, director, null, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
