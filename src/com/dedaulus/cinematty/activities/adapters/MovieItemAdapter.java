package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:40
 */
public class MovieItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<Movie> mMovies;

    public MovieItemAdapter(Context context, List<Movie> movies) {
        mContext = context;
        mMovies = movies;
    }

    public int getCount() {
        return mMovies.size();
    }

    public Object getItem(int i) {
        return i >= 0 && i < mMovies.size() ? mMovies.get(i) : null;
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.movie_item, parent, false);
    }

    private void bindView(int position, View view) {
        TextView text = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        text.setText(mMovies.get(position).getCaption());

        text = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (mMovies.get(position).getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : mMovies.get(position).getGenres()) {
                genres.append(genre.getGenre() + "/");
            }
            genres.delete(genres.length() - 1, genres.length());
            text.setText(genres.toString());

            text.setVisibility(View.VISIBLE);
        }
        else {
            text.setVisibility(View.GONE);
        }
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
