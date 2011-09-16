package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.CinemattyApplication;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * User: Dedaulus
 * Date: 16.09.11
 * Time: 19:14
 */
public class StateReceiver {
    private String mXmlFile;
    private File mCacheDir;
    private CinemattyApplication mApp;

    public StateReceiver(CinemattyApplication app, String fileName) {
        mApp = app;
        mXmlFile = fileName;
        mCacheDir = app.getCacheDir();
    }

    public HashMap<String, ActivityState> getState() {
        InputStream is = getFileStream();
        if (is == null) return null;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            StateHandler handler = new StateHandler(mApp.getCinemas(), mApp.getMovies(), mApp.getActors(), mApp.getGenres());
            parser.parse(is, handler);
            return handler.getState();
        } catch (Exception e) {
            return null;
        }
    }

    private InputStream getFileStream() {
        File file = new File(mCacheDir, mXmlFile);
        if (!file.exists()) return null;

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        return is;
    }
}
