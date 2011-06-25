package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 2:48
 */
public class MovieActor implements Comparable<MovieActor> {
    private String mActor;
    private UniqueSortedList<Movie> mMovies;
    private long mFavValue = 0;

    public MovieActor(String actor) {
        mActor = actor;
        mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
    }

    public String getActor() {
        return mActor;
    }

    public void addMovie(Movie movie) {
        mMovies.add(movie);
    }

    public UniqueSortedList<Movie> getMovies() {
        return mMovies;
    }

    public void setFavourite(boolean addToFavourite) {
        if (addToFavourite) {
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

        MovieActor that = (MovieActor) o;

        if (!mActor.equals(that.mActor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mActor.hashCode();
    }

    public int compareTo(MovieActor o) {
        if (this == o) return 0;
        if (o == null) return 1;

        return mActor.compareTo(o.mActor);
    }
}
