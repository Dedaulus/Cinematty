package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 29.08.11
 * Time: 0:22
 */
public class MoviePoster {
    private Movie movie;
    private String posterPath;

    public MoviePoster(Movie movie, String posterPath) {
        this.movie = movie;
        this.posterPath = posterPath;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getPosterPath() {
        return posterPath;
    }
}
