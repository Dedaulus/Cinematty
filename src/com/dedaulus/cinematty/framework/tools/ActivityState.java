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
public class ActivityState implements Cloneable {
    public enum ActivityType {
        CINEMA_LIST,
        CINEMA_LIST_W_MOVIE,
        MOVIE_LIST,
        MOVIE_LIST_W_CINEMA,
        MOVIE_LIST_W_ACTOR,
        MOVIE_LIST_W_GENRE,
        ACTOR_LIST,
        ACTOR_LIST_W_MOVIE,
        GENRE_LIST,
        GENRE_LIST_W_MOVIE,
        CINEMA_INFO,
        MOVIE_INFO
    }

    public ActivityType activityType;
    public Cinema cinema;
    public Movie movie;
    public MovieActor actor;
    public MovieGenre genre;

    public ActivityState(ActivityType activityType, Cinema cinema, Movie movie, MovieActor actor, MovieGenre genre) {
        this.activityType = activityType;
        this.cinema = cinema;
        this.movie = movie;
        this.actor = actor;
        this.genre = genre;
    }

    public ActivityState clone() {
        return new ActivityState(activityType, cinema, movie, actor, genre);
    }
}
