package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieActivity;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.MovieComparator;
import com.dedaulus.cinematty.framework.tools.MovieSortOrder;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:18
 */
public class MoviesPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private SortableAdapter<Movie> movieListAdapter;
    private boolean binded = false;

    public MoviesPage(Context context, CinemattyApplication app) {
        this.context = context;
        this.app = app;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.movie_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.movies_caption);
    }

    public void onResume() {
        if (binded) {
            ((StoppableAndResumable) movieListAdapter).onResume();
            movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), settings.getCurrentDay()));
        }
    }

    public void onPause() {}

    public void onStop() {
        ((StoppableAndResumable) movieListAdapter).onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((Activity) context).getMenuInflater();
        inflater.inflate(R.menu.movie_sort_menu, menu);

        switch (settings.getMovieSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
            break;

        case BY_POPULAR:
            menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
            break;

        case BY_RATING:
            menu.findItem(R.id.submenu_movie_sort_by_rating).setChecked(true);
            break;

        default:
            break;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_movie_sort:
            return true;

        case R.id.submenu_movie_sort_by_caption:
            movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION, settings.getCurrentDay()));
            settings.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
            item.setChecked(true);
            return true;

        case R.id.submenu_movie_sort_by_popular:
            movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, settings.getCurrentDay()));
            settings.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
            item.setChecked(true);
            return true;

        case R.id.submenu_movie_sort_by_rating:
            movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_RATING, settings.getCurrentDay()));
            settings.saveMovieSortOrder(MovieSortOrder.BY_RATING);
            item.setChecked(true);
            return true;

        default:
            return true;
        }
    }

    private View bindView(View view) {
        ListView list = (ListView)view.findViewById(R.id.movie_list);
        movieListAdapter = new MovieItemAdapter(context, new ArrayList<Movie>(settings.getMovies().values()), app.getImageRetrievers().getMovieSmallImageRetriever());
        list.setAdapter(movieListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(adapterView, view, i, l);
            }
        });
        binded = true;
        onResume();

        return view;
    }

    private void onMovieItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        MovieItemAdapter adapter = (MovieItemAdapter)adapterView.getAdapter();
        ListView list = (ListView)view.getParent();
        Movie movie = (Movie)adapter.getItem(i - list.getHeaderViewsCount());
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, movie, null, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
