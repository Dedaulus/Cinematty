package com.dedaulus.cinematty.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.*;
import com.dedaulus.cinematty.activities.adapters.SearchAdapter;
import com.dedaulus.cinematty.framework.*;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;
import com.dedaulus.cinematty.framework.tools.LocationClient;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 25.02.12
 * Time: 21:33
 */
public class SearchableActivity extends SherlockActivity implements LocationClient {
    CinemattyApplication app;
    ApplicationSettings settings;
    ActivitiesState activitiesState;
    LocationState locationState;
    SearchAdapter searchAdapter;

    Map<String, Metro> metros;

    {
        metros = new TreeMap<String, Metro>();
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
        locationState = app.getLocationState();

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();

        inflater.inflate(R.menu.preferences_menu, menu);

        inflater.inflate(R.menu.problem_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                app.goHome(this);
                return true;

            case R.id.menu_preferences:
                app.showPreferences(this);
                return true;

            case R.id.menu_problem:
                app.showProblemResolver(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        locationState.removeLocationClient(this);
        locationState.stopLocationListening();
        settings.saveFavouriteCinemas();
        settings.saveFavouriteDirectors();
        settings.saveFavouriteActors();
        super.onPause();
    }

    @Override
    protected void onResume() {
        locationState.startLocationListening();
        locationState.addLocationClient(this);
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            processSearch(intent);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            processSuggest(intent);
        }
    }
    
    private void processSearch(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        String pattern = new StringBuilder().append("(?i).* [\"«„”‘(]*").append(query).append(".*|(?i)^[\"«„”‘]*").append(query).append(".*").append("|(?i).*[-(/]+").append(query).append(".*").toString();

        List<Cinema> foundCinemas = new ArrayList<Cinema>();
        Map<String, Cinema> cinemas = settings.getCinemas();
        for (Cinema cinema : cinemas.values()) {
            for (Metro metro : cinema.getMetros()) {
                metros.put(metro.getName(), metro);
            }
            if (cinema.getName().matches(pattern)) {
                foundCinemas.add(cinema);
            }
        }
        Collections.sort(foundCinemas);

        List<Movie> foundMovies = new ArrayList<Movie>();
        Map<String, Movie> movies = settings.getMovies();
        for (String caption : movies.keySet()) {
            if (caption.matches(pattern)) {
                foundMovies.add(movies.get(caption));
            }
        }
        Collections.sort(foundMovies);

        List<MovieDirector> foundDirectors = new ArrayList<MovieDirector>();
        Map<String, MovieDirector> directors = settings.getDirectors();
        for (String caption : directors.keySet()) {
            if (caption.matches(pattern)) {
                foundDirectors.add(directors.get(caption));
            }
        }
        Collections.sort(foundDirectors);

        List<MovieActor> foundActors = new ArrayList<MovieActor>();
        Map<String, MovieActor> actors = settings.getActors();
        for (String caption : actors.keySet()) {
            if (caption.matches(pattern)) {
                foundActors.add(actors.get(caption));
            }
        }
        Collections.sort(foundActors);

        List<Metro> foundMetros = new ArrayList<Metro>();
        for (Metro metro : metros.values()) {
            if (metro.getName().matches(pattern)) {
                foundMetros.add(metro);
            }
        }
        
        if (foundCinemas.isEmpty()
                && foundMovies.isEmpty()
                && foundDirectors.isEmpty()
                && foundActors.isEmpty()
                && foundMetros.isEmpty()) {
            findViewById(R.id.empty_search).setVisibility(View.VISIBLE);
            return;
        }

        IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
        searchAdapter = new SearchAdapter(
                this,
                notifier,
                foundCinemas,
                locationState.getCurrentLocation(),
                foundMovies,
                app.getImageRetrievers().getMovieSmallImageRetriever(),
                foundDirectors,
                foundActors,
                foundMetros);
        ListView list = (ListView)findViewById(R.id.search_list);
        list.setAdapter(searchAdapter);
        list.setOnScrollListener(notifier);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchAdapter adapter = (SearchAdapter)parent.getAdapter();
                switch (adapter.getItemViewType(position)) {
                    case Constants.CINEMA_TYPE_ID:
                        Cinema cinema = (Cinema)adapter.getItem(position);
                        showCinema(cinema);
                        break;
                    
                    case Constants.MOVIE_TYPE_ID:
                        Movie movie = (Movie)adapter.getItem(position);
                        showMovie(movie);
                        break;

                    case Constants.DIRECTOR_TYPE_ID:
                        MovieDirector director = (MovieDirector)adapter.getItem(position);
                        showDirector(director);
                        break;

                    case Constants.ACTOR_TYPE_ID:
                        MovieActor actor = (MovieActor)adapter.getItem(position);
                        showActor(actor);
                        break;

                    case Constants.METRO_TYPE_ID:
                        Metro metro = (Metro)adapter.getItem(position);
                        showMetro(metro);
                        break;
                }
            }
        });
    }

    private void processSuggest(Intent intent) {
        String caption = intent.getDataString();
        int searchableId = Integer.parseInt(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        boolean succeeded = false;
        switch (searchableId) {
            case Constants.CINEMA_TYPE_ID:
                succeeded = showCinema(caption);
                break;
            
            case Constants.MOVIE_TYPE_ID:
                succeeded = showMovie(caption);
                break;

            case Constants.DIRECTOR_TYPE_ID:
                succeeded = showDirector(caption);
                break;
            
            case Constants.ACTOR_TYPE_ID:
                succeeded = showActor(caption);
                break;

            case Constants.METRO_TYPE_ID:
                for (Cinema cinema : settings.getCinemas().values()) {
                    for (Metro metro : cinema.getMetros()) {
                        metros.put(metro.getName(), metro);
                    }
                }
                succeeded = showMetro(caption);
                break;
        }
        
        if (!succeeded) {
            processSearch(intent);
        } else {
            finish();
        }
    }

    private boolean showCinema(Cinema cinema) {
        if (cinema != null) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_LIST_W_CINEMA,
                    cinema,
                    null,
                    null,
                    null,
                    null,
                    null);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(this, CinemaActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_DESCRIPTION_PAGE_ID);
            startActivity(intent);

            return true;
        }

        return false;
    }
    
    private boolean showCinema(String caption) {
        return showCinema(settings.getCinemas().get(caption));
    }

    private boolean showMovie(Movie movie) {
        if (movie != null) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_INFO,
                    null,
                    movie,
                    null,
                    null,
                    null,
                    null);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(this,MovieActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);

            return true;
        }

        return false;
    }

    private boolean showMovie(String caption) {
        return showMovie(settings.getMovies().get(caption));
    }

    private boolean showDirector(MovieDirector director) {
        if (director != null) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_LIST_W_DIRECTOR,
                    null,
                    null,
                    director,
                    null,
                    null,
                    null);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);

            return true;
        }

        return false;
    }

    private boolean showDirector(String caption) {
        return showDirector(settings.getDirectors().get(caption));
    }

    private boolean showActor(MovieActor actor) {
        if (actor != null) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(
                    ActivityState.MOVIE_LIST_W_ACTOR,
                    null,
                    null,
                    null,
                    actor,
                    null,
                    null);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(this, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);

            return true;
        }

        return false;
    }

    private boolean showActor(String caption) {
        return showActor(settings.getActors().get(caption));
    }

    private boolean showMetro(Metro metro) {
        if (metro != null) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(
                    ActivityState.CINEMA_LIST_W_METRO,
                    null,
                    null,
                    null,
                    null,
                    null,
                    metro);
            activitiesState.setState(cookie, state);

            Intent intent = new Intent(this, CinemaListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);

            return true;
        }

        return false;
    }

    private boolean showMetro(String caption) {
        return showMetro(metros.get(caption));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (searchAdapter != null) {
            searchAdapter.setLocation(location);
        }
    }
}
