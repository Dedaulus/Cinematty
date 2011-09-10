package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 22:50
 */
public class ScheduleHandler extends DefaultHandler {
    private static final String CINEMA_TAG                = "theater";
    private static final String CINEMA_ID_ATTR            = "id";
    private static final String CINEMA_TITLE_ATTR         = "title";
    private static final String CINEMA_ADDRESS_ATTR       = "address";
    private static final String CINEMA_LATITUDE_ATTR      = "latitude";
    private static final String CINEMA_LONGITUDE_ATTR     = "longitude";
    private static final String CINEMA_INTO_ATTR          = "into";
    private static final String CINEMA_METRO_ATTR         = "metro";
    private static final String CINEMA_PHONE_ATTR         = "phone";
    private static final String CINEMA_URL_ATTR           = "www";

    private static final String MOVIE_TAG                 = "movie";
    private static final String MOVIE_TITLE_ATTR          = "title";
    private static final String MOVIE_PICID_ATTR          = "picid";
    private static final String MOVIE_LENGTH_ATTR         = "length";
    private static final String MOVIE_TYPE_ATTR           = "type";
    private static final String MOVIE_ID_ATTR             = "id";

    private static final String SHOWTIME_TAG              = "showtime";
    private static final String SHOWTIME_THEATER_ID_ATTR  = "theater_id";
    private static final String SHOWTIME_MOVIE_ID_ATTR    = "movie_id";

    private static final String PICTURES_FOLDER_TAG       = "pictures_folder";
    private static final String PICTURES_FOLDER_NAME_ATTR = "name";

    private static final String POSTERS_TAG               = "posters";
    private static final String POSTERS_LIVE_TIME_ATTR    = "live_time";
    private static final String POSTERS_DATE_ATTR         = "date";

    private static final String POSTER_TAG                = "poster";
    private static final String POSTER_IMAGE_ATTR         = "image";
    private static final String POSTER_MOVIE_ATTR         = "movie";

    private static final String ACTORS_BUG_SUFFIX = " - ;";

    private static final int LAST_SHOWTIME_HOUR = 6;

    private HashMap<String, Cinema> mCinemaIds;
    private HashMap<String, Movie> mMovieIds;
    private HashMap<String, MovieActor> mActors;
    private HashMap<String, MovieGenre> mGenres;
    private List<MoviePoster> mPosters;
    private boolean mIsPostersUpToDate;
    private String mPictureFolder;
    private StringBuilder mBuffer;
    private Cinema mCurrentCinema;
    private Movie mCurrentMovie;

    public void getSchedule(UniqueSortedList<Cinema> cinemas,
                            UniqueSortedList<Movie> movies,
                            UniqueSortedList<MovieActor> actors,
                            UniqueSortedList<MovieGenre> genres,
                            StringBuffer pictureFolder,
                            List<MoviePoster> posters) {
        cinemas.clear();
        movies.clear();
        actors.clear();
        genres.clear();
        pictureFolder.setLength(0);
        posters.clear();

        cinemas.addAll(mCinemaIds.values());
        movies.addAll(mMovieIds.values());
        actors.addAll(mActors.values());
        genres.addAll(mGenres.values());
        posters.addAll(mPosters);
        pictureFolder.append(mPictureFolder);
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        mCinemaIds = new HashMap<String, Cinema>();
        mMovieIds = new HashMap<String, Movie>();
        mActors = new HashMap<String, MovieActor>();
        mGenres = new HashMap<String, MovieGenre>();
        mPosters = new ArrayList<MoviePoster>();
        mIsPostersUpToDate = false;
        mBuffer = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(CINEMA_TAG)) {
            Cinema cinema = new Cinema(attributes.getValue(CINEMA_TITLE_ATTR));
            cinema.setAddress(attributes.getValue(CINEMA_ADDRESS_ATTR));
            cinema.setInto(attributes.getValue(CINEMA_INTO_ATTR));
            cinema.setMetro(attributes.getValue(CINEMA_METRO_ATTR));
            cinema.setPhone(attributes.getValue(CINEMA_PHONE_ATTR));
            cinema.setUrl(attributes.getValue(CINEMA_URL_ATTR));

            String latitude = attributes.getValue(CINEMA_LATITUDE_ATTR);
            String longitude = attributes.getValue(CINEMA_LONGITUDE_ATTR);
            if (latitude != null && longitude != null) {
                cinema.setCoordinate(new Coordinate(parseLatitude(latitude), parseLongitude(longitude)));
            }

            mCinemaIds.put(attributes.getValue(CINEMA_ID_ATTR), cinema);
        } else if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            Movie movie = new Movie(attributes.getValue(MOVIE_TITLE_ATTR));

            movie.setPicId(attributes.getValue(MOVIE_PICID_ATTR));

            movie.setLengthInMinutes(parseLength(attributes.getValue(MOVIE_LENGTH_ATTR)));

            // Get movie genres
            List<String> genres = parseGenres(attributes.getValue(MOVIE_TYPE_ATTR));
            for (String genre : genres) {
                MovieGenre movieGenre = mGenres.get(genre);
                if (movieGenre == null) {
                    movieGenre = new MovieGenre(genre);
                    movie.addGenre(movieGenre);
                    mGenres.put(genre, movieGenre);
                }
                else {
                    movie.addGenre(movieGenre);
                }
            }
            ///

