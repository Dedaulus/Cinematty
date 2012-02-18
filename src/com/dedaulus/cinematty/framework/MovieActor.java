package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

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
    private long mFavValue = 0;
    
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
            mFavValue = System.currentTimeMillis();
        } else {
            mFavValue = 0;
        }
    }

    public void setFavourite(long favValue) {
        mFavValue = favValue;
    }

    public long getFavourite() {
        return mFavValue;
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
        return name.compareTo(o.name);
    }
}
