package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.*;
import java.util.List;
import java.util.NavigableSet;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 22:37
 */
public class ScheduleReceiver {
    private URL mXmlUrl;

    public ScheduleReceiver(String url) {
        try {
            mXmlUrl = new URL(url);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getSchedule(UniqueSortedList<Cinema> cinemas,
                            UniqueSortedList<Movie> movies,
                            UniqueSortedList<MovieActor> actors,
                            UniqueSortedList<MovieGenre> genres) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ScheduleHandler handler = new ScheduleHandler();
            parser.parse(mXmlUrl.openConnection().getInputStream(), handler);
            handler.getSchedule(cinemas, movies, actors, genres);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
