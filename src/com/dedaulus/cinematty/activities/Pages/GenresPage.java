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
import com.dedaulus.cinematty.activities.adapters.GenreItemAdapter;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:18
 */
public class GenresPage implements SliderPage {
    private Context context;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private boolean visible = false;

    public GenresPage(Context context, CinemattyApplication app) {
        this.context = context;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.genre_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.genres_caption);
    }

    public void onResume() {}

    public void onPause() {}

    public void onStop() {}

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        GridView grid = (GridView)view.findViewById(R.id.genre_list);
        grid.setAdapter(new GenreItemAdapter(context, new ArrayList<MovieGenre>(settings.getGenres().values())));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onGenreItemClick(adapterView, view, i, l);
            }
        });

        return view;
    }

    private void onGenreItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GenreItemAdapter adapter = (GenreItemAdapter)adapterView.getAdapter();
        MovieGenre genre = (MovieGenre)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_GENRE, null, null, null, genre);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
