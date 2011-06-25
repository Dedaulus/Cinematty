package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.ActorItemAdapter;
import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.tools.CurrentState;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:28
 */
public class ActorListActivity extends Activity {
    private CinemattyApplication mApp;
    private CurrentState mCurrentState;
    private SortableAdapter<MovieActor> mActorListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actor_list);

        mApp = (CinemattyApplication)getApplication();
        mCurrentState = mApp.getCurrentState();

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_actor_list);
        ListView list = (ListView)findViewById(R.id.actor_list);

        if (mCurrentState.movie == null) {
            movieLabel.setVisibility(View.GONE);

            mActorListAdapter = new ActorItemAdapter(this, new ArrayList<MovieActor>(mApp.getActors()));
            list.setAdapter(mActorListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onActorItemClick(adapterView, view, i, l);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        mCurrentState = mApp.getCurrentState();
        mActorListAdapter.sortBy(new Comparator<MovieActor>() {
            public int compare(MovieActor a1, MovieActor a2) {
                if (a1.getFavourite() == a2.getFavourite()) {
                    return a1.getActor().compareTo(a2.getActor());
                } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
            }
        });

        super.onResume();
    }

    @Override
    protected void onPause() {
        mApp.saveFavouriteActors();

        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.revertCurrentState();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        String caption = textView.getText().toString();
        int actorId = mApp.getActors().indexOf(new MovieActor(caption));
        if (actorId != -1) {
            CurrentState state = mCurrentState.clone();
            state.actor = mApp.getActors().get(actorId);
            mApp.setCurrentState(state);

            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }
    }

    public void onFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.actor_caption_in_actor_list);

        int actorId = mApp.getActors().indexOf(new MovieActor(caption.getText().toString()));
        if (actorId != -1) {
            MovieActor actor = mApp.getActors().get(actorId);

            if (actor.getFavourite() > 0) {
                actor.setFavourite(false);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
            } else {
                actor.setFavourite(true);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
            }
        }
    }
}