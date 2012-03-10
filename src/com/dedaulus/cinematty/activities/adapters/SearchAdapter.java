package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.*;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.Coordinate;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * User: Dedaulus
 * Date: 03.03.12
 * Time: 2:43
 */
public class SearchAdapter extends BaseAdapter implements LocationAdapter, MovieImageRetriever.MovieImageReceivedAction {
    private static class CinemaViewHolder {
        ImageView favIcon;
        TextView caption;
        RelativeLayout addressPanel;
        TextView address;
        TextView distance;
    }

    private static class MovieViewHolder {
        ImageView image;
        RelativeLayout imageLoadingPanel;
        TextView caption;
        TextView genres;
        TextView imdb;
        TextView favActors;
    }

    private static class ActorViewHolder {
        View favIconRegion;
        ImageView favIcon;
        TextView caption;
    }

    private static class SeparatorViewHolder {
        TextView caption;
    }

    public static final int SEPARATOR_TYPE_ID = -1;
    private static final int VIEW_TYPE_COUNT = 4;
    
    private Context context;
    private LayoutInflater inflater;
    private ArrayList items;
    private Location location;
    private final Object locationMutex = new Object();
    private MovieImageRetriever imageRetriever;
    
    private Pair<Integer, Integer> cinemasRange;
    private Pair<Integer, Integer> moviesRange;
    private Pair<Integer, Integer> actorsRange;

    {
        items = new ArrayList();
    }

