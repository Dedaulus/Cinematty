package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.City;
import com.dedaulus.cinematty.framework.SyncStatus;

/**
 * User: Dedaulus
 * Date: 18.03.12
 * Time: 12:39
 */
public class PreferencesActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    CinemattyApplication app;
    private City previousCity;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }
        previousCity = app.getCurrentCity();
        final Context context = this;
        
        Preference problemPreference = getPreferenceScreen().findPreference("problem");
        problemPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                String email = context.getString(R.string.email);
                String subject = String.format(
                        context.getString(R.string.support_email_subject), 
                        context.getString(R.string.app_name), 
                        context.getString(R.string.app_version));
                
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

                context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.support_email_intent_caption)));
                return true;
            }
        });

        Preference aboutPreference = getPreferenceScreen().findPreference("about");
        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(context, AboutActivity.class));
                return true;
            }
        });
        String aboutSummary = String.format(getString(R.string.pref_about_summary), getString(R.string.app_version));
        aboutPreference.setSummary(aboutSummary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListPreference citiesPreferences = (ListPreference)getPreferenceScreen().findPreference("current_city");
        citiesPreferences.setSummary(citiesPreferences.getEntry());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if (!app.getCurrentCity().equals(previousCity)) {
            app.restart();
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference)pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}