/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : FetchAddressIntentService.java
 *  Last modified : 9/29/20 12:29 AM
 *
 *  -----------------------------------------------------------
 */

/*
 *  This code was extracted and modified from:
 *  https://developer.android.com/training/location/display-address.html
 *  according to Creative Commons Attribution 2.5 license:
 *  http://creativecommons.org/licenses/by/2.5/
 *
 *  File          : LocationItem.java
 *  Last modified : 6/25/16 10:24 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.map;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.fragments.MainFragment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class FetchAddressIntentService extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    protected MainFragment.AddressResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super(null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        mReceiver = new MainFragment.AddressResultReceiver(null);

        try {
            assert location != null;
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e(Constants.LOG_TAG, "Location Service not available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.e(Constants.LOG_TAG, "Invalid Latitude,Longitude used" + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.isEmpty()) {

            errorMessage = getString(R.string.toast_no_address_found);
            Log.e(Constants.LOG_TAG, errorMessage);
            deliverResultToReceiver(FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(Constants.LIST_HEAD);
            deliverResultToReceiver(SUCCESS_RESULT, address.getAddressLine(Constants.LIST_HEAD));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }


}

/*
 * Portions of this page are reproduced from work created and shared by the Android Open Source Project
 * and used according to terms described in the Creative Commons 2.5 Attribution License.
 *
 * Portions of this page are modifications based on work created and shared by the Android Open Source Project
 * and used according to terms described in the Creative Commons 2.5 Attribution License.
 */