    public SearchAdapter(Context context, List<Cinema> cinemas, Location location, List<Movie> movies, MovieImageRetriever imageRetriever, List<MovieActor> actors) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        
        int position = 0;
        if (cinemas != null && cinemas.size() != 0) {
            int prev = position + 1; // skip separator position 
            position += cinemas.size() + 1;
            items.add(context.getString(R.string.cinemas_separator));
            items.addAll(cinemas);
            cinemasRange = Pair.create(prev, position);
            this.location = location;
        }
        if (movies != null && movies.size() != 0) {
            int prev = position + 1;
            position += movies.size() + 1;
            items.add(context.getString(R.string.movies_separator));
            items.addAll(movies);
            moviesRange = Pair.create(prev, position);
            this.imageRetriever = imageRetriever;
        }
        if (actors != null && actors.size() != 0) {
            int prev = position + 1;
            position += actors.size() + 1;
            items.add(context.getString(R.string.actors_separator));
            items.addAll(actors);
            actorsRange = Pair.create(prev, position);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case Constants.CINEMA_TYPE_ID: {
                CinemaViewHolder viewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.cinema_item, null);
                    viewHolder = new CinemaViewHolder();
                    viewHolder.favIcon = (ImageView)convertView.findViewById(R.id.fav_icon_in_cinema_list);
                    viewHolder.caption = (TextView)convertView.findViewById(R.id.cinema_caption_in_cinema_list);
                    viewHolder.addressPanel = (RelativeLayout)convertView.findViewById(R.id.cinema_address_panel);
                    viewHolder.address = (TextView)convertView.findViewById(R.id.cinema_address_in_cinema_list);
                    viewHolder.distance = (TextView)convertView.findViewById(R.id.distance);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (CinemaViewHolder)convertView.getTag();
                }
                setCinemaView(position, viewHolder);
            }
            break;
            
            case Constants.MOVIE_TYPE_ID: {
                MovieViewHolder viewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.movie_item, null);
                    viewHolder = new MovieViewHolder();
                    viewHolder.image = (ImageView)convertView.findViewById(R.id.movie_list_icon);
                    viewHolder.imageLoadingPanel = (RelativeLayout)convertView.findViewById(R.id.movie_list_icon_loading);
                    viewHolder.caption = (TextView)convertView.findViewById(R.id.movie_caption_in_movie_list);
                    viewHolder.genres = (TextView)convertView.findViewById(R.id.movie_genre_in_movie_list);
                    viewHolder.imdb = (TextView)convertView.findViewById(R.id.imdb);
                    viewHolder.favActors = (TextView)convertView.findViewById(R.id.movie_actor_in_movie_list);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (MovieViewHolder)convertView.getTag();
                }
                setMovieView(position, viewHolder);
            }
            break;
            
            case Constants.ACTOR_TYPE_ID: {
                ActorViewHolder viewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.actor_item, null);
                    viewHolder = new ActorViewHolder();
                    viewHolder.favIconRegion = convertView.findViewById(R.id.fav_icon_region);
                    viewHolder.favIcon = (ImageView)viewHolder.favIconRegion.findViewById(R.id.fav_icon);
                    viewHolder.caption = (TextView)convertView.findViewById(R.id.actor_caption);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ActorViewHolder)convertView.getTag();
                }
                setActorView(position, viewHolder);
            }
            break;
            
            case SEPARATOR_TYPE_ID: {
                SeparatorViewHolder viewHolder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.data_divider, null);
                    viewHolder = new SeparatorViewHolder();
                    viewHolder.caption = (TextView)convertView.findViewById(R.id.caption);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (SeparatorViewHolder)convertView.getTag();
                }
                setSeparatorView(position, viewHolder);                
            }
            break;
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (cinemasRange != null && inRange(position, cinemasRange)) {
            return Constants.CINEMA_TYPE_ID;
        } else if (moviesRange != null && inRange(position, moviesRange)) {
            return Constants.MOVIE_TYPE_ID;
        } else if (actorsRange != null && inRange(position, actorsRange)) {
            return Constants.ACTOR_TYPE_ID;
        } else {
            return SEPARATOR_TYPE_ID;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    
    private boolean inRange(int position, Pair<Integer, Integer> range) {
        return position >= range.first && position < range.second;
    }

    private void setCinemaView(int position, CinemaViewHolder viewHolder) {
        final Cinema cinema = (Cinema)items.get(position);
        viewHolder.caption.setText(cinema.getName());

        if (cinema.getFavourite() > 0) {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_off);
        }

        viewHolder.favIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cinema.getFavourite() > 0) {
                    cinema.setFavourite(false);
                    ((ImageView)view).setImageResource(R.drawable.ic_list_fav_off);
                } else {
                    cinema.setFavourite(true);
                    ((ImageView)view).setImageResource(R.drawable.ic_list_fav_on);
                }
            }
        });

        String address = cinema.getAddress();
        if (address != null) {
            viewHolder.address.setText(address);
            Coordinate coordinate = cinema.getCoordinate();
            synchronized (locationMutex) {
                if (coordinate != null && location != null) {
                    float[] distance = new float[1];
                    Location.distanceBetween(coordinate.latitude, coordinate.longitude, location.getLatitude(), location.getLongitude(), distance);
                    int m = (int)distance[0];
                    viewHolder.distance.setText(DataConverter.metersToDistance(context, m));
                }
            }
            viewHolder.addressPanel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.addressPanel.setVisibility(View.GONE);
        }
    }

    private void setMovieView(int position, MovieViewHolder viewHolder) {
        final Movie movie = (Movie)items.get(position);
        viewHolder.caption.setText(movie.getName());

        String picId = movie.getPicId();
        if (picId != null) {
            Bitmap picture = imageRetriever.getImage(picId, true);
            if (picture != null) {
                viewHolder.imageLoadingPanel.setVisibility(View.GONE);
                viewHolder.image.setImageBitmap(picture);
                viewHolder.image.setVisibility(View.VISIBLE);
            } else {
                viewHolder.image.setVisibility(View.GONE);
                imageRetriever.addRequest(picId, true, this);
                viewHolder.imageLoadingPanel.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.imageLoadingPanel.setVisibility(View.GONE);
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

    private void setActorView(int position, ActorViewHolder viewHolder) {
        final MovieActor actor = (MovieActor)items.get(position);
        viewHolder.caption.setText(actor.getName());

        if (actor.getFavourite() > 0) {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_on);
        } else {
            viewHolder.favIcon.setImageResource(R.drawable.ic_list_fav_off);
        }
        viewHolder.favIconRegion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ImageView imageView = (ImageView)view.findViewById(R.id.fav_icon);
                if (actor.getFavourite() > 0) {
                    actor.setFavourite(false);
                    imageView.setImageResource(R.drawable.ic_list_fav_off);
                } else {
                    actor.setFavourite(true);
                    imageView.setImageResource(R.drawable.ic_list_fav_on);
                }
            }
        });
    }

    private void setSeparatorView(int position, SeparatorViewHolder viewHolder) {
        String caption = (String)items.get(position);
        viewHolder.caption.setText(caption);
    }

    public void setCurrentLocation(Location location) {
        synchronized (locationMutex) {
            this.location = location;
        }
        notifyDataSetChanged();
    }

    @Override
    public void onImageReceived(boolean downloaded) {
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
