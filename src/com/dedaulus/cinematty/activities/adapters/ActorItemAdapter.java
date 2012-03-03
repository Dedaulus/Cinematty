package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieActor;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:34
 */
public class ActorItemAdapter extends BaseAdapter implements SortableAdapter<MovieActor> {
    private Context context;
    private Map<String, MovieActor> actorEntries;
    private ArrayList<MovieActor> actors;

    public ActorItemAdapter(Context context, Map<String, MovieActor> actorEntries) {
        this.context = context;
        this.actorEntries = actorEntries;
        actors = new ArrayList<MovieActor>(actorEntries.values());
    }

    public int getCount() {
        return actors.size();
    }

    public Object getItem(int i) {
        return actors.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.actor_item, parent, false);
    }

    private void bindView(int position, View view) {
        MovieActor actor = actors.get(position);

        ImageView image = (ImageView)view.findViewById(R.id.fav_icon_in_actor_list);
        if (actor.getFavourite() > 0) {
            image.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            image.setImageResource(R.drawable.ic_list_fav_off);
        }
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onActorFavIconClick(view);
            }
        });

        TextView text = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        text.setText(actor.getName());
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView;

        if (view != null) {
            myView = view;
        } else {
            myView = newView(context, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    public void sortBy(Comparator<MovieActor> actorComparator) {
        Collections.sort(actors, actorComparator);
        notifyDataSetChanged();
    }

    private void onActorFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.actor_caption_in_actor_list);
        MovieActor actor = actorEntries.get(caption.getText().toString());
        if (actor.getFavourite() > 0) {
            actor.setFavourite(false);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_off);
        } else {
            actor.setFavourite(true);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_on);
        }
    }
}
