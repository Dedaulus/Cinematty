package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;

/**
 * User: Dedaulus
 * Date: 10.04.11
 * Time: 14:48
 */
public class CinemaActivity extends Activity {
    CinemattyApplication mApp;
    Cinema mCurrentCinema;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinema_info);

        mApp = (CinemattyApplication)getApplication();
        mCurrentCinema = mApp.getCurrentCinema();

        setCaption();
        setAddress();
        setPhone();
        setUrl();
    }

    private void setCaption() {
        TextView view = (TextView)findViewById(R.id.cinema_caption);
        view.setText(mCurrentCinema.getCaption());
    }

    private void setAddress() {
        View view = findViewById(R.id.cinema_address_panel);
        if (mCurrentCinema.getAddress() != null) {
            TextView address = (TextView)findViewById(R.id.cinema_address);
            address.setText(mCurrentCinema.getAddress());

            TextView metro = (TextView)findViewById(R.id.cinema_metro);
            if (mCurrentCinema.getMetro() != null) {
                metro.setText(getString(R.string.metro_near) + ": " + mCurrentCinema.getMetro());

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
        if (mCurrentCinema.getPhone() != null) {
            TextView phone = (TextView)findViewById(R.id.cinema_phone);
            phone.setText(mCurrentCinema.getPhone());

            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
        }
    }

    private void setUrl() {
        View view = findViewById(R.id.cinema_url_panel);
        if (mCurrentCinema.getUrl() != null) {
            StringBuffer buf = new StringBuffer(mCurrentCinema.getUrl());

            if (mCurrentCinema.getUrl().startsWith("http://")) {
                buf.delete(0, "http://".length());
            }

            int slashPos = buf.indexOf("/");
            if (slashPos != -1) {
                buf.delete(slashPos, buf.length());
            }

            SpannableString str = new SpannableString(buf.toString());
            str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);

            TextView url = (TextView)findViewById(R.id.cinema_url);
            url.setText(str);

            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public void onAddressClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=Россия, Санкт-Петербург, " + mCurrentCinema.getAddress()));
        startActivity(intent);
    }

    public void onPhoneClick(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+7" + mCurrentCinema.getPlainPhone()));
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mCurrentCinema.getUrl()));
        startActivity(intent);
    }
}