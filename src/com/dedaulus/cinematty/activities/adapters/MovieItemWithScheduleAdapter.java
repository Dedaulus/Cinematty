package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.*;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:41
 */
public class MovieItemWithScheduleAdapter extends BaseAdapter implements SortableAdapter<Movie>, MovieImageRetriever.MovieImageReceivedAction, StoppableAndResumable {
    private Context context;
    private ArrayList<Movie> movies;
    private Cinema cinema;
    private int currentDay;
    private MovieImageRetriever imageRetriever;
    private MovieImageReceivedActionHandler imageReceivedActionHandler;

    public MovieItemWithScheduleAdapter(Context context, ArrayList<Movie> movies, Cinema cinema, int day, MovieImageRetriever imageRetriever) {
        this.context = context;
        this.movies = movies;
        this.cinema = cinema;
        currentDay = day;
        this.imageRetriever = imageRetriever;
        imageReceivedActionHandler = new MovieImageReceivedActionHandler(this, (Activity)this.context);
    }

    public int getCount() {
        return movies.size();
    }

    public Object getItem(int i) {
        return movies.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.movie_item_w_schedule, parent, false);
    }

    private void bindView(int position, View view) {
        Movie movie = movies.get(position);

        RelativeLayout progressBar = (RelativeLayout)view.findViewById(R.id.movie_list_icon_loading);
        ImageView imageView = (ImageView)view.findViewById(R.id.movie_list_icon);

        imageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        String picId = movie.getPicId();
        if (picId != null) {
            Bitmap picture = imageRetriever.getImage(picId, true);
            if (picture != null) {
                imageView.setImageBitmap(picture);
                imageView.setBackgroundResource(R.drawable.picture_border);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageRetriever.addRequest(picId, true, imageReceivedActionHandler);
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_list_blank_movie);
            imageView.setBackgroundResource(0);
            imageView.setVisibility(View.VISIBLE);
        }

        TextView captionView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        captionView.setText(movie.getName());

        TextView genreView = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (movie.getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : movie.getGenres().values()) {
                genres.append(genre.getName()).append("/");
            }
            genres.delete(genres.length() - 1, genres.length());
            genreView.setText(genres.toString());
            genreView.setVisibility(View.VISIBLE);
        } else {
            genreView.setVisibility(View.GONE);
        }

        TextView imdbView = (TextView)view.findViewById(R.id.imdb);
        String imdb = DataConverter.imdbToString(movie.getImdb());
        if (imdb.length() != 0) {
            imdbView.setText(imdb);
            imdbView.setVisibility(View.VISIBLE);
        } else {
            imdbView.setVisibility(View.GONE);
        }

        TextView actorView = (TextView)view.findViewById(R.id.movie_actor_in_movie_list);
        if (movie.getActors().size() != 0) {
            StringBuilder actors = new StringBuilder();
            for (MovieActor actor : movie.getActors().values()) {
                if (actor.getFavourite() != 0) {
                    actors.append(actor.getName()).append(", ");
                }
            }

            if (actors.length() != 0) {
                actors.delete(actors.length() - 2, actors.length());
                actorView.setText(actors.toString());
                actorView.setVisibility(View.VISIBLE);
            } else {
                actorView.setVisibility(View.GONE);
            }
        } else {
            actorView.setVisibility(View.GONE);
        }

        TextView scheduleView = (TextView)view.findViewById(R.id.movie_schedule_in_movie_list);
        TextView timeLeftView = (TextView)view.findViewById(R.id.time_left_in_movie_list);

        Map<String, Pair<Movie, List<Calendar>>> showTimesEntries = cinema.getShowTimes(currentDay);
        if (!showTimesEntries.isEmpty()) {
            List<Calendar> showTimes = showTimesEntries.get(movie.getName()).second;
            String showTimesStr = DataConverter.showTimesToString(showTimes);
            if (showTimesStr.length() != 0) {
                scheduleView.setText(showTimesStr);
                scheduleView.setVisibility(View.VISIBLE);
            } else {
                scheduleView.setVisibility(View.GONE);
            }
            if (currentDay == Constants.TODAY_SCHEDULE) {
                SpannableString timeLeftString = DataConverter.showTimesToClosestTimeString(context, showTimes);
                timeLeftView.setText(timeLeftString);
            } else {
                timeLeftView.setVisibility(View.GONE);
            }
        } else {
            scheduleView.setVisibility(View.GONE);
            timeLeftView.setVisibility(View.GONE);
        }
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

    public void sortBy(Comparator<Movie> movieComparator) {
        Collections.sort(movies, movieComparator);
        notifyDataSetChanged();
    }

    public void onImageReceived(boolean success) {
        notifyDataSetChanged();
    }

    public void onStop() {
        imageReceivedActionHandler.stop();
        imageRetriever.saveState();
    }

    public void onResume() {
        imageReceivedActionHandler.start();
    }
}
