package com.dedaulus.cinematty.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 2:48
 */
public class MovieActor implements Comparable<MovieActor> {
    private String name;
    private Map<String, Movie> movies;
    private long favValue = 0;
    
    {
        movies = new HashMap<String, Movie>();
    }

    public MovieActor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addMovie(Movie movie) {
        movies.put(movie.getName(), movie);
    }

    public Map<String, Movie> getMovies() {
        return movies;
    }

    public void setFavourite(boolean isFavourite) {
        if (isFavourite) {
            favValue = System.currentTimeMillis();
        } else {
            favValue = 0;
        }
    }

    public void setFavourite(long favValue) {
        this.favValue = favValue;
    }

    public long getFavourite() {
        return favValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieActor other = (MovieActor)o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(MovieActor o) {
        if (favValue == o.favValue) {
            return name.compareTo(o.name);
        }
        return (favValue < o.favValue) ? 1 : -1;
    }
}
