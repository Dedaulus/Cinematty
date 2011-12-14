package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:27
 */
public class City implements Comparable<City> {
    private int mId;
    private String mName;
    private String mFileName;

    public City(int id, String name, String fileName) {
        mId = id;
        mName = name;
        mFileName = fileName;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getFileName() {
        return mFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City city = (City) o;

        return mId == city.mId;
    }

    @Override
    public int hashCode() {
        return mId;
    }

    public int compareTo(City city) {
        if (mId > city.mId) return 1;
        else if (mId < city.mId) return -1;
        else return 0;

    }
}
