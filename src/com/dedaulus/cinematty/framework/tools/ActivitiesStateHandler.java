package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 16.09.11
 * Time: 17:47
 */
public class ActivitiesStateHandler extends DefaultHandler {
    private static final String DATA_TAG            = "data";
    private static final String DATA_DATE_ATTR      = "date";
    private static final String STATE_TAG           = "state";
    private static final String STATE_COOKIE_ATTR   = "cookie";
    private static final String STATE_TYPE_ATTR     = "type";
    private static final String STATE_CINEMA_ATTR   = "cinema";
    private static final String STATE_MOVIE_ATTR    = "movie";
    private static final String STATE_DIRECTOR_ATTR = "director";
    private static final String STATE_ACTOR_ATTR    = "actor";
    private static final String STATE_GENRE_ATTR    = "genre";

    private static final Object INVALID_FIELD = new Object();

    private Map<String, ActivityState> states;

    private Map<String, Cinema> cinemas;
    private Map<String, Movie> movies;
    private Map<String, MovieDirector> directors;
    private Map<String, MovieActor> actors;
    private Map<String, MovieGenre> genres;

    {
        states = ActivitiesStateRestorer.BLANK_STATES;
    }

    public ActivitiesStateHandler(
            Map<String, Cinema> cinemaList,
            Map<String, Movie> movieList,
            Map<String, MovieDirector> directorList,
            Map<String, MovieActor> actorList,
            Map<String, MovieGenre> genreList) {
        cinemas = cinemaList;
        movies = movieList;
        directors = directorList;
        actors = actorList;
        genres = genreList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(DATA_TAG)) {
            if (isDataActual(attributes.getValue(DATA_DATE_ATTR))) {
                states = new HashMap<String, ActivityState>();
            }
        } else if (states != ActivitiesStateRestorer.BLANK_STATES && qName.equalsIgnoreCase(STATE_TAG)) {
            ActivityState state = new ActivityState();

            state.activityType = Integer.parseInt(attributes.getValue(STATE_TYPE_ATTR));

            Object cinema = getActivityStateField(attributes, STATE_CINEMA_ATTR, cinemas);
            if (cinema == INVALID_FIELD) {
                states = ActivitiesStateRestorer.BLANK_STATES;
                return;
            }
            state.cinema = (Cinema)cinema;

            Object movie = getActivityStateField(attributes, STATE_MOVIE_ATTR, movies);
            if (movie == INVALID_FIELD) {
                states = ActivitiesStateRestorer.BLANK_STATES;
                return;
            }
            state.movie = (Movie)movie;

            Object director = getActivityStateField(attributes, STATE_DIRECTOR_ATTR, directors);
            if (director == INVALID_FIELD) {
                states = ActivitiesStateRestorer.BLANK_STATES;
                return;
            }
            state.director = (MovieDirector)director;

            Object actor = getActivityStateField(attributes, STATE_ACTOR_ATTR, actors);
            if (actor == INVALID_FIELD) {
                states = ActivitiesStateRestorer.BLANK_STATES;
                return;
            }
            state.actor = (MovieActor)actor;

            Object genre = getActivityStateField(attributes, STATE_GENRE_ATTR, genres);
            if (genre == INVALID_FIELD) {
                states = ActivitiesStateRestorer.BLANK_STATES;
                return;
            }
            state.genre = (MovieGenre)genre;

            states.put(attributes.getValue(STATE_COOKIE_ATTR), state);
        }
    }

    public Map<String, ActivityState> getStates() {
        return states;
    }

    private boolean isDataActual(String date) {
        List<Integer> list = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(date, ".");
        while (st.hasMoreTokens()) {
            list.add(Integer.parseInt(st.nextToken()));
        }

        Calendar got = Calendar.getInstance();
        got.set(Calendar.YEAR, list.get(0));
        got.set(Calendar.MONTH, list.get(1));
        got.set(Calendar.DAY_OF_MONTH, list.get(2));
        got.set(Calendar.HOUR_OF_DAY, list.get(3));
        got.set(Calendar.MINUTE, list.get(4));
        got.set(Calendar.SECOND, list.get(5));
        got.add(Calendar.HOUR_OF_DAY, 1);

        Calendar now = Calendar.getInstance();
        return now.before(got);
    }

    private <T> Object getActivityStateField(Attributes attributes, String attribute, Map<String, T> map) {
        String name = attributes.getValue(attribute);
        if (name != null) {
            T field = map.get(name);
            if (field != null) {
                return field;
            } else {
                return INVALID_FIELD;
            }
        }

        return null;
    }
}
