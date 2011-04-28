package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.Calendar;
import java.util.List;

public class MovieItemWithScheduleAdapter extends BaseAdapter {
    private Context mContext;
    private List<Movie> mMovies;
    private Cinema mCinema;

    public MovieItemWithScheduleAdapter(Context context, List<Movie> movies, Cinema cinema) {
        mContext = context;
        mMovies = movies;
        mCinema = cinema;
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
        return layoutInflater.inflate(R.layout.movie_item_w_schedule, parent, false);
    }

    private void bindView(int position, View view) {
        Movie movie = mMovies.get(position);

        TextView captionView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        captionView.setText(movie.getCaption());

        TextView genreView = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (movie.getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : movie.getGenres()) {
                genres.append(genre.getGenre() + "/");
            }
            genres.delete(genres.length() - 1, genres.length());
            genreView.setText(genres.toString());

            genreView.setVisibility(View.VISIBLE);
        } else {
            genreView.setVisibility(View.GONE);
        }

        TextView scheduleView = (TextView)view.findViewById(R.id.movie_schedule_in_movie_list);
        List<Calendar> showTimes = mCinema.getShowTimes().get(movie);
        if (showTimes != null) {
            String showTimesStr = DataConverter.showTimesToString(showTimes);
            if (showTimesStr.length() != 0) {
                scheduleView.setText(showTimesStr);
                scheduleView.setVisibility(View.VISIBLE);
            } else {
                scheduleView.setVisibility(View.GONE);
            }
        } else {
            scheduleView.setVisibility(View.GONE);
        }
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = null;

        if (view != null) {
            myView = view;
        } else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }
}
