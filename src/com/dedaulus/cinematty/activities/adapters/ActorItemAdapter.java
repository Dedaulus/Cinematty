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
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

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
    private QuickAction mQuickAction;
    private String mCurrentActorName;

    public ActorItemAdapter(Context context, List<MovieActor> actors) {
        mContext = context;
        mActors = actors;
        mCurrentActorName = null;
        mQuickAction = new QuickAction(mContext);
        setQuickActions(this);
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

        image = (ImageView)view.findViewById(R.id.more_icon_in_actor_list);
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onMoreIconClick(view);
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

    private void setQuickActions(ActorItemAdapter adapter) {
        ActionItem googleAction = new ActionItem(mContext.getResources().getDrawable(R.drawable.ic_more_google));
        googleAction.setTitle(mContext.getString(R.string.google));

        ActionItem yandexAction = new ActionItem(mContext.getResources().getDrawable(R.drawable.ic_more_yandex));
        yandexAction.setTitle(mContext.getString(R.string.yandex));

        ActionItem wikiAction = new ActionItem(mContext.getResources().getDrawable(R.drawable.ic_more_wiki));
        wikiAction.setTitle(mContext.getString(R.string.wiki));

        mQuickAction.addActionItem(googleAction);
        mQuickAction.addActionItem(yandexAction);
        mQuickAction.addActionItem(wikiAction);

        final ActorItemAdapter aia = adapter;

        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(int pos) {
                aia.onMoreItemClick(pos);
            }
        });
    }

    private void onMoreItemClick(int pos) {
        String url;
        switch (pos) {
        case 1:
            url = mContext.getString(R.string.yandex_search_url);
            break;
        case 2:
            url = mContext.getString(R.string.wiki_search_url);
            break;
        case 0:
        default:
            url = mContext.getString(R.string.google_search_url);
            break;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url + mCurrentActorName));
        mContext.startActivity(intent);
    }

    public void onMoreIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.actor_caption_in_actor_list);
        mCurrentActorName = caption.getText().toString();

        mQuickAction.show(view);
    }
}
