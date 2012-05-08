package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.CinemaMapView;
import com.dedaulus.cinematty.framework.Metro;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.List;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 02.04.12
 * Time: 0:51
 */
public class CinemaPage implements SliderPage {
    Context context;
    ActivitiesState activitiesState;
    ActivityState state;
    private boolean visible = false;

    public CinemaPage(Context context, ActivitiesState activitiesState, ActivityState state) {
        this.context = context;
        this.activitiesState = activitiesState;
        this.state = state;
    }

    @Override
    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View pageView = layoutInflater.inflate(R.layout.cinema_info, null, false);
        setCinemaCaption(pageView);
        setCinemaUrl(pageView);
        setCinemaAddress(pageView);
        setCinemaPhone(pageView);
        return pageView;
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.cinema_caption);
    }

    @Override
    public void onResume() {}

    @Override
    public void onPause() {}

    @Override
    public void onStop() {}

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((SherlockActivity)context).getSupportMenuInflater();

        if (state.cinema.getAddress() != null) {
            inflater.inflate(R.menu.show_map_menu, menu);
        }

        if (state.cinema.getPhone() != null) {
            inflater.inflate(R.menu.call_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call:
                onCinemaPhoneClick();
                break;

            case R.id.menu_show_map:
                showCinemaOnMap();
                break;
        }

        return true;
    }

    private void setCinemaCaption(View view) {
        TextView captionView = (TextView)view.findViewById(R.id.cinema_caption);
        captionView.setText(state.cinema.getName());
    }

    private void setCinemaAddress(View view) {
        View region = view.findViewById(R.id.cinema_address_region);
        String address = state.cinema.getAddress();
        if (address != null) {
            TextView divider = (TextView)region.findViewById(R.id.cinema_address_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.address_separator));
            TextView addressView = (TextView)region.findViewById(R.id.cinema_address);
            addressView.setText(address);

            TextView intoView = (TextView)region.findViewById(R.id.cinema_into);
            String into = state.cinema.getInto();
            if (into != null) {
                intoView.setText(into);
                intoView.setVisibility(View.VISIBLE);
            } else {
                intoView.setVisibility(View.GONE);
            }

            ViewGroup metroRegion = (ViewGroup)region.findViewById(R.id.cinema_metro_region);
            List<Metro> metros = state.cinema.getMetros();
            if (metros.isEmpty()) {
                metroRegion.setVisibility(View.GONE);
            } else {
                //metroView.setText(getString(R.string.metro_near) + ": " + state.cinema.getMetros());
                LayoutInflater inflater = LayoutInflater.from(context);
                for (Metro metro : metros) {
                    View metroView = inflater.inflate(R.layout.metro_item, null);
                    View indicator = metroView.findViewById(R.id.indicator);
                    indicator.setBackgroundColor(metro.getColor());
                    TextView caption = (TextView)metroView.findViewById(R.id.metro_caption);
                    caption.setText(metro.getName());

                    metroRegion.addView(metroView);
                }
                metroRegion.setVisibility(View.VISIBLE);
            }

            region.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCinemaOnMap();
                }
            });
            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }

    private void setCinemaPhone(View view) {
        View region = view.findViewById(R.id.cinema_phone_region);
        String phone = state.cinema.getPhone();
        if (phone != null) {
            TextView divider = (TextView)region.findViewById(R.id.cinema_phone_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.phone_separator));
            TextView phoneView = (TextView)view.findViewById(R.id.cinema_phone);
            phoneView.setText(phone);

            region.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCinemaPhoneClick();
                }
            });
            region.setVisibility(View.VISIBLE);
        }
        else {
            region.setVisibility(View.GONE);
        }
    }

    private void setCinemaUrl(View view) {
        View region = view.findViewById(R.id.cinema_url_region);
        TextView urlView = (TextView)region.findViewById(R.id.cinema_url);
        String url = state.cinema.getUrl();
        if (url != null) {
            StringBuilder buf = new StringBuilder(url);
            if (state.cinema.getUrl().startsWith("http://")) {
                buf.delete(0, "http://".length());
            }
            int slashPos = buf.indexOf("/");
            if (slashPos != -1) {
                buf.delete(slashPos, buf.length());
            }
            SpannableString str = new SpannableString(buf.toString());
            str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);
            urlView.setText(str);

            region.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCinemaUrlClick();
                }
            });

            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }

    private void onCinemaPhoneClick() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+7" + state.cinema.getPlainPhone()));
        context.startActivity(intent);
    }

    private void onCinemaUrlClick() {
        String url = state.cinema.getUrl();
        if (url != null) {
            if (!url.startsWith("http://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    private void onCinemaFavIconClick(View view) {
        if (state.cinema.getFavourite() > 0) {
            state.cinema.setFavourite(false);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_off);
        } else {
            state.cinema.setFavourite(true);
            ((ImageView)view).setImageResource(R.drawable.ic_list_fav_on);
        }
    }

    private void showCinemaOnMap() {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.CINEMA_ON_MAP, this.state.cinema, null, null, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, CinemaMapView.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
}
