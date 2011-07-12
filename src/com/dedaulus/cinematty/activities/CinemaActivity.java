package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.tools.CurrentState;

/**
 * User: Dedaulus
 * Date: 10.04.11
 * Time: 14:48
 */
public class CinemaActivity extends Activity {
    private CinemattyApplication mApp;
    private CurrentState mCurrentState;

    @Override
    protected void onResume() {
        mCurrentState = mApp.getCurrentState();

        super.onResume();
    }

    @Override
    protected void onPause() {
        mApp.saveFavouriteCinemas();

        super.onPause();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_info);

        mApp = (CinemattyApplication)getApplication();
        mCurrentState = mApp.getCurrentState();

        switch (mCurrentState.activityType) {
        case CINEMA_INFO:
            setFavourite();
            setCaption();
            setAddress();
            setPhone();
            setUrl();
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.revertCurrentState();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setFavourite() {
        ImageView imageView = (ImageView)findViewById(R.id.fav_icon_in_cinema_info);

        if (mCurrentState.cinema.getFavourite() > 0) {
            imageView.setImageResource(R.drawable.ic_fav_cinema_on);
        } else {
            imageView.setImageResource(R.drawable.ic_fav_cinema_off);
        }
    }

    private void setCaption() {
        TextView view = (TextView)findViewById(R.id.cinema_caption);
        view.setText(mCurrentState.cinema.getCaption());
    }

    private void setAddress() {
        View view = findViewById(R.id.cinema_address_panel);
        if (mCurrentState.cinema.getAddress() != null) {
            TextView address = (TextView)findViewById(R.id.cinema_address);
            address.setText(mCurrentState.cinema.getAddress());

            TextView into = (TextView)findViewById(R.id.cinema_into);
            if (mCurrentState.cinema.getInto() != null) {
                into.setText(mCurrentState.cinema.getInto());
                into.setVisibility(View.VISIBLE);
            } else {
                into.setVisibility(View.GONE);
            }

            TextView metro = (TextView)findViewById(R.id.cinema_metro);
            if (mCurrentState.cinema.getMetro() != null) {
                metro.setText(getString(R.string.metro_near) + ": " + mCurrentState.cinema.getMetro());
                metro.setVisibility(View.VISIBLE);
            } else {
                metro.setVisibility(View.GONE);
            }

            view.setVisibility(View.VISIBLE);
            findViewById(R.id.address_divider).setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
            findViewById(R.id.address_divider).setVisibility(View.GONE);
        }
    }

    private void setPhone() {
        View view = findViewById(R.id.cinema_phone_panel);
        if (mCurrentState.cinema.getPhone() != null) {
            TextView phone = (TextView)findViewById(R.id.cinema_phone);
            phone.setText(mCurrentState.cinema.getPhone());

            view.setVisibility(View.VISIBLE);
            findViewById(R.id.phone_divider).setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
            findViewById(R.id.phone_divider).setVisibility(View.GONE);
        }
    }

    private void setUrl() {
        TextView url = (TextView)findViewById(R.id.cinema_url);
        if (mCurrentState.cinema.getUrl() != null) {
            StringBuffer buf = new StringBuffer(mCurrentState.cinema.getUrl());

            if (mCurrentState.cinema.getUrl().startsWith("http://")) {
                buf.delete(0, "http://".length());
            }

            int slashPos = buf.indexOf("/");
            if (slashPos != -1) {
                buf.delete(slashPos, buf.length());
            }

            SpannableString str = new SpannableString(buf.toString());
            str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);

            url.setText(str);

            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }
    }

    public void onAddressClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=Россия, " + mApp.getCurrentCity().getName() + ", " + mCurrentState.cinema.getAddress()));
        startActivity(intent);
    }

    public void onPhoneClick(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+7" + mCurrentState.cinema.getPlainPhone()));
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        if (mCurrentState.cinema.getUrl() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mCurrentState.cinema.getUrl()));
            startActivity(intent);
        }
    }

    public void onFavIconClick(View view) {
        if (mCurrentState.cinema.getFavourite() > 0) {
            mCurrentState.cinema.setFavourite(false);
            ((ImageView)view).setImageResource(R.drawable.ic_fav_cinema_off);
        } else {
            mCurrentState.cinema.setFavourite(true);
            ((ImageView)view).setImageResource(R.drawable.ic_fav_cinema_on);
        }
    }

    public void onSchedulesClick(View view) {
        CurrentState state = mCurrentState.clone();
        state.activityType = CurrentState.ActivityType.MOVIE_LIST_W_CINEMA;
        mApp.setCurrentState(state);

        Intent intent = new Intent(this, MovieListActivity.class);
        startActivity(intent);
    }
}