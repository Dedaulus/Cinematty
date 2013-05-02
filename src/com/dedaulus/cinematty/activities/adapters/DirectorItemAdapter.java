package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieDirector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: dedaulus
 * Date: 02.05.13
 * Time: 4:56
 */
public class DirectorItemAdapter extends BaseAdapter implements SortableAdapter<MovieDirector> {
    private static class ViewHolder {
        View favIconRegion;
        ImageView favIcon;
        TextView caption;
    }

    private ArrayList<MovieDirector> directors;
    LayoutInflater inflater;

    public DirectorItemAdapter(Context context, ArrayList<MovieDirector> directors) {
        inflater = LayoutInflater.from(context);
        this.directors = directors;
    }

    @Override
    public void sortBy(Comparator<MovieDirector> comparator) {
        Collections.sort(directors, comparator);
        notifyDataSetChanged();
    }

    @Override
    public boolean isSorted(Comparator<MovieDirector> comparator) {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public int getCount() {
        return directors.size();
    }

    @Override
    public Object getItem(int position) {
        return directors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.director_item, null);
            viewHolder = new ViewHolder();
            viewHolder.caption = (TextView)convertView.findViewById(R.id.director_caption);
            viewHolder.favIconRegion = convertView.findViewById(R.id.fav_icon_region);
            viewHolder.favIcon = (ImageView)viewHolder.favIconRegion.findViewById(R.id.fav_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        bindView(position, viewHolder);

        return convertView;
    }

    private void bindView(int position, ViewHolder viewHolder) {
        final MovieDirector director = directors.get(position);
        viewHolder.caption.setText(director.getName());

        if (director.getFavourite() > 0) {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_off);
        }
        viewHolder.favIconRegion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ImageView imageView = (ImageView)view.findViewById(R.id.fav_icon);
                if (director.getFavourite() > 0) {
                    director.setFavourite(false);
                    imageView.setImageResource(R.drawable.ic_list_fav_off);
                } else {
                    director.setFavourite(true);
                    imageView.setImageResource(R.drawable.ic_list_fav_on);
                }
            }
        });
    }
}
