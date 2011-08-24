package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.*;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.*;
import com.github.ysamlan.horizontalpager.HorizontalPager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 2:15
 */
public class MainActivity extends Activity implements LocationClient {
    private CinemattyApplication mApp;
    private SortableAdapter<Cinema> mCinemaListAdapter;
    private SortableAdapter<Movie> mMovieListAdapter;
    private SortableAdapter<MovieActor> mActorListAdapter;

    private static final int CATEGORIES_SCREEN = 0;
    private static final int WHATS_NEW_SCREEN  = 1;
    private static final int CINEMAS_SCREEN    = 2;
    private static final int MOVIES_SCREEN     = 3;
    private static final int ACTORS_SCREEN     = 4;
    private static final int GENRES_SCREEN     = 5;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        pager.setCurrentScreen(WHATS_NEW_SCREEN, false);

        mApp = (CinemattyApplication)getApplication();

        // Categories
        TextView cityLabel = (TextView)findViewById(R.id.city_caption);
        cityLabel.setText(mApp.getCurrentCity().getName());

        // What's new
        GridView whatsNewGrid = (GridView)findViewById(R.id.whats_new_grid);
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            whatsNewGrid.setNumColumns(2);
        } else {
            whatsNewGrid.setNumColumns(1);
        }
        whatsNewGrid.setAdapter(new PosterItemAdapter(this));

        // Cinemas
        ListView cinemaList = (ListView)findViewById(R.id.cinema_list);
        mCinemaListAdapter = new CinemaItemAdapter(this, new ArrayList<Cinema>(mApp.getCinemas()), mApp.getCurrentLocation());
        cinemaList.setAdapter(mCinemaListAdapter);
        cinemaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onCinemaItemClick(adapterView, view, i, l);
            }
        });

        // Movies
        ListView movieList = (ListView)findViewById(R.id.movie_list);
        mMovieListAdapter = new MovieItemAdapter(this, new ArrayList<Movie>(mApp.getMovies()), mApp.getPictureRetriever());
        movieList.setAdapter(mMovieListAdapter);
        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(view);
            }
        });

        // Actors
        ListView actorList = (ListView)findViewById(R.id.actor_list);
        mActorListAdapter = new ActorItemAdapter(this, new ArrayList<MovieActor>(mApp.getActors()));
        actorList.setAdapter(mActorListAdapter);
        actorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onActorItemClick(adapterView, view, i, l);
            }
        });

        // Genres
        ListView genreList = (ListView)findViewById(R.id.genre_list);
        genreList.setAdapter(new GenreItemAdapter(this, new ArrayList<MovieGenre>(mApp.getGenres())));
        genreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onGenreItemClick(adapterView, view, i, l);
            }
        });
    }

    @Override
    protected void onResume() {
        mApp.startListenLocation();
        mApp.addLocationClient(this);

        mCinemaListAdapter.sortBy(new CinemaComparator(mApp.getCinemaSortOrder(), mApp.getCurrentLocation()));

        mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder()));

        mActorListAdapter.sortBy(new Comparator<MovieActor>() {
            public int compare(MovieActor a1, MovieActor a2) {
                if (a1.getFavourite() == a2.getFavourite()) {
                    return a1.getActor().compareTo(a2.getActor());
                } else return a1.getFavourite() < a2.getFavourite() ? 1 : -1;
            }
        });

        super.onResume();
    }

    @Override
    protected void onPause() {
        mApp.removeLocationClient(this);
        mApp.stopListenLocation();

        mApp.saveFavouriteCinemas();

        mApp.saveFavouriteActors();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mApp.stopListenLocation();

        super.onBackPressed();
    }

    private boolean createCinemaListScreenMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cinema_list_menu, menu);

        switch (mApp.getCinemaSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_cinema_sort_by_caption).setChecked(true);
            break;

        case BY_FAVOURITE:
            menu.findItem(R.id.submenu_cinema_sort_by_favourite).setChecked(true);
            break;

        case BY_DISTANCE:
            menu.findItem(R.id.submenu_cinema_sort_by_distance).setChecked(true);
            break;

        default:
            break;
        }

        return true;
    }

    private boolean createMovieListScreenMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_list_menu, menu);

        switch (mApp.getMovieSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
            break;

        case BY_POPULAR:
            menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
            break;

        default:
            break;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        switch (pager.getCurrentScreen()) {
        case CATEGORIES_SCREEN:
            return false;

        case WHATS_NEW_SCREEN:
            return false;

        case CINEMAS_SCREEN:
            return createCinemaListScreenMenu(menu);

        case MOVIES_SCREEN:
            return createMovieListScreenMenu(menu);

        case ACTORS_SCREEN:
            return false;

        case GENRES_SCREEN:
            return false;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_cinema_sort:
            return true;

        case R.id.submenu_cinema_sort_by_caption:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_CAPTION, null));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_cinema_sort_by_favourite:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_FAVOURITE, null));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_FAVOURITE);
            item.setChecked(true);
            return true;

        case R.id.submenu_cinema_sort_by_distance:
            mCinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_DISTANCE, mApp.getCurrentLocation()));
            mApp.saveCinemaSortOrder(CinemaSortOrder.BY_DISTANCE);
            item.setChecked(true);
            return true;

        case R.id.menu_movie_sort:
            return true;

        case R.id.submenu_movie_sort_by_caption:
            mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION));
            mApp.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_movie_sort_by_popular:
            mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR));
            mApp.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
            item.setChecked(true);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void onLocationChanged(Location location) {
        LocationAdapter adapter = (LocationAdapter)mCinemaListAdapter;
        adapter.setCurrentLocation(location);
    }

    // Categories handlers

    public void onCityClick(View view) {
        deleteFile(getString(R.string.cities_file));

        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCinemaClick(View view) {
        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        pager.setCurrentScreen(CINEMAS_SCREEN, true);
        //String cookie = UUID.randomUUID().toString();
        //mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.CINEMA_LIST, null, null, null, null));
        //Intent intent = new Intent(this, CinemaListActivity.class);
        //intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        //startActivity(intent);
    }

    public void onMoviesClick(View view) {
        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        pager.setCurrentScreen(MOVIES_SCREEN, true);
        //String cookie = UUID.randomUUID().toString();
        //mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.MOVIE_LIST, null, null, null, null));
        //Intent intent = new Intent(this, MovieListActivity.class);
        //intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        //startActivity(intent);
    }

    public void onActorsClick(View view) {
        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        pager.setCurrentScreen(ACTORS_SCREEN, true);
        //String cookie = UUID.randomUUID().toString();
        //mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.ACTOR_LIST, null, null, null, null));
        //Intent intent = new Intent(this, ActorListActivity.class);
        //intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        //startActivity(intent);
    }

    public void onGenresClick(View view) {
        HorizontalPager pager = (HorizontalPager)findViewById(R.id.flipper);
        pager.setCurrentScreen(GENRES_SCREEN, true);
        //String cookie = UUID.randomUUID().toString();
        //mApp.setState(cookie, new ActivityState(ActivityState.ActivityType.GENRE_LIST, null, null, null, null));
        //Intent intent = new Intent(this, GenreListActivity.class);
        //intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        //startActivity(intent);
    }

    // Cinemas handlers

    private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.cinema_caption_in_cinema_list);
        String caption = textView.getText().toString();
        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption));
        if (cinemaId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_LIST_W_CINEMA, mApp.getCinemas().get(cinemaId), null, null, null);
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onCinemaFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.cinema_caption_in_cinema_list);

        int cinemaId = mApp.getCinemas().indexOf(new Cinema(caption.getText().toString()));
        if (cinemaId != -1) {
            Cinema cinema = mApp.getCinemas().get(cinemaId);

            if (cinema.getFavourite() > 0) {
                cinema.setFavourite(false);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
            } else {
                cinema.setFavourite(true);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
            }
        }
    }

    // Movies handlers

    private void onMovieItemClick(View view) {
        TextView textView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        String caption = textView.getText().toString();
        int movieId = mApp.getMovies().indexOf(new Movie(caption));
        if (movieId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_INFO, null, mApp.getMovies().get(movieId), null, null);
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    // Actors handlers

    private void onActorItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.actor_caption_in_actor_list);
        String caption = textView.getText().toString();
        int actorId = mApp.getActors().indexOf(new MovieActor(caption));
        if (actorId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_LIST_W_ACTOR, null, null, mApp.getActors().get(actorId), null);
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onActorFavIconClick(View view) {
        View parent = (View)view.getParent();
        TextView caption = (TextView)parent.findViewById(R.id.actor_caption_in_actor_list);

        int actorId = mApp.getActors().indexOf(new MovieActor(caption.getText().toString()));
        if (actorId != -1) {
            MovieActor actor = mApp.getActors().get(actorId);

            if (actor.getFavourite() > 0) {
                actor.setFavourite(false);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
            } else {
                actor.setFavourite(true);
                ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
            }
        }
    }

    // Genres handlers

    private void onGenreItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.genre_caption_in_genre_list);
        String caption = textView.getText().toString();
        int genreId = mApp.getGenres().indexOf(new MovieGenre(caption));
        if (genreId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_LIST_W_GENRE, null, null, null, mApp.getGenres().get(genreId));
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth()==getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                 orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}