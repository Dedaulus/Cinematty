package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieActor;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 4:34
 */
public class ActorItemAdapter extends BaseAdapter {
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
        return i >= 0 && i < mActors.size() ? mActors.get(i) : null;
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.actor_item, parent, false);
    }

    private void bindView(int position, View view) {
        TextView text = (TextView)view.findViewById(R.id.actor_item_in_list);
        text.setText(mActors.get(position).getActor());
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = null;

        if (view != null) {
            myView = view;
        }
        else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }
}