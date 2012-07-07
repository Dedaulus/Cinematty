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
    private static final String CINEMA_METRO_ATTR         = "metro2";
    private static final String CINEMA_PHONE_ATTR         = "phone";
    private static final String CINEMA_URL_ATTR           = "www";

    private static final String MOVIE_TAG                 = "movie";
    private static final String MOVIE_TITLE_ATTR          = "title";
    private static final String MOVIE_ID_ATTR             = "id";
    private static final String MOVIE_PICID_ATTR          = "picid";
    private static final String MOVIE_DIRECTORS_ATTR      = "director";
    private static final String MOVIE_COUNTRIES_ATTR      = "countries";
    private static final String MOVIE_YEAR_ATTR           = "year";
    private static final String MOVIE_LENGTH_ATTR         = "length";
    private static final String MOVIE_TYPE_ATTR           = "type";
    private static final String MOVIE_IMDB_ATTR           = "imdb";
    private static final String MOVIE_KP_ATTR             = "kp";
    private static final String MOVIE_ACTORS_TAG          = "actors";
    private static final String MOVIE_DESCRIPTION_TAG     = "description";
    private static final String MOVIE_REVIEWS_TAG         = "reviews";
    private static final String MOVIE_REVIEW_TAG          = "review";
    private static final String MOVIE_REVIEW_TITLE_ATTR   = "title";
    private static final String MOVIE_REVIEW_URL_ATTR     = "url";
    private static final String MOVIE_FRAMES_TAG          = "frames";
    private static final String MOVIE_FRAMES_ID_ATTR      = "id";
    private static final String MOVIE_FRAME_TAG           = "frame";
    private static final String MOVIE_FRAME_ID_ATTR       = "id";

    private static final String SHOWTIME_TAG              = "showtime";
    private static final String SHOWTIME_THEATER_ID_ATTR  = "theater_id";
    private static final String SHOWTIME_MOVIE_ID_ATTR    = "movie_id";
    private static final String SHOWTIME_DAY_ATTR         = "day";
    private static final String SHOWTIME_TIMES_ATTR       = "times";

    private static final String POSTERS_TAG               = "posters";
    private static final String POSTERS_LIVE_TIME_ATTR    = "live_time";
    private static final String POSTERS_DATE_ATTR         = "date";
    private static final String POSTER_TAG                = "poster";
    private static final String POSTER_PICID_ATTR         = "picid";
    private static final String POSTER_TRAILER_ATTR       = "youtube";
    private static final String POSTER_MOVIE_ATTR         = "movie";

    private static final String ACTORS_BUG_SUFFIX = " - ;";

    private static class CinemaData {
        String name;
        String id;
        Coordinate coordinate;
        String address;
        String into;
        List<Metro> metros;
        String phone;
        String url;
        
        Cinema createCinema() {
            return new Cinema(name, id, coordinate, address, into, metros, phone, url);
        }
    }
    
    private static class MovieData {
        String name;
        String id;
        String picId;
        MovieFrameIdsStore frameIdsStore;
        int length;
        int year;
        List<String> countries;
        List<String> directors;
        String description;
        Map<String, MovieActor> actors;
        Map<String, MovieGenre> genres;
        float imdb;
        float kp;

        public Movie createMovie() {
            return new Movie(name, id, picId, frameIdsStore, length, year, countries, directors, description, actors, genres, imdb, kp);
        }
    }

    private static class MovieReviewData {
        String name;
        Movie movie;
        String url;
        String text;

        MovieReview createReview() {
            return new MovieReview(name, movie, url, text);
        }
    }

    private Map<String, Cinema> cinemaIds;
    private Map<String, Movie> movieIds;
    private Map<String, MovieActor> actors;
    private Map<String, MovieGenre> genres;
    private List<MoviePoster> posters;
    private Map<String, Metro> metros;
    private boolean isPostersUpToDate;
    private StringBuilder buffer;
    private CinemaData currentCinemaData;
    private MovieData currentMovieData;
    private MovieReviewData currentReviewData;
    private List<MovieReviewData> currentReviewsData;

    public void getSchedule(
            Map<String, Cinema> cinemas,
            Map<String, Movie> movies,
            Map<String, MovieActor> actors,
            Map<String, MovieGenre> genres,
            List<MoviePoster> posters) {
        cinemas.clear();
        movies.clear();
        actors.clear();
        genres.clear();
        posters.clear();
        
        for (Cinema cinema : cinemaIds.values()) {
            cinemas.put(cinema.getName(), cinema);
        }
        
        for (Movie movie : movieIds.values()) {
            movies.put(movie.getName(), movie);
        }

        actors.putAll(this.actors);
        genres.putAll(this.genres);
        posters.addAll(this.posters);
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        cinemaIds = new HashMap<String, Cinema>();
        movieIds = new HashMap<String, Movie>();
        actors = new HashMap<String, MovieActor>();
        genres = new HashMap<String, MovieGenre>();
        posters = new ArrayList<MoviePoster>();
        metros = new HashMap<String, Metro>();
        isPostersUpToDate = false;
        buffer = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(CINEMA_TAG)) {
            currentCinemaData = new CinemaData();

            currentCinemaData.name = attributes.getValue(CINEMA_TITLE_ATTR);

            currentCinemaData.id = attributes.getValue(CINEMA_ID_ATTR);

            String latitude = attributes.getValue(CINEMA_LATITUDE_ATTR);
            String longitude = attributes.getValue(CINEMA_LONGITUDE_ATTR);
            if (latitude != null && longitude != null) {
                currentCinemaData.coordinate = new Coordinate(parseLatitude(latitude), parseLongitude(longitude));
            }

            currentCinemaData.address = attributes.getValue(CINEMA_ADDRESS_ATTR);

            currentCinemaData.into = attributes.getValue(CINEMA_INTO_ATTR);

            currentCinemaData.metros = parseMetros(attributes.getValue(CINEMA_METRO_ATTR));

            currentCinemaData.phone = attributes.getValue(CINEMA_PHONE_ATTR);

            currentCinemaData.url = attributes.getValue(CINEMA_URL_ATTR);
        } else if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            currentMovieData = new MovieData();

            currentMovieData.name = attributes.getValue(MOVIE_TITLE_ATTR);

            currentMovieData.id = attributes.getValue(MOVIE_ID_ATTR);

            currentMovieData.picId = attributes.getValue(MOVIE_PICID_ATTR);

            currentMovieData.length = parseLength(attributes.getValue(MOVIE_LENGTH_ATTR));
            
            String yearStr = attributes.getValue(MOVIE_YEAR_ATTR);
            if (yearStr != null && yearStr.length() != 0) {
                currentMovieData.year = Integer.parseInt(yearStr.trim());
            }

            currentMovieData.countries = parseStrings(attributes.getValue(MOVIE_COUNTRIES_ATTR), ";");

            currentMovieData.directors = parseStrings(attributes.getValue(MOVIE_DIRECTORS_ATTR), ";");

            currentMovieData.genres = parseGenres(attributes.getValue(MOVIE_TYPE_ATTR));

            String imdbStr = attributes.getValue(MOVIE_IMDB_ATTR);
            if (imdbStr != null && imdbStr.length() != 0) {
                currentMovieData.imdb = Float.parseFloat(imdbStr);
            }

            String kpStr = attributes.getValue(MOVIE_KP_ATTR);
            if (kpStr != null && kpStr.length() != 0) {
                currentMovieData.kp = Float.parseFloat(kpStr);
            }
        } else if (qName.equalsIgnoreCase(MOVIE_FRAMES_TAG)) {
            currentMovieData.frameIdsStore = new MovieFrameIdsStore(attributes.getValue(MOVIE_FRAMES_ID_ATTR));
        } else if (qName.equalsIgnoreCase(MOVIE_FRAME_TAG)) {
            currentMovieData.frameIdsStore.addFrameId(Integer.parseInt(attributes.getValue(MOVIE_FRAME_ID_ATTR)));
        } else if (qName.equalsIgnoreCase(MOVIE_REVIEWS_TAG)) {
            currentReviewsData = new ArrayList<MovieReviewData>();
        } else if (qName.equalsIgnoreCase(MOVIE_REVIEW_TAG)) {
            currentReviewData = new MovieReviewData();

            currentReviewData.name = attributes.getValue(MOVIE_REVIEW_TITLE_ATTR);

            currentReviewData.url = attributes.getValue(MOVIE_REVIEW_URL_ATTR);

            currentReviewsData.add(currentReviewData);
        } else if (qName.equalsIgnoreCase(SHOWTIME_TAG)) {
            Cinema cinema = cinemaIds.get(attributes.getValue(SHOWTIME_THEATER_ID_ATTR));
            Movie movie = movieIds.get(attributes.getValue(SHOWTIME_MOVIE_ID_ATTR));
            int currentDay = Integer.parseInt(attributes.getValue(SHOWTIME_DAY_ATTR));
            String timesStr = attributes.getValue(SHOWTIME_TIMES_ATTR);
            cinema.addShowTime(movie, parseTimes(timesStr, currentDay), currentDay);
        } else if (qName.equalsIgnoreCase(POSTERS_TAG)) {
            isPostersUpToDate = isPostersUpToDate(attributes.getValue(POSTERS_DATE_ATTR), attributes.getValue(POSTERS_LIVE_TIME_ATTR));
        } else if (qName.equalsIgnoreCase(POSTER_TAG) && isPostersUpToDate) {
            String name = attributes.getValue(POSTER_MOVIE_ATTR);
            String name3d = name + " 3d";
            for (Movie movie : movieIds.values()) {
                if (movie.getName().equalsIgnoreCase(name) || movie.getName().equalsIgnoreCase(name3d)) {
                    String picId = attributes.getValue(POSTER_PICID_ATTR);
                    if (picId == null || picId.length() == 0) continue;

                    String trailerUrl = attributes.getValue(POSTER_TRAILER_ATTR);
                    if (trailerUrl == null) trailerUrl = "";
                    posters.add(new MoviePoster(
                            movie,
                            attributes.getValue(POSTER_PICID_ATTR),
                            trailerUrl));
                    break;
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equalsIgnoreCase(CINEMA_TAG)) {
            cinemaIds.put(currentCinemaData.id, currentCinemaData.createCinema());
        } else if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            Movie movie = currentMovieData.createMovie();
            if (currentReviewsData != null && currentReviewsData.size() != 0) {
                for (MovieReviewData reviewData : currentReviewsData) {
                    reviewData.movie = movie;
                    movie.addReview(reviewData.createReview());
                }

                currentReviewsData = null;
            }
            movieIds.put(currentMovieData.id, currentMovieData.createMovie());
        } else if (qName.equalsIgnoreCase(MOVIE_DESCRIPTION_TAG) && buffer.length() != 0) {
            currentMovieData.description = buffer.toString();
        } else if (qName.equalsIgnoreCase(MOVIE_ACTORS_TAG) && buffer.length() != 0) {
            currentMovieData.actors = parseActors(buffer.toString());
        } else if (qName.equalsIgnoreCase(MOVIE_REVIEW_TAG) && buffer.length() != 0) {
            currentReviewData.text = buffer.toString();
        }

        buffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        buffer.append(ch, start, length);
    }
    
    private List<String> parseStrings(String str, String delimiter) {
        if (str == null || str.length() == 0 || delimiter == null) return null;

        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        
        return list;
    }

    private List<Metro> parseMetros(String metrosStr) {
        if (metrosStr == null || metrosStr.length() == 0) return null;

        List<Metro> metros = new ArrayList<Metro>();
        StringTokenizer st = new StringTokenizer(metrosStr, ";");
        while (st.hasMoreTokens()) {
            StringTokenizer nameAndColor = new StringTokenizer(st.nextToken(), ":");
            if (nameAndColor.hasMoreTokens()) {
                String name = nameAndColor.nextToken();
                Metro metro = this.metros.get(name);
                if (metro == null) {
                    metro = new Metro(name, (int)Long.parseLong(nameAndColor.nextToken().toUpperCase(), 16));
                    this.metros.put(name, metro);
                }
                metros.add(metro);
            }
        }

        return metros;
    }

    private Map<String, MovieGenre> parseGenres(String genresStr) {
        if (genresStr == null || genresStr.length() == 0) return null;
        
        Map<String, MovieGenre> genres = new HashMap<String, MovieGenre>();
        StringTokenizer st = new StringTokenizer(genresStr, "/");
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            MovieGenre genre = this.genres.get(name);
            if (genre == null) {
                genre = new MovieGenre(name);
                this.genres.put(name, genre);
            }
            genres.put(name, genre);
        }

        return genres;
    }

    private Map<String, MovieActor> parseActors(String actorsStr) {
        if (actorsStr == null || actorsStr.length() == 0) return null;

        if (actorsStr.endsWith(ACTORS_BUG_SUFFIX)) {
            actorsStr = actorsStr.substring(0, actorsStr.length() - ACTORS_BUG_SUFFIX.length());
        }
        
        Map<String, MovieActor> actors = new HashMap<String, MovieActor>();
        StringTokenizer st = new StringTokenizer(actorsStr, ";");
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            MovieActor actor = this.actors.get(name);
            if (actor == null) {
                actor = new MovieActor(name);
                this.actors.put(name, actor);
            }
            actors.put(name, actor);
        }

        return actors;
    }

    private int parseLength(String timeStr) {
        if (timeStr == null || timeStr.length() == 0) return 0;

        if (timeStr.contains(":")) {
            StringTokenizer st = new StringTokenizer(timeStr, ":");
            return Integer.parseInt(st.nextToken()) * 60 + Integer.parseInt(st.nextToken());
        } else {
            return Integer.parseInt(timeStr);
        }
    }

    private List<Calendar> parseTimes(String timesStr, int day) {
        List<Calendar> list = new UniqueSortedList<Calendar>(new DefaultComparator<Calendar>());
        StringTokenizer st = new StringTokenizer(timesStr, ";");

        while (st.hasMoreTokens()) {
            StringTokenizer hoursAndMinutes = new StringTokenizer(st.nextToken(), ":");

            if (hoursAndMinutes.hasMoreTokens()) {
                int hours = Integer.parseInt(hoursAndMinutes.nextToken());
                int minutes = Integer.parseInt(hoursAndMinutes.nextToken());

                Calendar now = Calendar.getInstance();
                int hourNow = now.get(Calendar.HOUR_OF_DAY);

                now.set(Calendar.HOUR_OF_DAY, hours);
                now.set(Calendar.MINUTE, minutes);

                if (hourNow < Constants.LAST_SHOWTIME_HOUR) {
                    now.add(Calendar.DAY_OF_MONTH, -1);
                }

                if (hours < Constants.LAST_SHOWTIME_HOUR) { /* holly fuck!*/
                    now.add(Calendar.DAY_OF_MONTH, 1);
                }

                now.add(Calendar.DAY_OF_MONTH, day);

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
        if (dateStr == null || dateStr.length() == 0 || liveTimeStr == null || liveTimeStr.length() == 0) return false;

        int liveDays = Integer.parseInt(liveTimeStr);
        List<Integer> list = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(dateStr, ".");

        while (st.hasMoreTokens()) {
            list.add(Integer.parseInt(st.nextToken()));
        }

        Calendar date = Calendar.getInstance();

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
