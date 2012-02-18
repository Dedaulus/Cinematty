package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:27
 */
public class City implements Comparable<City> {
    private String name;
    private int id;
    private String fileName;
    private String zipFileName;

    public City(String name, int id, String fileName) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        zipFileName = fileName + ".gz";
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City other = (City)o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int compareTo(City city) {
        if (id > city.id) return 1;
        else if (id < city.id) return -1;
        else return 0;

    }
}
