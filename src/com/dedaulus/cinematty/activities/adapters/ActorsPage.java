package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
    private Context mContext;
    private CinemattyApplication mApp;
    private SortableAdapter<MovieActor> mActorListAdapter;
    private boolean mBinded = false;

    public ActorsPage(Context context, CinemattyApplication app) {
        mContext = context;
        mApp = app;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.actor_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return mContext.getString(R.string.actors_caption);
    }

    public void onResume() {
        if (mBinded) {
            mActorListAdapter.sortBy(new Comparator<MovieActor>() {
                public int compare(MovieActor a1, MovieActor a2) {
                    if (a1.getFavourite() == a2.getFavourite()) {
                        return a1.getActor().compareTo(a2.getActor());
                    } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
                }
            });
        }
    }

    public void onPause() {
        mApp.saveFavouriteActors();
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
        mActorListAdapter = new ActorItemAdapter(mContext, new ArrayList<MovieActor>(mApp.getActors()));
        list.setAdapter(mActorListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onActorItemClick(adapterView, view, i, l);
            }
        });
        mBinded = true;
        onResume();

        return view;
    }

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        String caption = textView.getText().toString();
        int actorId = mApp.getActors().indexOf(new MovieActor(caption));
        if (actorId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_LIST_W_ACTOR, null, null, mApp.getActors().get(actorId), null);
            mApp.setState(cookie, state);

            Intent intent = new Intent(mContext, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            mContext.startActivity(intent);
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
}
