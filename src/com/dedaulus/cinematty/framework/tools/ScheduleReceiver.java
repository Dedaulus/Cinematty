package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 22:37
 */
public class ScheduleReceiver {
    private URL mXmlUrl;
    private String mXmlFile;
    private File mCacheDir;

    public ScheduleReceiver(CinemattyApplication app, String fileName) throws MalformedURLException {
        mXmlUrl = new URL(app.getString(R.string.settings_url) + "/" + fileName);
        mXmlFile = fileName;
        mCacheDir = app.getCacheDir();
    }

    public boolean getSchedule(UniqueSortedList<Cinema> cinemas,
                            UniqueSortedList<Movie> movies,
                            UniqueSortedList<MovieActor> actors,
                            UniqueSortedList<MovieGenre> genres,
                            StringBuffer pictureFolder,
                            List<MoviePoster> posters,
                            boolean useLocalOnly) throws IOException, ParserConfigurationException, SAXException {
        InputStream is = useLocalOnly ? getFileStream() : getActualXmlStream();
        if (is == null) return false;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        ScheduleHandler handler = new ScheduleHandler();
        parser.parse(is, handler);
        handler.getSchedule(cinemas, movies, actors, genres, pictureFolder, posters);
        return true;
    }

    private InputStream getActualXmlStream() throws IOException, ParserConfigurationException, SAXException {
        InputStream is = getFileStream();
        if (is == null) {
            is = dumpStream(mXmlUrl.openConnection().getInputStream());
            if (isActualXmlStream(is)) {
                return getFileStream();
            } else {
                return null;
            }
        }

        boolean isActual = false;
        try {
            isActual = isActualXmlStream(is);
        } catch (Exception e) {}
        if (isActual) return getFileStream();

        is = dumpStream(mXmlUrl.openConnection().getInputStream());
        if (isActualXmlStream(is)) {
            return getFileStream();
        } else {
            return null;
        }
    }

    private boolean isActualXmlStream(InputStream is) throws IOException, ParserConfigurationException, SAXException {
        if (is == null) return false;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        ScheduleDateHandler handler = new ScheduleDateHandler();
        parser.parse(is, handler);

        Calendar got = handler.getActualDate();
        Calendar now = Calendar.getInstance();
        return now.before(got);
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

    private InputStream dumpStream(InputStream is) throws IOException {
        InputStream input = new BufferedInputStream(is);
        OutputStream output = new FileOutputStream(new File(mCacheDir, mXmlFile));

        byte data[] = new byte[1024];

        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();

        return getFileStream();
    }
}
