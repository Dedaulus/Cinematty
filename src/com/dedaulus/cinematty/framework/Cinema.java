package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.UniqueSortedList;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:58
 */
public class Cinema implements Comparable<Cinema> {
    private String mCaption;
    private String mAddress;
    private String mMetro;
    private String mPhone;
    private String mUrl;
    private Map<Movie, List<String>> mShowTimes = new HashMap<Movie, List<String>>();

    public Cinema(String caption) {
        mCaption = caption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setMetro(String metro) {
        mMetro = metro;
    }

    public String getMetro() {
        return mMetro;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getPlainPhone() {
        StringBuffer plain = new StringBuffer(mPhone.length());
        for (char c : mPhone.toCharArray()) {
            if (c != '(' && c!= ')' && c != ' ') {
                plain.append(c);
            }
        }

        return plain.toString();
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPlainUrl() {
        return "http://" + mUrl;
    }

    public void addShowTime(Movie movie, List<String> times) {
        mShowTimes.put(movie, times);
        movie.addCinema(this);
    }

    public Map<Movie, List<String>> getShowTimes() {
        return mShowTimes;
    }

    public UniqueSortedList<Movie> getMovies() {
        return new UniqueSortedList<Movie>(mShowTimes.keySet(), new Comparator<Movie>() {
            public int compare(Movie o1, Movie o2) {
                return o1.compareTo(o2);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cinema cinema = (Cinema) o;

        if (!mCaption.equals(cinema.mCaption)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mCaption.hashCode();
    }

    public int compareTo(Cinema o) {
        if (this == o) return 0;
        if (o == null) return 1;

        return mCaption.compareTo(o.mCaption);
    }
}
