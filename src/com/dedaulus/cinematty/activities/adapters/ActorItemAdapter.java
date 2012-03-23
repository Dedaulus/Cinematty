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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:34
 */
public class ActorItemAdapter extends BaseAdapter implements SortableAdapter<MovieActor> {
    private static class ViewHolder {
        View favIconRegion;
        ImageView favIcon;
        TextView caption;
    }
    
    private ArrayList<MovieActor> actors;
    LayoutInflater inflater;

    public ActorItemAdapter(Context context, ArrayList<MovieActor> actors) {
        inflater = LayoutInflater.from(context);
        this.actors = actors;
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

    private void bindView(int position, ViewHolder viewHolder) {
        final MovieActor actor = actors.get(position);
        viewHolder.caption.setText(actor.getName());

        if (actor.getFavourite() > 0) {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_off);
        }
        viewHolder.favIconRegion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ImageView imageView = (ImageView)view.findViewById(R.id.fav_icon);
                if (actor.getFavourite() > 0) {
                    actor.setFavourite(false);
                    imageView.setImageResource(R.drawable.ic_list_fav_off);
                } else {
                    actor.setFavourite(true);
                    imageView.setImageResource(R.drawable.ic_list_fav_on);
                }
            }
        });
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.actor_item, null);
            viewHolder = new ViewHolder();
            viewHolder.caption = (TextView)convertView.findViewById(R.id.actor_caption);
            viewHolder.favIconRegion = convertView.findViewById(R.id.fav_icon_region);
            viewHolder.favIcon = (ImageView)viewHolder.favIconRegion.findViewById(R.id.fav_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        bindView(position, viewHolder);

        return convertView;
    }

    public void sortBy(Comparator<MovieActor> actorComparator) {
        Collections.sort(actors, actorComparator);
        notifyDataSetChanged();
    }
}
