/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : GoBackNotificationActivity.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.messaging;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.apps.mohb.voltaki.map.MapSavedState;


// Activity that will be called when the status bar notification is clicked
// This activity is closed just after being called so it is not shown

public class GoBackNotificationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        MapSavedState mapSavedState = new MapSavedState(getApplicationContext());
        ButtonCurrentState buttonCurrentState = new ButtonCurrentState(getApplicationContext());

        // set button to GO BACK CLICKED state because notification being clicked
        // is equivalent to clicking button when in GO BACK state
        buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK_CLICKED);

        // get navigation option and default navigation mode
        String defNavOption = sharedPref.getString(Constants.NAVIGATION_OPTION, "");
        String defDefNavMode = sharedPref.getString(Constants.DEFAULT_NAV_MODE, "");

        // start Google Maps with the gotten option and mode
        assert defDefNavMode != null;
        startActivity(mapSavedState.getNavigationOptionIntent(getApplicationContext(), defNavOption, defDefNavMode));

        // close activity
        finish();

    }

}
