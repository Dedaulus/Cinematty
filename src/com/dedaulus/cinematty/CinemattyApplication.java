package com.dedaulus.cinematty;

import android.app.Application;
import android.content.SharedPreferences;
import com.dedaulus.cinematty.framework.*;
import com.dedaulus.cinematty.framework.tools.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

/*
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 21:41
 */
public class CinemattyApplication extends Application {
    private UniqueSortedList<Cinema> mCinemas;
    private UniqueSortedList<Movie> mMovies;
    private UniqueSortedList<MovieActor> mActors;
    private UniqueSortedList<MovieGenre> mGenres;

    private City mCurrentCity;

    private Stack<CurrentState> mState = new Stack<CurrentState>();

    private PictureRetriever mPictureRetriever = null;
    private static final String LOCAL_PICTURES_FOLDER = "pictures";

    private static final String FAV_CINEMAS_FILE = "cinematty_fav_cinemas";
    private static final String PREFERENCES_FILE = "cinematty_preferences";
    private static final String PREF_CINEMA_SORT_ORDER = "cinema_sort_order";
    private static final String PREF_CURRENT_CITY = "current_city";

    {
        mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
        mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
        mActors = new UniqueSortedList<MovieActor>(new DefaultComparator<MovieActor>());
        mGenres = new UniqueSortedList<MovieGenre>(new DefaultComparator<MovieGenre>());
    }

    public void retrieveData() throws IOException, ParserConfigurationException, SAXException {
        ScheduleReceiver receiver = new ScheduleReceiver(this, mCurrentCity.getFileName());
        StringBuffer pictureFolder = new StringBuffer();
        receiver.getSchedule(mCinemas, mMovies, mActors, mGenres, pictureFolder);

        String remotePictureFolder = getString(R.string.settings_url) + "/" + pictureFolder.toString();
        if (mPictureRetriever == null) {
            mPictureRetriever = new PictureRetriever(this, remotePictureFolder, LOCAL_PICTURES_FOLDER);
        } else {
            mPictureRetriever.setRemotePictureFolder(remotePictureFolder);
        }

        Map<String, ?> favs = getFavouriteCinemas();
        for (String caption : favs.keySet()) {
            int cinemaId = mCinemas.indexOf(new Cinema(caption));
            if (cinemaId != -1) {
                mCinemas.get(cinemaId).setFavourite(((Long)favs.get(caption)).longValue());
            }
        }
    }

    public boolean isUpToDate() {
        //return mCinemas.size() != 0;
        return false;
    }

    public UniqueSortedList<Cinema> getCinemas() {
        return mCinemas;
    }

    public UniqueSortedList<Movie> getMovies() {
        return mMovies;
    }

    public UniqueSortedList<MovieActor> getActors() {
        return mActors;
    }

    public UniqueSortedList<MovieGenre> getGenres() {
        return mGenres;
    }

    public PictureRetriever getPictureRetriever() {
        return mPictureRetriever;
    }

    public CurrentState getCurrentState() {
        return mState.peek();
    }

    public void setCurrentState(CurrentState currentState) {
        mState.add(currentState);
    }

    public void revertCurrentState() {
        mState.pop();
    }

    public void saveFavouriteCinemas() {
        SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (Cinema cinema : mCinemas) {
            if (cinema.getFavourite() > 0) {
                editor.putLong(cinema.getCaption(), cinema.getFavourite());
            } else {
                editor.remove(cinema.getCaption());
            }
        }

        editor.commit();
    }

    private Map<String, ?> getFavouriteCinemas() {
        SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
        return preferences.getAll();
    }

    public void saveCinemasSortOrder(CinemaSortOrder sortOrder) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_CINEMA_SORT_ORDER, sortOrder.ordinal());

        editor.commit();
    }

    public CinemaSortOrder getCinemaSortOrder() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        int order = preferences.getInt(PREF_CINEMA_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());

        for (CinemaSortOrder sortOrder : CinemaSortOrder.values()) {
            if (sortOrder.ordinal() == order) return sortOrder;
        }

        return CinemaSortOrder.BY_CAPTION;
    }

    public void saveCurrentCityId(int id) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_CURRENT_CITY, id);

        editor.commit();
    }

    public int getCurrentCityId() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        return preferences.getInt(PREF_CURRENT_CITY, 1);
    }

    public City getCurrentCity() {
        return mCurrentCity;
    }

    public void setCurrentCity(City city) {
        mCurrentCity = city;
    }
}
