package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieActor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:34
 */
public class ActorItemAdapter extends BaseAdapter implements SortableAdapter<MovieActor> {
    private Context mContext;
    private List<MovieActor> mActors;

    public ActorItemAdapter(Context context, List<MovieActor> actors) {
        mContext = context;
        mActors = actors;
    }

    public int getCount() {
        return mActors.size();
    }

    public Object getItem(int i) {
        return mActors.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.actor_item, parent, false);
    }

    private void bindView(int position, View view) {
        MovieActor actor = mActors.get(position);

        ImageView image = (ImageView)view.findViewById(R.id.fav_icon_in_actor_list);
        if (actor.getFavourite() > 0) {
            image.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            image.setImageResource(android.R.drawable.btn_star_big_off);
        }
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onActorFavIconClick(view);
            }
        });

        TextView text = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        text.setText(actor.getActor());
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView;

        if (view != null) {
            myView = view;
        } else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    public void sortBy(Comparator<MovieActor> actorComparator) {
        Collections.sort(mActors, actorComparator);
        notifyDataSetChanged();
    }

    private void onActorFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.actor_caption_in_actor_list);

        int actorId = mActors.indexOf(new MovieActor(caption.getText().toString()));
        if (actorId != -1) {
            MovieActor actor = mActors.get(actorId);

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