            mMovieIds.put(attributes.getValue(MOVIE_ID_ATTR), movie);
            mCurrentMovie = movie;
        } else if (qName.equalsIgnoreCase(SHOWTIME_TAG)) {
            mCurrentCinema = mCinemaIds.get(attributes.getValue(SHOWTIME_THEATER_ID_ATTR));
            mCurrentMovie = mMovieIds.get(attributes.getValue(SHOWTIME_MOVIE_ID_ATTR));
        } else if (qName.equalsIgnoreCase(PICTURES_FOLDER_TAG)) {
            mPictureFolder = attributes.getValue(PICTURES_FOLDER_NAME_ATTR);
        } else if (qName.equalsIgnoreCase(POSTERS_TAG)) {
            mIsPostersUpToDate = isPostersUpToDate(attributes.getValue(POSTERS_DATE_ATTR), attributes.getValue(POSTERS_LIVE_TIME_ATTR));
        } else if (qName.equalsIgnoreCase(POSTER_TAG) && mIsPostersUpToDate) {
            String name = attributes.getValue(POSTER_MOVIE_ATTR);
            for (Movie movie : mMovieIds.values()) {
                if (movie.getCaption().equalsIgnoreCase(name)) {
                    mPosters.add(new MoviePoster(movie, attributes.getValue(POSTER_IMAGE_ATTR)));
                    break;
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equalsIgnoreCase("actors") && mBuffer.length() > 0) {
            List<String> actors = parseActors(mBuffer.toString());
            for (String actor : actors) {
                MovieActor movieActor = mActors.get(actor);
                if (movieActor == null) {
                    movieActor = new MovieActor(actor);
                    mCurrentMovie.addActor(movieActor);
                    mActors.put(actor, movieActor);
                } else {
                    mCurrentMovie.addActor(movieActor);
                }
            }
        }
        else if (qName.equalsIgnoreCase("description") && mBuffer.length() > 0) {
            mCurrentMovie.setDescription(mBuffer.toString());
        }
        else if (qName.equalsIgnoreCase(SHOWTIME_TAG) && mBuffer.length() > 0) {
            mCurrentCinema.addShowTime(mCurrentMovie, parseTimes(mBuffer.toString()));
        }

        mBuffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        mBuffer.append(ch, start, length);
    }

    private List<String> parseGenres(String genres) {
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(genres, "/");

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }

        return list;
    }

    private List<String> parseActors(String actors) {
        if (actors.endsWith(ACTORS_BUG_SUFFIX)) {
            actors = actors.substring(0, actors.length() - ACTORS_BUG_SUFFIX.length());
        }

        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(actors, ";");

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }

        return list;
    }

    private int parseLength(String time) {
        if (time == null || time.length() == 0) return 0;
        else if (time.contains(":")) {
            StringTokenizer st = new StringTokenizer(time, ":");
            return Integer.parseInt(st.nextToken()) * 60 + Integer.parseInt(st.nextToken());
        } else return Integer.parseInt(time);
    }

    private List<Calendar> parseTimes(String times) {
        List<Calendar> list = new UniqueSortedList<Calendar>(new DefaultComparator<Calendar>());
        StringTokenizer st = new StringTokenizer(times, ";");

        while (st.hasMoreTokens()) {
            StringTokenizer hoursAndMinutes = new StringTokenizer(st.nextToken(), ":");

            if (hoursAndMinutes.hasMoreTokens()) {
                int hours = Integer.parseInt(hoursAndMinutes.nextToken());
                int minutes = Integer.parseInt(hoursAndMinutes.nextToken());

                Calendar now = Calendar.getInstance();
                int hourNow = now.get(Calendar.HOUR_OF_DAY);

                now.set(Calendar.HOUR_OF_DAY, hours);
                now.set(Calendar.MINUTE, minutes);

                if (hourNow < LAST_SHOWTIME_HOUR) {
                    now.add(Calendar.DAY_OF_MONTH, -1);
                }

                if (hours < LAST_SHOWTIME_HOUR) { /* holly fuck!*/
                    now.add(Calendar.DAY_OF_MONTH, 1);
                }

                list.add(now);
            }
        }

        return list;
    }

    private double parseLatitude(String latitude) {
        return Double.parseDouble(latitude);
    }

    private double parseLongitude(String longitude) {
        return Double.parseDouble(longitude);
    }

    private boolean isPostersUpToDate(String dateStr, String liveTimeStr) {
        int liveDays = Integer.parseInt(liveTimeStr);
        List<Integer> list = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(dateStr, ".");

        while (st.hasMoreTokens()) {
            list.add(Integer.parseInt(st.nextToken()));
        }

        Calendar date = Calendar.getInstance();

        //mDate.set(Calendar.YEAR, list.get(0));
        date.set(Calendar.MONTH, list.get(1) - 1);
        date.set(Calendar.DAY_OF_MONTH, list.get(2));
        date.set(Calendar.HOUR_OF_DAY, 6);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.add(Calendar.DAY_OF_MONTH, liveDays);

        Calendar now = Calendar.getInstance();
        return now.before(date);
    }
}
