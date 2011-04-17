package com.dedaulus.cinematty;

import android.app.Application;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.ScheduleReceiver;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

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

    {
        mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
        mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
        mActors = new UniqueSortedList<MovieActor>(new DefaultComparator<MovieActor>());
        mGenres = new UniqueSortedList<MovieGenre>(new DefaultComparator<MovieGenre>());
    }

    public void retrieveData() {
        ScheduleReceiver so = new ScheduleReceiver(getString(R.string.default_url));

        so.getSchedule(mCinemas, mMovies, mActors, mGenres);
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
}
