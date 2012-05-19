package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 29.08.11
 * Time: 0:22
 */
public class MoviePoster {
    private Movie movie;
    private String picId;
    private String trailerUrl;

    public MoviePoster(Movie movie, String picId, String trailerUrl) {
        this.movie = movie;
        this.picId = picId;
        this.trailerUrl = trailerUrl;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getPicId() {
        return picId;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }
}
