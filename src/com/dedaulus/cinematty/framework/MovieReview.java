package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 13.12.11
 * Time: 19:42
 */
public class MovieReview {
    private String name;
    private Movie movie;
    private String url;
    private String text;
    
    public MovieReview(String name, Movie movie, String url, String text) {
        this.movie = movie;
        this.name = name;
        this.url = url;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }
}
