package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 22:37
 */
public class ScheduleReceiver {
    private URL mXmlUrl;
    private String mXmlFile;
    private File mCacheDir;

    public ScheduleReceiver(CinemattyApplication app, String fileName) {
        try {
            mXmlUrl = new URL(app.getString(R.string.settings_url) + "/" + fileName);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        mXmlFile = fileName;
        mCacheDir = app.getCacheDir();
    }

    public void getSchedule(UniqueSortedList<Cinema> cinemas,
                            UniqueSortedList<Movie> movies,
                            UniqueSortedList<MovieActor> actors,
                            UniqueSortedList<MovieGenre> genres,
                            StringBuffer pictureFolder) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            InputStream is = getActualXmlStream();

            SAXParser parser = factory.newSAXParser();
            ScheduleHandler handler = new ScheduleHandler();
            parser.parse(is, handler);
            handler.getSchedule(cinemas, movies, actors, genres, pictureFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getActualXmlStream() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            InputStream is = getFileStream();

            if (is == null) {
                return dumpStream(mXmlUrl.openConnection().getInputStream());
            }

            SAXParser parser = factory.newSAXParser();
            ScheduleDateHandler handler = new ScheduleDateHandler();

            try {
                parser.parse(getFileStream(), handler);
            } catch (SAXException e) {
                return dumpStream(mXmlUrl.openConnection().getInputStream());
            }

            Calendar got = handler.getActualDate();
            Calendar now = Calendar.getInstance();

            if (now.before(got)) {
                return is;
            } else {
                return dumpStream(mXmlUrl.openConnection().getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getFileStream() {
        File file = new File(mCacheDir, mXmlFile);
        if (!file.exists()) return null;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        return is;
    }

    private InputStream dumpStream(InputStream is) {
        try {
            InputStream input = new BufferedInputStream(is);
            OutputStream output = new FileOutputStream(new File(mCacheDir, mXmlFile));

            byte data[] = new byte[1024];

            int count = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

        } catch(Exception e) {
        }

        return getFileStream();
    }
}
