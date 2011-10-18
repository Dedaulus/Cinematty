package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.DefaultComparator;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:58
 */
public class Movie implements Comparable<Movie> {
    private String mId;
    private String mCaption;
    private String mPicId = null;
    private String mDescription = "";
    private int mLengthInMinutes;
    private float mImdb = -1;
    private List<MovieGenre> mGenres = new ArrayList<MovieGenre>();
    private List<MovieActor> mActors = new ArrayList<MovieActor>();
    private Map<Integer, UniqueSortedList<Cinema>> mCinemas = new HashMap<Integer, UniqueSortedList<Cinema>>();

    public Movie(String caption) {
        mCaption = caption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
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

    public void setImdb(float imdb) {
        mImdb = imdb;
    }

    public float getImdb() {
        return mImdb;
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

    public void addCinema(Cinema cinema, int day) {
        if (mCinemas.get(day) == null) {
            mCinemas.put(day, new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>()));
        }

        mCinemas.get(day).add(cinema);
    }

    public UniqueSortedList<Cinema> getCinemas(int day) {
        return mCinemas.get(day);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        return mCaption.equals(movie.mCaption);
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
