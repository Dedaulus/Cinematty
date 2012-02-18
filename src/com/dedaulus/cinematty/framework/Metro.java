package com.dedaulus.cinematty.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 05.01.12
 * Time: 5:11
 */
public class Metro implements Comparable<Metro> {
    private String name;
    private int color;
    private Map<String, Cinema> cinemas;
    
    {
        cinemas = new HashMap<String, Cinema>();
    }

    public Metro(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void addCinema(Cinema cinema) {
        cinemas.put(cinema.getName(), cinema);
    }

    public Map<String, Cinema> getCinemas() {
        return cinemas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metro other = (Metro)o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(Metro o) {
        return name.compareTo(o.name);
    }
}
