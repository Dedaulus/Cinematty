package com.dedaulus.cinematty.framework;

import android.util.Pair;
import com.dedaulus.cinematty.framework.tools.Coordinate;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:58
 */
public class Cinema implements Comparable<Cinema> {
    private static final List<Metro> BLANK_METROS                                        = new ArrayList<Metro>();
    private static final Map<String, Movie> BLANK_MOVIES_IN_DAY                          = new HashMap<String, Movie>();
    private static final Map<String, Pair<Movie, List<Calendar>>> BLANK_SHOWTIMES_IN_DAY = new HashMap<String, Pair<Movie, List<Calendar>>>();
    
    private String name;
    private String id;
    private Coordinate coordinate;
    private String address;
    private String into;
    private List<Metro> metros;
    private String phone;
    private String url;
    private long favValue;
    private Map<Integer, Map<String, Pair<Movie, List<Calendar>>>> showTimes;
    private Map<Integer, Map<String, Movie>> movies;

    {
        metros = BLANK_METROS;
        showTimes = new HashMap<Integer, Map<String, Pair<Movie, List<Calendar>>>>();
        movies = new HashMap<Integer, Map<String, Movie>>();
    }

    public Cinema(
            String name,
            String id,
            Coordinate coordinate,
            String address,
            String into,
            List<Metro> metros,
            String phone,
            String url) {
        this.name = name;
        this.id = id;
        this.coordinate = coordinate;
        this.address = address;
        this.into = into;
        
        if (metros != null) {
            this.metros = metros;
            for (Metro metro : metros) {
                metro.addCinema(this);
            }
        }
        
        this.phone = phone;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getAddress() {
        return address;
    }

    public String getInto() {
        return into;
    }

    public List<Metro> getMetros() {
        return metros;
    }

    public String getPhone() {
        return phone;
    }

    public String getPlainPhone() {
        if (phone != null) {
            StringBuilder plain = new StringBuilder(phone.length());
            for (char c : phone.toCharArray()) {
                if (c != '(' && c!= ')' && c != ' ') {
                    plain.append(c);
                }
            }

            return plain.toString();
        }

        return null;
    }

    public String getUrl() {
        return url;
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

    public void addShowTime(Movie movie, List<Calendar> times, int day) {
        if (!showTimes.containsKey(day)) {
            showTimes.put(day, new HashMap<String, Pair<Movie, List<Calendar>>>());
            movies.put(day, new HashMap<String, Movie>());
        }
        
        showTimes.get(day).put(movie.getName(), Pair.create(movie, times));
        movies.get(day).put(movie.getName(), movie);
        movie.addCinema(this, day);
    }

    public Map<String, Pair<Movie, List<Calendar>>> getShowTimes(int day) {
        if (showTimes.containsKey(day)) {
            return showTimes.get(day);
        } else {
            showTimes.put(day, BLANK_SHOWTIMES_IN_DAY);
            return BLANK_SHOWTIMES_IN_DAY;
        }
    }
    
    public Map<String, Movie> getMovies(int day) {
        if (movies.containsKey(day)) {
            return movies.get(day);
        } else {
            movies.put(day, BLANK_MOVIES_IN_DAY);
            return BLANK_MOVIES_IN_DAY;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cinema other = (Cinema)o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(Cinema o) {
        return name.compareTo(o.name);
    }
}
