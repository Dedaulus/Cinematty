package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:58
 */
public class Movie implements Comparable<Movie> {
    private String mCaption;
    private String mPicId = null;
    private String mDescription = "";
    private int mLengthInMinutes;
    private List<MovieGenre> mGenres = new ArrayList<MovieGenre>();
    private List<MovieActor> mActors = new ArrayList<MovieActor>();
    private UniqueSortedList<Cinema> mCinemas;

    public Movie(String caption) {
        mCaption = caption;
        mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
    }

    public String getCaption() {
        return mCaption;
    }

    public void setPicId(String picId) {
        mPicId = picId;
    }

    public String getPicId() {
        return mPicId;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setLengthInMinutes(int minutes) {
        mLengthInMinutes = minutes;
    }

    public int getLengthInMinutes() {
        return mLengthInMinutes;
    }

    public void addActor(MovieActor actor) {
        mActors.add(actor);
        actor.addMovie(this);
    }

    public Collection<MovieActor> getActors() {
        return mActors;
    }

    public void addGenre(MovieGenre genre) {
        mGenres.add(genre);
        genre.addMovie(this);
    }

    public Collection<MovieGenre> getGenres() {
        return mGenres;
    }

    public void addCinema(Cinema cinema) {
        mCinemas.add(cinema);
    }

    public UniqueSortedList<Cinema> getCinemas() {
        return mCinemas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (!mCaption.equals(movie.mCaption)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mCaption.hashCode();
    }

    public int compareTo(Movie o) {
        if (this == o) return 0;
        if (o == null) return 1;

        return mCaption.compareTo(o.mCaption);
    }
}
