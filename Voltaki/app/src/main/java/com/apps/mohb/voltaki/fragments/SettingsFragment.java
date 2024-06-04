/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : SettingsFragment.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.MainActivity;
import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonEnums;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.apps.mohb.voltaki.lists.Lists;
import com.apps.mohb.voltaki.map.MapCurrentState;
import com.apps.mohb.voltaki.messaging.Notification;
import com.google.android.gms.maps.GoogleMap;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of navigation option, default navigation mode,
        // status bar icon and history max items preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.set_key_map_type))));
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.set_key_nav_option))));
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.set_key_def_nav_mode))));
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.set_key_def_zoom_level))));
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.set_key_max_history_items))));
        setPreferenceClickListener(Objects.requireNonNull(findPreference(Constants.NOTIFICATION)));
    }

    // start main activity if back arrow is pressed in the app bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            // if value changed is the maximum history items then calls method that will
            // will update history list number of items to the value set
            if (stringValue.matches(getString(R.string.set_max_history_items_default)) ||
                    stringValue.matches(getString(R.string.set_max_history_items_10)) ||
                    stringValue.matches(getString(R.string.set_max_history_items_unlimited))) {
                updateHistoryMaxItems();
            } else
                // if value changed is the map type then update map
                if (stringValue.matches(getString(R.string.set_map_type_normal))) {
                    MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (stringValue.matches(getString(R.string.set_map_type_satellite))) {
                    MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (stringValue.matches(getString(R.string.set_map_type_hybrid))) {
                    MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(value.toString());

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(value.toString());
            }

            return true;

        }

    };

    // Listen to changes on Notification setting
    private Preference.OnPreferenceClickListener preferenceClickListener
            = new Preference.OnPreferenceClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Notification notification = new Notification();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
            ButtonCurrentState buttonCurrentState = new ButtonCurrentState(requireContext());
            // if button is GREEN and Notification setting is checked start notification
            if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                    > ButtonEnums.convertEnumToInt(ButtonStatus.COME_BACK_HERE)
                    && (sharedPreferences.getBoolean(Constants.NOTIFICATION, true))) {
                notification.startGoBackNotification(requireActivity().getApplicationContext());
            } else { // cancel notification
                notification.cancelNotification(requireActivity().getApplicationContext(), Constants.NOTIFICATION_ID);
            }
            return true;
        }
    };

    private void setPreferenceClickListener(Preference preference) {
        preference.setOnPreferenceClickListener(preferenceClickListener);
    }

    private void updateHistoryMaxItems() {
        Lists lists = new Lists(requireActivity().getApplicationContext());
        lists.pruneHistory();
    }

}