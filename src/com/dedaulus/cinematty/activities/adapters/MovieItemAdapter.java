package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.tools.DataConverter;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:40
 */
public class MovieItemAdapter extends BaseAdapter implements SortableAdapter<Movie>, StoppableAndResumable, MovieImageRetriever.MovieImageReceivedAction {
    private static class MovieViewHolder {
        ImageView image;
        View progress;
        TextView caption;
        TextView genres;
        TextView imdb;
        TextView favActors;
    }
    
    private Context context;
    private LayoutInflater inflater;
    private IdleDataSetChangeNotifier notifier;
    private ArrayList<Movie> movies;
    private MovieImageRetriever imageRetriever;

    public MovieItemAdapter(Context context, IdleDataSetChangeNotifier notifier, ArrayList<Movie> movies, MovieImageRetriever imageRetriever) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.notifier = notifier;
        notifier.setAdapter(this);
        this.movies = movies;
        this.imageRetriever = imageRetriever;
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

    private void bindView(int position, MovieViewHolder viewHolder) {
        final Movie movie = movies.get(position);
        viewHolder.caption.setText(movie.getName());

        String picId = movie.getPicId();
        if (picId != null) {
            Bitmap picture = imageRetriever.getImage(picId, true);
            if (picture != null) {
                viewHolder.progress.setVisibility(View.GONE);
                viewHolder.image.setImageBitmap(picture);
                viewHolder.image.setVisibility(View.VISIBLE);
            } else {
                viewHolder.image.setVisibility(View.GONE);
                imageRetriever.addRequest(picId, true, this);
                viewHolder.progress.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.progress.setVisibility(View.GONE);
            viewHolder.image.setImageResource(R.drawable.ic_list_blank_movie);
            viewHolder.image.setVisibility(View.VISIBLE);
        }

        String genres = DataConverter.genresToString(movie.getGenres().values());
        if (genres.length() != 0) {
            viewHolder.genres.setText(genres);
            viewHolder.genres.setVisibility(View.VISIBLE);
        } else {
            viewHolder.genres.setVisibility(View.GONE);
        }

        String imdb = DataConverter.imdbToString(movie.getImdb());
        if (imdb.length() != 0) {
            viewHolder.imdb.setText(imdb);
            viewHolder.imdb.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imdb.setVisibility(View.GONE);
        }

        String actors = DataConverter.favActorsToString(movie.getActors().values());
        if (actors.length() != 0) {
            viewHolder.favActors.setText(actors);
            viewHolder.favActors.setVisibility(View.VISIBLE);
        } else {
            viewHolder.favActors.setVisibility(View.GONE);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MovieViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.movie_item, null);
            viewHolder = new MovieViewHolder();
            viewHolder.image = (ImageView)convertView.findViewById(R.id.movie_icon);
            viewHolder.progress = convertView.findViewById(R.id.progress);
            viewHolder.caption = (TextView)convertView.findViewById(R.id.movie_caption);
            viewHolder.genres = (TextView)convertView.findViewById(R.id.movie_genre);
            viewHolder.imdb = (TextView)convertView.findViewById(R.id.imdb);
            viewHolder.favActors = (TextView)convertView.findViewById(R.id.movie_actor);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MovieViewHolder)convertView.getTag();
        }
        bindView(position, viewHolder);
        return convertView;
    }

    public void sortBy(Comparator<Movie> movieComparator) {
        Collections.sort(movies, movieComparator);
        notifyDataSetChanged();
    }

    @Override
    public boolean isSorted(Comparator<Movie> movieComparator) {
        throw new RuntimeException("Method not implemented");
    }

    public void onImageReceived(boolean success) {
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                notifier.askForNotifyDataSetChanged();
            }
        });
    }

    public void onStop() {
        imageRetriever.saveState();
    }

    public void onResume() {}
}
