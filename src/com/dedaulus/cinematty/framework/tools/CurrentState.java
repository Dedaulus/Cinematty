package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;

/**
 * User: Dedaulus
 * Date: 12.05.11
 * Time: 22:40
 */
public class CurrentState implements Cloneable {
    public Cinema cinema;
    public Movie movie;
    public MovieActor actor;
    public MovieGenre genre;

    public CurrentState(Cinema cinema, Movie movie, MovieActor actor, MovieGenre genre) {
        this.cinema = cinema;
        this.movie = movie;
        this.actor = actor;
        this.genre = genre;
    }

    public CurrentState clone() {
        return new CurrentState(cinema, movie, actor, genre);
    }
}
