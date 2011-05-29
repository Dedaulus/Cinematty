package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:27
 */
public class City {
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
}
