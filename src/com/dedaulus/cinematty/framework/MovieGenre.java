package com.dedaulus.cinematty.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 2:52
 */
public class MovieGenre implements Comparable<MovieGenre> {
    private String name;
    private Map<String, Movie> movies;
    
    {
        movies = new HashMap<String, Movie>();
    }

    public MovieGenre(String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieGenre other = (MovieGenre)o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(MovieGenre o) {
        return name.compareTo(o.name);
    }
}
