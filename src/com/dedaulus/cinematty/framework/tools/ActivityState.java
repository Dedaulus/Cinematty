package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.*;

/**
 * User: Dedaulus
 * Date: 12.05.11
 * Time: 22:40
 */
public class ActivityState implements Cloneable {
    public static final int CINEMA_LIST           = 1;
    public static final int CINEMA_LIST_W_MOVIE   = 2;
    public static final int MOVIE_LIST            = 3;
    public static final int MOVIE_LIST_W_CINEMA   = 4;
    public static final int MOVIE_LIST_W_ACTOR    = 5;
    public static final int MOVIE_LIST_W_GENRE    = 6;
    public static final int ACTOR_LIST            = 7;
    public static final int ACTOR_LIST_W_MOVIE    = 8;
    public static final int GENRE_LIST            = 9;
    public static final int GENRE_LIST_W_MOVIE    = 10;
    public static final int MOVIE_INFO            = 11;
    public static final int MOVIE_INFO_W_SCHEDULE = 12;
    public static final int CINEMA_ON_MAP         = 13;
    public static final int VIEW_FRAME            = 14;
    public static final int MOVIE_LIST_W_DIRECTOR = 15;

    public int activityType;
    public Cinema cinema;
    public Movie movie;
    public MovieDirector director;
    public MovieActor actor;
    public MovieGenre genre;

    public ActivityState() {}

    public ActivityState(
            int activityType,
            Cinema cinema,
            Movie movie,
            MovieDirector director,
            MovieActor actor,
            MovieGenre genre) {
        this.activityType = activityType;
        this.cinema = cinema;
        this.movie = movie;
        this.director = director;
        this.actor = actor;
        this.genre = genre;
    }

    public ActivityState clone() {
        return new ActivityState(activityType, cinema, movie, director, actor, genre);
    }
}
