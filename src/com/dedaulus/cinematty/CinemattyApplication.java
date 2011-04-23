package com.dedaulus.cinematty;

import android.app.Application;
import android.content.SharedPreferences;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.CinemaSortOrder;
import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.ScheduleReceiver;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.Map;

/*
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:41
 */
public class CinemattyApplication extends Application {
    private UniqueSortedList<Cinema> mCinemas;
    private UniqueSortedList<Movie> mMovies;
    private UniqueSortedList<MovieActor> mActors;
    private UniqueSortedList<MovieGenre> mGenres;
    private Cinema mCurrentCinema;
    private Movie mCurrentMovie;
    private MovieActor mCurrentActor;
    private MovieGenre mCurrentGenre;

    private static final String FAV_CINEMAS_FILE = "cinematty_fav_cinemas";
    private static final String PREFERENCES_FILE = "cinematty_preferences";
    private static final String PREF_CINEMA_SORT_ORDER = "cinema_sort_order";

    {
        mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
        mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
        mActors = new UniqueSortedList<MovieActor>(new DefaultComparator<MovieActor>());
        mGenres = new UniqueSortedList<MovieGenre>(new DefaultComparator<MovieGenre>());
    }

    public void retrieveData() {
        ScheduleReceiver receiver = new ScheduleReceiver(getString(R.string.default_url));
        receiver.getSchedule(mCinemas, mMovies, mActors, mGenres);

        Map<String, ?> favs = getFavouriteCinemas();
        for (String caption : favs.keySet()) {
            int cinemaId = mCinemas.indexOf(new Cinema(caption));
            if (cinemaId != -1) {
                mCinemas.get(cinemaId).setFavourite(((Long)favs.get(caption)).longValue());
            }
        }
    }

    public boolean isUpToDate() {
        //return mCinemas.size() != 0;
        return false;
    }

    public UniqueSortedList<Cinema> getCinemas() {
        return mCinemas;
    }

    public UniqueSortedList<Movie> getMovies() {
        return mMovies;
    }

    public UniqueSortedList<MovieActor> getActors() {
        return mActors;
    }

    public UniqueSortedList<MovieGenre> getGenres() {
        return mGenres;
    }

    public void setCurrentCinema(Cinema cinema) {
        mCurrentCinema = cinema;
    }

    public Cinema getCurrentCinema() {
        return mCurrentCinema;
    }

    public void setCurrentMovie(Movie movie) {
        mCurrentMovie = movie;
    }

    public Movie getCurrentMovie() {
        return mCurrentMovie;
    }

    public void setCurrentActor(MovieActor actor) {
        mCurrentActor = actor;
    }

    public MovieActor getCurrentActor() {
        return mCurrentActor;
    }

    public void setCurrentGenre(MovieGenre genre) {
        mCurrentGenre = genre;
    }

    public MovieGenre getCurrentGenre() {
        return mCurrentGenre;
    }

    public void saveFavouriteCinemas() {
        SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (Cinema cinema : mCinemas) {
            if (cinema.getFavourite() > 0) {
                editor.putLong(cinema.getCaption(), cinema.getFavourite());
            } else {
                editor.remove(cinema.getCaption());
            }
        }

        editor.commit();
    }

    private Map<String, ?> getFavouriteCinemas() {
        SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE, MODE_PRIVATE);
        return preferences.getAll();
    }

    public void saveCinemasSortOrder(CinemaSortOrder sortOrder) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_CINEMA_SORT_ORDER, sortOrder.ordinal());

        editor.commit();
    }

    public CinemaSortOrder getCinemaSortOrder() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        int order = preferences.getInt(PREF_CINEMA_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());

        for (CinemaSortOrder sortOrder : CinemaSortOrder.values()) {
            if (sortOrder.ordinal() == order) return sortOrder;
        }

        return CinemaSortOrder.BY_CAPTION;
    }
}
