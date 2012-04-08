package com.dedaulus.cinematty;

import com.dedaulus.cinematty.framework.*;
import com.dedaulus.cinematty.framework.tools.CinemaSortOrder;
import com.dedaulus.cinematty.framework.tools.MovieSortOrder;

import java.util.List;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 17.12.11
 * Time: 10:30
 */
public interface ApplicationSettings {
    void setCurrentDay(int day);

    int getCurrentDay();

    Map<String, Cinema> getCinemas();

    Map<String, Movie> getMovies();

    Map<String, MovieActor> getActors();

    Map<String, MovieGenre> getGenres();

    List<MoviePoster> getPosters();

    void saveFavouriteActors();

    Map<String, MovieActor> getFavouriteActors();

    void saveFavouriteCinemas();

    Map<String, Cinema> getFavouriteCinemas();

    void saveCinemaSortOrder(CinemaSortOrder sortOrder);

    CinemaSortOrder getCinemaSortOrder();

    void saveMovieSortOrder(MovieSortOrder sortOrder);

    MovieSortOrder getMovieSortOrder();

    void saveMovieWithScheduleSortOrder(MovieSortOrder sortOrder);

    MovieSortOrder getMovieWithScheduleSortOrder();

    void saveCurrentCity(City city);

    City getCurrentCity();
}
