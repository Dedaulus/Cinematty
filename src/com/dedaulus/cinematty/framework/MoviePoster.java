package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 29.08.11
 * Time: 0:22
 */
public class MoviePoster {
    private Movie mMovie;
    private String mPosterPath;

    public MoviePoster(Movie movie, String posterPath) {
        mMovie = movie;
        mPosterPath = posterPath;
    }

    public Movie getMovie() {
        return mMovie;
    }

    public String getPosterPath() {
        return mPosterPath;
    }
}
