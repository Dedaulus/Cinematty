package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.CityListActivity;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.HashMap;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:17
 */
public class CategoriesPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private HashMap<Integer, Integer> slideIds;
    private ViewPager slider;

    public CategoriesPage(Context context, CinemattyApplication app, ViewPager slider, HashMap<Integer, Integer> slideIds) {
        this.slider = slider;
        this.context = context;
        this.app = app;
        this.slideIds = slideIds;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.categories, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.categories_caption);
    }

    public void onResume() {}

    public void onPause() {}

    public void onStop() {}

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        View cityPanel = view.findViewById(R.id.select_city_panel);
        cityPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onCityClick(view);
            }
        });
        TextView cityCaption = (TextView)cityPanel.findViewById(R.id.city_caption);
        cityCaption.setText(app.getCurrentCity().getName());

        View whatsNewPanel = view.findViewById(R.id.whats_new_panel);
        whatsNewPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onWhatsNewClick(view);
            }
        });

        View cinemasPanel = view.findViewById(R.id.cinemas_panel);
        cinemasPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onCinemasClick(view);
            }
        });

        View moviesPanel = view.findViewById(R.id.movies_panel);
        moviesPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onMoviesClick(view);
            }
        });

        View actorsPanel = view.findViewById(R.id.actors_panel);
        actorsPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onActorsClick(view);
            }
        });

        View genresPanel = view.findViewById(R.id.genres_panel);
        genresPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onGenresClick(view);
            }
        });

        return view;
    }

    public void onCityClick(View view) {
        Intent intent = new Intent(context, CityListActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public void onWhatsNewClick(View view) {
        slider.setCurrentItem(slideIds.get(Constants.WHATS_NEW_SLIDE));
    }

    public void onCinemasClick(View view) {
        slider.setCurrentItem(slideIds.get(Constants.CINEMAS_SLIDE));
    }

    public void onMoviesClick(View view) {
        slider.setCurrentItem(slideIds.get(Constants.MOVIES_SLIDE));
    }

    public void onActorsClick(View view) {
        slider.setCurrentItem(slideIds.get(Constants.ACTORS_SLIDE));
    }

    public void onGenresClick(View view) {
        slider.setCurrentItem(slideIds.get(Constants.GENRES_SLIDE));
    }
}
