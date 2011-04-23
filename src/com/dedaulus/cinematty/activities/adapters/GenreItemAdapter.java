package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 5:09
 */
public class GenreItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<MovieGenre> mGenres;

    public GenreItemAdapter(Context context, List<MovieGenre> genres) {
        mContext = context;
        mGenres = genres;
    }

    public int getCount() {
        return mGenres.size();
    }

    public Object getItem(int i) {
        return i >= 0 && i < mGenres.size() ? mGenres.get(i) : null;
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
        text.setText(mGenres.get(position).getGenre());
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
