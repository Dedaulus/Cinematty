package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieListActivity;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:18
 */
public class ActorsPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private SortableAdapter<MovieActor> actorListAdapter;
    private boolean binded = false;

    public ActorsPage(Context context, CinemattyApplication app) {
        this.context = context;
        this.app = app;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.actor_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.actors_caption);
    }

    public void onResume() {
        if (binded) {
            actorListAdapter.sortBy(new Comparator<MovieActor>() {
                public int compare(MovieActor a1, MovieActor a2) {
                    if (a1.getFavourite() == a2.getFavourite()) {
                        return a1.getName().compareTo(a2.getName());
                    } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
                }
            });
        }
    }

    public void onPause() {
        settings.saveFavouriteActors();
    }

    public void onStop() {}

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        ListView list = (ListView)view.findViewById(R.id.actor_list);
        actorListAdapter = new ActorItemAdapter(context, settings.getActors());
        list.setAdapter(actorListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onActorItemClick(adapterView, view, i, l);
            }
        });

        binded = true;
        onResume();

        return view;
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ActorItemAdapter adapter = (ActorItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        MovieActor actor = (MovieActor)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();

        ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_ACTOR, null, null, actor, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
