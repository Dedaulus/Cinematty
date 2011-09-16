package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 16.09.11
 * Time: 17:47
 */
public class StateHandler extends DefaultHandler {
    private static final String DATA_TAG          = "data";
    private static final String DATA_DATE_ATTR    = "date";
    private static final String STATE_TAG         = "state";
    private static final String STATE_COOKIE_ATTR = "cookie";
    private static final String STATE_TYPE_ATTR   = "type";
    private static final String STATE_CINEMA_ATTR = "cinema";
    private static final String STATE_MOVIE_ATTR  = "movie";
    private static final String STATE_ACTOR_ATTR  = "actor";
    private static final String STATE_GENRE_ATTR  = "genre";

    private HashMap<String, ActivityState> mState = new HashMap<String, ActivityState>();
    private List<Cinema> mCinemaList;
    private List<Movie> mMovieList;
    private List<MovieActor> mActorList;
    private List<MovieGenre> mGenreList;
    private boolean mIsDataActual = false;

    public StateHandler(List<Cinema> cinemaList, List<Movie> movieList, List<MovieActor> actorList, List<MovieGenre> genreList) {
        mCinemaList = cinemaList;
        mMovieList = movieList;
        mActorList = actorList;
        mGenreList = genreList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(DATA_TAG)) {
            mIsDataActual = isDataActual(attributes.getValue(DATA_DATE_ATTR));
        } else if (mIsDataActual && qName.equalsIgnoreCase(STATE_TAG)) {
            ActivityState state = new ActivityState(0, null, null, null, null);

            state.activityType = Integer.parseInt(attributes.getValue(STATE_TYPE_ATTR));

            String cinema = attributes.getValue(STATE_CINEMA_ATTR);
            if (cinema != null) {
                int cinemaId = mCinemaList.indexOf(new Cinema(cinema));
                if (cinemaId != -1) {
                    state.cinema = mCinemaList.get(cinemaId);
                } else mIsDataActual = false;
            }

            String movie = attributes.getValue(STATE_MOVIE_ATTR);
            if (movie != null) {
                int movieId = mMovieList.indexOf(new Movie(movie));
                if (movieId != -1) {
                    state.movie = mMovieList.get(movieId);
                } else mIsDataActual = false;
            }

            String actor = attributes.getValue(STATE_ACTOR_ATTR);
            if (actor != null) {
                int actorId = mActorList.indexOf(new MovieActor(actor));
                if (actorId != -1) {
                    state.actor = mActorList.get(actorId);
                } else mIsDataActual = false;
            }

            String genre = attributes.getValue(STATE_GENRE_ATTR);
            if (genre != null) {
                int genreId = mGenreList.indexOf(new MovieGenre(genre));
                if (genreId != -1) {
                    state.genre = mGenreList.get(genreId);
                } else mIsDataActual = false;
            }

            mState.put(attributes.getValue(STATE_COOKIE_ATTR), state);
        }
    }

    public HashMap<String, ActivityState> getState() {
        return mIsDataActual ? mState : null;
    }

    private boolean isDataActual(String dateStr) {
        List<Integer> list = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(dateStr, ".");
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
}
