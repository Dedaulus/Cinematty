package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 2:52
 */
public class MovieGenre implements Comparable<MovieGenre> {
    private String mGenre;
    private UniqueSortedList<Movie> mMovies;

    public MovieGenre(String genre) {
        mGenre = genre;
        mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
    }

    public String getGenre() {
        return mGenre;
    }

    public void addMovie(Movie movie) {
        mMovies.add(movie);
    }

    public UniqueSortedList<Movie> getMovies() {
        return mMovies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieGenre that = (MovieGenre) o;

        return mGenre.equals(that.mGenre);
    }

    @Override
    public int hashCode() {
        return mGenre.hashCode();
    }

    public int compareTo(MovieGenre o) {
        return mGenre.compareTo(o.mGenre);
    }
}
