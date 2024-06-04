/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapSavedState.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;


// This class manages the map saved states

public class MapSavedState {

    private SharedPreferences preferences;


    public MapSavedState(Context context) {
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
    }

    public void setLocationStatus(double latitude, double longitude, String address) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.MAP_LATITUDE, (float) latitude);
        editor.putFloat(Constants.MAP_LONGITUDE, (float) longitude);
        editor.putString(Constants.MAP_ADDRESS, address);
        editor.apply();
    }

    public float getLatitude() {
        return preferences.getFloat(Constants.MAP_LATITUDE, (float) Constants.DEFAULT_LATITUDE);
    }

    public float getLongitude() {
        return preferences.getFloat(Constants.MAP_LONGITUDE, (float) Constants.DEFAULT_LONGITUDE);
    }

    public String getAddress() {
        return preferences.getString(Constants.MAP_ADDRESS, Constants.MAP_NO_ADDRESS);
    }

    // This method returns the intent that will call Google Maps set according to preferences_old defined in Settings
    public Intent getNavigationOptionIntent(Context context, String navigationOption, String navigationMode) {

        Uri gmmIntentUri;
        String navMode;

        // set navigation mode
        if (navigationMode.matches(context.getString(R.string.set_def_nav_mode_walk))) { // Walk
            navMode = "w";
        } else if (navigationMode.matches(context.getString(R.string.set_def_nav_mode_drive))) { // Drive
            navMode = "d";
        } else if (navigationMode.matches(context.getString(R.string.set_def_nav_mode_bicycle))) { // Bicycle
            navMode = "b";
        } else {
            navMode = "";
        }

        // set navigation option
        if (navigationOption.matches(context.getString(R.string.set_nav_option_map))) { // Map
            gmmIntentUri = Uri.parse("geo:?q=" + String.valueOf(getLatitude()) + ","
                    + String.valueOf(getLongitude()) + "&mode=" + navMode);
        } else if (navigationOption.matches(context.getString(R.string.set_nav_option_navigator))) { // Navigator
            gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(getLatitude()) + ","
                    + String.valueOf(getLongitude()) + "&mode=" + navMode);
        } else {
            gmmIntentUri = null;
        }

        // Calls Google Maps with the chosen settings
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage("com.google.android.apps.maps");

        return intent;
    }


}
