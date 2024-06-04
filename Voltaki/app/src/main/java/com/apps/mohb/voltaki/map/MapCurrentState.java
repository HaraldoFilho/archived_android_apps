/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapCurrentState.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.map;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonEnums;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


// This class manages the map current states

public class MapCurrentState {

    private Context context;

    public static GoogleMap googleMap;
    public static boolean mapMoved;

    private static Location location;
    private static double latitude;
    private static double longitude;
    private static String locationAddress;


    public MapCurrentState(Context context) {
        this.context = context;
    }

    public Location getLastLocation() {
        return location;
    }

    public void setLastLocation(Location mLastLocation) {
        location = mLastLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double mLatitude) {
        latitude = mLatitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double mLongitude) {
        longitude = mLongitude;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public static void setLocationAddress(String mLocationAddress) {
        locationAddress = mLocationAddress;
    }

    public void gotoLocation(double latitude, double longitude, int zoomLevel) {
        googleMap.clear();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
                longitude), zoomLevel));
    }

    // Updates red marker position and label in Map
    public void updateUI(double latitude, double longitude) {
        LatLng lastLocation = new LatLng(latitude, longitude);
        googleMap.clear();
        Marker marker = googleMap.addMarker(new MarkerOptions().position(lastLocation));
        ButtonCurrentState buttonCurrentState = new ButtonCurrentState(context);
        if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                == ButtonEnums.convertEnumToInt(ButtonStatus.COME_BACK_HERE)) {
            marker.setTitle(context.getResources().getString(R.string.map_you_are_here));
        } else {
            marker.setTitle(context.getResources().getString(R.string.map_you_were_here));
        }
        marker.showInfoWindow();
    }

    // Checks if GPS is enabled
    public boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    // Check if network location is enabled
    public boolean isNetworkEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

}
