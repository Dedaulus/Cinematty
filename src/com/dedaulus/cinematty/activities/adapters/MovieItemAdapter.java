package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.MoviePictureReceiver;
import com.dedaulus.cinematty.framework.tools.OnPictureReceiveAction;
import com.dedaulus.cinematty.framework.tools.PictureType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:40
 */
public class MovieItemAdapter extends BaseAdapter implements SortableAdapter<Movie>, OnPictureReceiveAction, StoppableAndResumable {
    private Context mContext;
    private List<Movie> mMovies;
    private PictureRetriever mPictureRetriever;
    private MoviePictureReceiver mPictureReceiver;

    public MovieItemAdapter(Context context, List<Movie> movies, PictureRetriever pictureRetriever) {
        mContext = context;
        mMovies = movies;
        mPictureRetriever = pictureRetriever;
        mPictureReceiver = new MoviePictureReceiver(this, (Activity)mContext);
    }

    public int getCount() {
        return mMovies.size();
    }

    public Object getItem(int i) {
        return mMovies.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.movie_item, parent, false);
    }

    private void bindView(int position, View view) {
        Movie movie = mMovies.get(position);

        RelativeLayout progressBar = (RelativeLayout)view.findViewById(R.id.movie_list_icon_loading);
        ImageView imageView = (ImageView)view.findViewById(R.id.movie_list_icon);

        imageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        String picId = movie.getPicId();
        if (picId != null) {
            Bitmap picture = mPictureRetriever.getPicture(picId, PictureType.LIST_BIG);
            if (picture != null) {
                imageView.setImageBitmap(picture);
                imageView.setBackgroundResource(R.drawable.picture_border);
                imageView.setVisibility(View.VISIBLE);
            } else {
                mPictureRetriever.addRequest(picId, PictureType.LIST_BIG, mPictureReceiver);
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_blank_movie);
            imageView.setBackgroundResource(0);
            imageView.setVisibility(View.VISIBLE);
        }

        TextView text = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        text.setText(movie.getCaption());

        text = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (movie.getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : movie.getGenres()) {
                genres.append(genre.getGenre()).append("/");
            }
            genres.delete(genres.length() - 1, genres.length());
            text.setText(genres.toString());

            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }

        text = (TextView)view.findViewById(R.id.movie_actor_in_movie_list);
        if (movie.getActors().size() != 0) {
            StringBuilder actors = new StringBuilder();
            for (MovieActor actor : movie.getActors()) {
                if (actor.getFavourite() != 0) {
                    actors.append(actor.getActor()).append(", ");
                }
            }

            if (actors.length() != 0) {
                actors.delete(actors.length() - 2, actors.length());
                text.setText(actors.toString());
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }

        } else {
            text.setVisibility(View.GONE);
        }

        View movieOnlyTomorrow = view.findViewById(R.id.movie_only_tomorrow_in_movie_list);
        if (movie.getCinemas(Constants.TODAY_SCHEDULE) == null) {
            movieOnlyTomorrow.setVisibility(View.VISIBLE);
        } else {
            movieOnlyTomorrow.setVisibility(View.GONE);
        }
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

    public void sortBy(Comparator<Movie> movieComparator) {
        Collections.sort(mMovies, movieComparator);
        notifyDataSetChanged();
    }

    public void OnPictureReceive(String picId, int pictureType, boolean success) {
        notifyDataSetChanged();
    }

    public void onStop() {
        mPictureReceiver.stop();
    }

    public void onResume() {
        mPictureReceiver.start();
    }
}
