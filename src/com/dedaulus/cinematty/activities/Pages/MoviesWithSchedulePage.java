package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieActivity;
import com.dedaulus.cinematty.activities.adapters.MovieItemWithScheduleAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 01.04.12
 * Time: 23:12
 */
public class MoviesWithSchedulePage implements SliderPage {
    private Context context;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private ActivityState state;
    private MovieImageRetriever imageRetriever;
    private MovieItemWithScheduleAdapter movieListAdapter;
    private IdleDataSetChangeNotifier notifier;
    private int currentDay;
    private int dayPart;
    private View pageView;
    private boolean binded = false;
    private boolean visible = false;

    public MoviesWithSchedulePage(Context context, ApplicationSettings settings, ActivitiesState activitiesState, ActivityState state, MovieImageRetriever imageRetriever) {
        this.context = context;
        this.settings = settings;
        this.activitiesState = activitiesState;
        this.state = state;
        this.imageRetriever = imageRetriever;
    }

    @Override
    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        pageView = layoutInflater.inflate(R.layout.movie_list_w_cinema, null, false);
        return bindView(pageView);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.showtime_caption);
    }

    @Override
    public void onResume() {
        if (binded) {
            movieListAdapter.onResume();
            if (currentDay != settings.getCurrentDay()) {
                setCurrentDay(settings.getCurrentDay());
            }
            movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
        }
    }

    @Override
    public void onPause() {}

    @Override
    public void onStop() {
        movieListAdapter.onStop();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((SherlockActivity)context).getSupportMenuInflater();

        inflater.inflate(R.menu.select_day_menu, menu);
        switch (currentDay) {
            case Constants.TODAY_SCHEDULE:
                menu.findItem(R.id.submenu_select_day_today).setChecked(true);
                break;

            case Constants.TOMORROW_SCHEDULE:
                menu.findItem(R.id.submenu_select_day_tomorrow).setChecked(true);
                break;

            case Constants.AFTER_TOMORROW_SCHEDULE:
                menu.findItem(R.id.submenu_select_day_after_tomorrow).setChecked(true);
                break;
        }

        inflater.inflate(R.menu.day_part_menu, menu);
        menu.findItem(R.id.submenu_day_part_whole).setChecked(true);

        inflater.inflate(R.menu.movie_w_schedule_sort_menu, menu);
        switch (settings.getMovieWithScheduleSortOrder()) {
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

            case BY_TIME_LEFT:
                menu.findItem(R.id.submenu_movie_sort_by_time_left).setChecked(true);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_day:
                return true;

            case R.id.submenu_select_day_today:
                setCurrentDay(Constants.TODAY_SCHEDULE);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_select_day_tomorrow:
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_select_day_after_tomorrow:
                setCurrentDay(Constants.AFTER_TOMORROW_SCHEDULE);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_day_part_whole:
                setCurrentDayPart(Constants.WHOLE_DAY);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_day_part_morning:
                setCurrentDayPart(Constants.IN_MORNING);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_day_part_afternoon:
                setCurrentDayPart(Constants.IN_AFTERNOON);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_day_part_evening:
                setCurrentDayPart(Constants.IN_EVENING);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.submenu_day_part_night:
                setCurrentDayPart(Constants.AT_NIGHT);
                movieListAdapter.sortBy(new MovieComparator(settings.getMovieWithScheduleSortOrder(), currentDay, state.cinema.getShowTimes(currentDay)));
                item.setChecked(true);
                return true;

            case R.id.menu_movie_sort:
                return true;

            case R.id.submenu_movie_sort_by_caption:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION));
                settings.saveMovieWithScheduleSortOrder(MovieSortOrder.BY_CAPTION);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_popular:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, currentDay));
                settings.saveMovieWithScheduleSortOrder(MovieSortOrder.BY_POPULAR);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_imdb:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_IMDB));
                settings.saveMovieWithScheduleSortOrder(MovieSortOrder.BY_IMDB);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_kp:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_KP));
                settings.saveMovieWithScheduleSortOrder(MovieSortOrder.BY_KP);
                item.setChecked(true);
                return true;

            case R.id.submenu_movie_sort_by_time_left:
                movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_TIME_LEFT, state.cinema.getShowTimes(currentDay)));
                settings.saveMovieWithScheduleSortOrder(MovieSortOrder.BY_TIME_LEFT);
                item.setChecked(true);
                return true;

            default:
                return true;
        }
    }

    private View bindView(View view) {
        notifier = new IdleDataSetChangeNotifier();
        setCurrentDay(settings.getCurrentDay());
        GridView grid = (GridView)view.findViewById(R.id.movie_list);
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
        MovieItemWithScheduleAdapter adapter = (MovieItemWithScheduleAdapter)adapterView.getAdapter();
        Movie movie = (Movie)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.movie = movie;
        state.activityType = ActivityState.MOVIE_INFO_W_SCHEDULE;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }

    private void setCurrentDay(int day) {
        settings.setCurrentDay(day);
        currentDay = day;

        TextView dayIndicator = (TextView)pageView.findViewById(R.id.day_indicator).findViewById(R.id.caption);
        StringBuilder builder = new StringBuilder();
        switch (currentDay) {
            case Constants.TODAY_SCHEDULE:
                builder.append(context.getString(R.string.today));
                break;

            case Constants.TOMORROW_SCHEDULE:
                builder.append(context.getString(R.string.tomorrow));
                break;

            case Constants.AFTER_TOMORROW_SCHEDULE:
                builder.append(context.getString(R.string.after_tomorrow));
                break;
        }

        builder.append(" ");

        switch (dayPart) {
            case Constants.IN_MORNING:
                builder.append(context.getString(R.string.in_morning).toLowerCase());
                break;

            case Constants.IN_AFTERNOON:
                builder.append(context.getString(R.string.in_afternoon).toLowerCase());
                break;

            case Constants.IN_EVENING:
                builder.append(context.getString(R.string.in_evening).toLowerCase());
                break;

            case Constants.AT_NIGHT:
                builder.append(context.getString(R.string.at_night).toLowerCase());
                break;
        }

        dayIndicator.setText(builder.toString());

        Pair<Calendar, Calendar> timeRange = DataConverter.getTimeRange(dayPart, currentDay);
        Collection<Movie> movies = DataConverter.getMoviesFromTimeRange(state.cinema.getShowTimes(currentDay), timeRange);

        if (movies.isEmpty()) {
            pageView.findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.no_schedule).setVisibility(View.GONE);
        }

        if (movieListAdapter != null) {
            movieListAdapter.onStop();
        }

        movieListAdapter = new MovieItemWithScheduleAdapter(context, notifier, new ArrayList<Movie>(movies), state.cinema, currentDay, timeRange, imageRetriever);
        GridView grid = (GridView)pageView.findViewById(R.id.movie_list);
        grid.setAdapter(movieListAdapter);

        movieListAdapter.onResume();

        String timeRangeString = DataConverter.getTimeRangeString(context, dayPart);
        if (timeRangeString != null) {
            Toast.makeText(context, timeRangeString, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentDayPart(int dayPart) {
        this.dayPart = dayPart;
        setCurrentDay(currentDay);
    }
}
