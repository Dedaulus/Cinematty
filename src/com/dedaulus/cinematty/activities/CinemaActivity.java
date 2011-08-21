package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 10.04.11
 * Time: 14:48
 */
public class CinemaActivity extends Activity {
    private CinemattyApplication mApp;
    private ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_info);

        mApp = (CinemattyApplication)getApplication();
        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        switch (mState.activityType) {
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
    protected void onPause() {
        mApp.saveFavouriteCinemas();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);

        super.onBackPressed();
    }

    private void setFavourite() {
        ImageView imageView = (ImageView)findViewById(R.id.fav_icon_in_cinema_info);

        if (mState.cinema.getFavourite() > 0) {
            imageView.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            imageView.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void setCaption() {
        TextView view = (TextView)findViewById(R.id.cinema_caption);
        view.setText(mState.cinema.getCaption());
    }

    private void setAddress() {
        View view = findViewById(R.id.cinema_address_panel);
        if (mState.cinema.getAddress() != null) {
            TextView address = (TextView)findViewById(R.id.cinema_address);
            address.setText(mState.cinema.getAddress());

            TextView into = (TextView)findViewById(R.id.cinema_into);
            if (mState.cinema.getInto() != null) {
                into.setText(mState.cinema.getInto());
                into.setVisibility(View.VISIBLE);
            } else {
                into.setVisibility(View.GONE);
            }

            TextView metro = (TextView)findViewById(R.id.cinema_metro);
            if (mState.cinema.getMetro() != null) {
                metro.setText(getString(R.string.metro_near) + ": " + mState.cinema.getMetro());
                metro.setVisibility(View.VISIBLE);
            } else {
                metro.setVisibility(View.GONE);
            }

            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setPhone() {
        View view = findViewById(R.id.cinema_phone_panel);
        if (mState.cinema.getPhone() != null) {
            TextView phone = (TextView)findViewById(R.id.cinema_phone);
            phone.setText(mState.cinema.getPhone());

            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
        }
    }

    private void setUrl() {
        TextView url = (TextView)findViewById(R.id.cinema_url);
        if (mState.cinema.getUrl() != null) {
            StringBuffer buf = new StringBuffer(mState.cinema.getUrl());

            if (mState.cinema.getUrl().startsWith("http://")) {
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
        intent.setData(Uri.parse("geo:0,0?q=Россия, " + mApp.getCurrentCity().getName() + ", " + mState.cinema.getAddress()));
        startActivity(intent);
    }

    public void onPhoneClick(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+7" + mState.cinema.getPlainPhone()));
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        if (mState.cinema.getUrl() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mState.cinema.getUrl()));
            startActivity(intent);
        }
    }

    public void onFavIconClick(View view) {
        if (mState.cinema.getFavourite() > 0) {
            mState.cinema.setFavourite(false);
            ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
        } else {
            mState.cinema.setFavourite(true);
            ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
        }
    }

    public void onSchedulesClick(View view) {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = mState.clone();
        state.activityType = ActivityState.ActivityType.MOVIE_LIST_W_CINEMA;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }
}