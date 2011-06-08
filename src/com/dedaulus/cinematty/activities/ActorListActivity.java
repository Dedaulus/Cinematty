package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.ActorItemAdapter;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.tools.CurrentState;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:28
 */
public class ActorListActivity extends Activity {
    private CinemattyApplication mApp;
    private CurrentState mCurrentState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actor_list);

        mApp = (CinemattyApplication)getApplication();
        mCurrentState = mApp.getCurrentState();

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_actor_list);
        ListView list = (ListView)findViewById(R.id.actor_list);

        if (mCurrentState.movie == null) {
            movieLabel.setVisibility(View.GONE);

            list.setAdapter(new ActorItemAdapter(this, new ArrayList<MovieActor>(mApp.getActors())));

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

        super.onResume();
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
}