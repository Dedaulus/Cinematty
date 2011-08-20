package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:28
 */
public class ActorListActivity extends Activity {
    private CinemattyApplication mApp;
    private SortableAdapter<MovieActor> mActorListAdapter;
    private ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actor_list);

        findViewById(R.id.actor_list_title_arrow_left).setVisibility(View.GONE);
        findViewById(R.id.actor_list_title_arrow_right).setVisibility(View.GONE);
        findViewById(R.id.actor_list_title_home).setVisibility(View.VISIBLE);

        mApp = (CinemattyApplication)getApplication();
        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_actor_list);
        ListView list = (ListView)findViewById(R.id.actor_list);

        switch (mState.activityType) {
        case ACTOR_LIST:
            movieLabel.setVisibility(View.GONE);
            mActorListAdapter = new ActorItemAdapter(this, new ArrayList<MovieActor>(mApp.getActors()));
            break;

        case ACTOR_LIST_W_MOVIE:
            movieLabel.setText(mState.movie.getCaption());
            movieLabel.setVisibility(View.VISIBLE);
            mActorListAdapter = new ActorItemAdapter(this, new ArrayList<MovieActor>(mState.movie.getActors()));
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }

        list.setAdapter(mActorListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onActorItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
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
    public void onBackPressed() {
        mApp.removeState(mStateId);

        super.onBackPressed();
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        String caption = textView.getText().toString();
        int actorId = mApp.getActors().indexOf(new MovieActor(caption));
        if (actorId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = mState.clone();
            state.actor = mApp.getActors().get(actorId);
            state.activityType = ActivityState.ActivityType.MOVIE_LIST_W_ACTOR;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onActorFavIconClick(View view) {
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

    public void onHomeButtonClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}