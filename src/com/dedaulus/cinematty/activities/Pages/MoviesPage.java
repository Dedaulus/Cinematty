package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieActivity;
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:18
 */
public class MoviesPage implements SliderPage {
    private Context context;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private MovieImageRetriever imageRetriever;
    private MovieItemAdapter movieListAdapter;
    private boolean binded = false;
    private boolean visible = false;

    public MoviesPage(Context context, ApplicationSettings settings, ActivitiesState activitiesState, MovieImageRetriever imageRetriever) {
        this.context = context;
        this.settings = settings;
        this.activitiesState = activitiesState;
        this.imageRetriever = imageRetriever;
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
            movieListAdapter.onResume();
            movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), settings.getCurrentDay()));
        }
    }

    public void onPause() {}

    public void onStop() {
        movieListAdapter.onStop();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((SherlockActivity)context).getSupportMenuInflater();
        inflater.inflate(R.menu.movie_sort_menu, menu);

        switch (settings.getMovieSortOrder()) {
        case BY_CAPTION:
            menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
            break;

        case BY_POPULAR:
            menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
            break;

        case BY_IMDB:
            menu.findItem(R.id.submenu_movie_sort_by_imdb).setChecked(true);
            break;

        case BY_KP:
            menu.findItem(R.id.submenu_movie_sort_by_kp).setChecked(true);
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

            case R.id.submenu_movie_sort_by_imdb:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_IMDB, settings.getCurrentDay()));
                settings.saveMovieSortOrder(MovieSortOrder.BY_IMDB);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_kp:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_KP, settings.getCurrentDay()));
                settings.saveMovieSortOrder(MovieSortOrder.BY_KP);
                item.setChecked(true);
                return true;

            default:
                return true;
        }
    }

    private View bindView(View view) {
        IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
        movieListAdapter = new MovieItemAdapter(context, notifier, new ArrayList<Movie>(settings.getMovies().values()), imageRetriever);
        GridView grid = (GridView)view.findViewById(R.id.movie_list);
        grid.setAdapter(movieListAdapter);
        grid.setOnScrollListener(notifier);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        Movie movie = (Movie)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, movie, null, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
