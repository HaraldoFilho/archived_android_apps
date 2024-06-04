/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : LocationItem.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.lists;

import android.content.Context;
import android.content.Intent;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LocationItem {

    private Context context;
    private String name;
    private String address;
    private double latitude;
    private double longitude;


    // Constructor which sets only context
    public LocationItem(Context context) {
        this.context = context;
    }

    // Constructor which sets all location fields
    public LocationItem(Context context, String locationName, String locationAddress,
                        double locationLatitude, double locationLongitude) {
        this.context = context;
        this.name = locationName;
        this.address = locationAddress;
        this.latitude = locationLatitude;
        this.longitude = locationLongitude;
    }

    public void setName(String locationName) {
        this.name = locationName;
    }

    public void setLatitude(double locationLatitude) {
        this.latitude = locationLatitude;
    }

    public void setLongitude(double locationLongitude) {
        this.longitude = locationLongitude;
    }

    public void setAddress(String locationAddress) {
        this.address = locationAddress;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    // get location address text that will be added to a bookmarks or history list
    protected String getAddressText() {

        String latitude;
        String longitude;
        String text;

        // if location item has an address set, return this address
        if (!getAddress().matches(Constants.MAP_NO_ADDRESS)) {
            return getAddress();
        } else { // returns the location latitude/longitude as address
            latitude = String.valueOf(getLatitude());
            longitude = String.valueOf(getLongitude());

            // round latitude value to a maximum length
            if (latitude.length() > Constants.LAT_LNG_MAX_LENGTH) {
                latitude = latitude.substring(Constants.STRING_HEAD, Constants.LAT_LNG_MAX_LENGTH);
            }
            // round longitude value to a maximum length
            if (longitude.length() > Constants.LAT_LNG_MAX_LENGTH) {
                longitude = longitude.substring(Constants.STRING_HEAD, Constants.LAT_LNG_MAX_LENGTH);
            }

            // set latitude/longitude text
            text = context.getString(R.string.layout_latitude) + " " + latitude
                    + ", " + context.getString(R.string.layout_longitude) + " " + longitude;

            return text;
        }

    }

    // if a name is not provided to location,
    // set current date and time as location name
    public void setTimeAsName() {

        /// get system language
        String systemLanguage = Locale.getDefault().getLanguage().toString();
        String dateFormat;

        switch (systemLanguage) {

            // set date and time to brazilian portuguese format
            // if this is the system language
            case Constants.LANGUAGE_PORTUGUESE:
                dateFormat = context.getResources().getString(R.string.system_time_pt);
                break;

            // set date and time to english format (default)
            // to any other system language
            default:
                dateFormat = context.getResources().getString(R.string.system_time_default);

        }

        SimpleDateFormat date = new SimpleDateFormat(dateFormat, Locale.getDefault());

        name = date.format(new Date().getTime());

    }

    // share location as a google maps url
    public void share() {
        String address;
        if (!getAddress().matches(Constants.MAP_NO_ADDRESS)) {
            address = getAddress() + "\n";
        } else {
            address = "";
        }
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.action_share_title));
        intent.putExtra(Intent.EXTRA_TEXT, getName() + ":\n" + address + "http://maps.google.com/?q="
                + getLatitude() + "," + getLongitude() + "\n" + context.getString(R.string.action_share_message)
                + ": " + context.getString(R.string.info_app_url));
        context.startActivity(Intent.createChooser(intent, null));
    }

}