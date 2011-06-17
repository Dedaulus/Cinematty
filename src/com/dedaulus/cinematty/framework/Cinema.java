package com.dedaulus.cinematty.framework;

import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:58
 */
public class Cinema implements Comparable<Cinema> {
    public class Coordinate {
        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double latitude;
        public double longitude;
    }

    private String mCaption;
    private Coordinate mCoordinate;
    private String mAddress;
    private String mInto;
    private String mMetro;
    private String mPhone;
    private String mUrl;
    private Map<Movie, List<Calendar>> mShowTimes = new HashMap<Movie, List<Calendar>>();
    private long mFavValue = 0;

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

    public void setCoordinate(double latitude, double longitude) {
        mCoordinate = new Coordinate(latitude, longitude);
    }

    public Coordinate getCoordinate() {
        return mCoordinate;
    }

    public void setInto(String into) {
        mInto = into;
    }

    public String getInto() {
        return mInto;
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
        if (mPhone != null) {
            StringBuffer plain = new StringBuffer(mPhone.length());
            for (char c : mPhone.toCharArray()) {
                if (c != '(' && c!= ')' && c != ' ') {
                    plain.append(c);
                }
            }

            return plain.toString();
        }

        return null;
    }

    public void setUrl(String url) {
        if (url != null) {
            if (url.startsWith("http://")) {
                mUrl = url;
            } else {
                mUrl = "http://" + url;
            }
        } else {
            mUrl = null;
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public void addShowTime(Movie movie, List<Calendar> times) {
        mShowTimes.put(movie, times);
        movie.addCinema(this);
    }

    public Map<Movie, List<Calendar>> getShowTimes() {
        return mShowTimes;
    }

    public UniqueSortedList<Movie> getMovies() {
        return new UniqueSortedList<Movie>(mShowTimes.keySet(), new Comparator<Movie>() {
            public int compare(Movie o1, Movie o2) {
                return o1.compareTo(o2);
            }
        });
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
