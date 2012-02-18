package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 5:09
 */
public class GenreItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MovieGenre> genres;

    public GenreItemAdapter(Context context, ArrayList<MovieGenre> genres) {
        this.context = context;
        this.genres = genres;
    }

    public int getCount() {
        return genres.size();
    }

    public Object getItem(int i) {
        return genres.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.genre_item, parent, false);
    }

    private void bindView(int position, View view) {
        TextView text = (TextView)view.findViewById(R.id.genre_caption_in_genre_list);
        text.setText(genres.get(position).getName());
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
}
