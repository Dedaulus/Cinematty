package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.ActorItemAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:28
 */
public class ActorListActivity extends Activity {
    CinemattyApplication mApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actor_list);

        mApp = (CinemattyApplication)getApplication();
        Movie movie = mApp.getCurrentMovie();
        TextView movieLabel = (TextView)findViewById(R.id.movie_label_in_actor_list);
        ListView list = (ListView)findViewById(R.id.actor_list);

        if (movie == null) {
            movieLabel.setVisibility(View.GONE);

            list.setAdapter(new ActorItemAdapter(this, new ArrayList<MovieActor>(mApp.getActors())));

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onActorItemClick(adapterView, view, i, l);
                }
            });
        }
        else {
            // to think!!!
        }
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String caption = ((TextView)view).getText().toString();
        int actorId = mApp.getActors().indexOf(new MovieActor(caption));
        if (actorId != -1) {
            mApp.setCurrentActor(mApp.getActors().get(actorId));
            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }
    }
}