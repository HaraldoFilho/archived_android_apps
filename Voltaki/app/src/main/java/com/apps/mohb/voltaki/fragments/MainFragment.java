/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MainFragment.java
 *  Last modified : 9/30/20 12:57 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonEnums;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.apps.mohb.voltaki.fragments.dialogs.BookmarkEditDialogFragment;
import com.apps.mohb.voltaki.fragments.dialogs.NoLocPermissionAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ResetAlertFragment;
import com.apps.mohb.voltaki.lists.Lists;
import com.apps.mohb.voltaki.lists.LocationItem;
import com.apps.mohb.voltaki.map.FetchAddressIntentService;
import com.apps.mohb.voltaki.map.MapCurrentState;
import com.apps.mohb.voltaki.map.MapSavedState;
import com.apps.mohb.voltaki.messaging.Notification;
import com.apps.mohb.voltaki.messaging.Toasts;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


@SuppressWarnings("deprecation")
public class MainFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener {

    private OnFragmentInteractionListener mListener;

    private SharedPreferences sharedPref;
    private SharedPreferences isFirstLocationGot;
    private static SharedPreferences isFirstAddressGot;
    private String defNavOption;
    private String defDefNavMode;

    private MapCurrentState mapCurrentState;
    private MapSavedState mapSavedState;
    private ButtonCurrentState buttonCurrentState;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private static AddressResultReceiver mResultReceiver;

    private View rootView;

    private MapView mMapView;
    private int zoomLevel;

    private Intent mapIntent;

    private Lists lists;
    private Vibrator vibrator;
    private Notification notification;
    private Toasts toasts;
    private static boolean addressFound;
    private static boolean addressNotFound;


    // The code of this inner class was extracted and modified from:
    // https://developer.android.com/training/location/display-address.html
    public static class AddressResultReceiver extends ResultReceiver {

        private String mAddressOutput;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
            assert mAddressOutput != null;
            if ((resultCode == FetchAddressIntentService.SUCCESS_RESULT)
                    && (!mAddressOutput.isEmpty())) {
                MapCurrentState.setLocationAddress(mAddressOutput);
                if (isFirstAddressGot.getBoolean(Constants.MAP_FIRST_ADDRESS, true)) {
                    addressFound = true;
                }
            } else {
                MapCurrentState.setLocationAddress(Constants.MAP_NO_ADDRESS);
                if (isFirstAddressGot.getBoolean(Constants.MAP_FIRST_ADDRESS, true)) {
                    addressNotFound = true;
                }
            }
            // register that the first address update was already taken
            isFirstAddressGot.edit().putBoolean(Constants.MAP_FIRST_ADDRESS, false).apply();

        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mAddressOutput);
        }

        public static final Parcelable.Creator<AddressResultReceiver> CREATOR
                = new Parcelable.Creator<AddressResultReceiver>() {

            public AddressResultReceiver createFromParcel(Parcel in) {
                return new AddressResultReceiver(null);
            }

            public AddressResultReceiver[] newArray(int size) {
                return new AddressResultReceiver[size];
            }
        };

    }

    // interface to interact with MainActivity
    public interface OnFragmentInteractionListener {
        void onReset();

        void onUpdateMainMenuItemResetTitle(int stringId);

        void onUpdateMainMenuItemAddBookmarksState(boolean state);

        void onUpdateMainMenuItemShareState(boolean state);

        void onMapMoved();

        void onButtonLongPressed();

        void onFloatingLongPressed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get settings preferences_old for navigation option and default navigation mode
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        defNavOption = sharedPref.getString(Constants.NAVIGATION_OPTION, "");
        defDefNavMode = sharedPref.getString(Constants.DEFAULT_NAV_MODE, "");

        // create instances of map and button
        mapCurrentState = new MapCurrentState(requireContext());
        mapSavedState = new MapSavedState(requireContext());

        // create instance of button state
        buttonCurrentState = new ButtonCurrentState(requireContext());

        // create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.requireContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // create an instance of the address receiver
        mResultReceiver = new AddressResultReceiver(null);

        // create an instance of the bookmarks and history lists
        lists = new Lists(requireContext());

        // create an instance of the vibrator
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        // create an instance of notification
        notification = new Notification();

        // create an instance of toasts
        toasts = new Toasts(requireContext());

        // when updating location, this variables are used to check if it is the first location value taken
        isFirstLocationGot = Objects.requireNonNull(requireActivity()).getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        isFirstAddressGot = requireActivity().getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

        // create toasts for address message
        toasts.createSearchAddress();
        toasts.createLocationAddress();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        try { // load main fragment view into main activity
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            hideFloatingButton();
            // create map and initialize it
            MapsInitializer.initialize(Objects.requireNonNull(this.requireActivity()));
            mMapView = (MapView) rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);

        } catch (InflateException e) {
            Log.e("mapCurrentState", "Inflate exception");
        }

        // create button
        buttonCurrentState.setButton((Button) rootView.findViewById(R.id.button));

        // manages the clicks on button
        buttonCurrentState.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if vibrate feedback is enabled on settings, vibrate when button is clicked
                if ((vibrator.hasVibrator()) && (sharedPref.getBoolean(Constants.BUTTON_VIBRATE, true))) {
                    vibrator.vibrate(Constants.VIBRATE_LONG_TIME);
                }

                // create intent that will call Google Maps when GO BACK button is clicked
                mapIntent = mapSavedState.getNavigationOptionIntent(Objects.requireNonNull(requireContext()), defNavOption, defDefNavMode);

                switch (buttonCurrentState.getButtonStatus()) {

                    // button is YELLOW
                    case COME_BACK_HERE:
                        // stop getting location updates as the map will be fixed
                        // on the last location value taken
                        stopLocationUpdates();
                        // turn button GREEN
                        buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK);
                        buttonCurrentState.setButtonGoBack(requireContext());
                        // update red marker text
                        mapCurrentState.updateUI(mapCurrentState.getLatitude(), mapCurrentState.getLongitude());
                        // change options menu item text in main screen from Refresh to Reset
                        mListener.onUpdateMainMenuItemResetTitle(R.string.action_reset);
                        // create location item and set its values
                        LocationItem locationItem = new LocationItem(requireContext());
                        locationItem.setTimeAsName();
                        locationItem.setLatitude(mapCurrentState.getLatitude());
                        locationItem.setLongitude(mapCurrentState.getLongitude());
                        // check if an address was gotten
                        if (!mapCurrentState.getLocationAddress().matches(Constants.MAP_NO_ADDRESS)) {
                            locationItem.setAddress(mapCurrentState.getLocationAddress());
                        } else {
                            locationItem.setAddress(Constants.MAP_NO_ADDRESS);
                        }
                        // add location item to history list
                        lists.addItemToHistory(locationItem);
                        // save map state on memory
                        saveMapState();
                        // if notification is enabled show it
                        if (sharedPref.getBoolean(Constants.NOTIFICATION, true)) {
                            notification.createNotificationChannel(requireContext());
                            notification.startGoBackNotification(requireContext());
                        }
                        break;

                    // button is GREEN
                    case GO_BACK:
                        // turn button GREEN with yellow letters
                        buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK_CLICKED);
                        buttonCurrentState.setButtonGoBackClicked(requireContext());
                        // open Google Maps
                        startActivity(mapIntent);
                        break;

                    // button is GREEN with yellow letters
                    case GO_BACK_CLICKED:
                        // open Google Maps
                        startActivity(mapIntent);
                        break;

                    // button is GREEN with red letters
                    case GO_BACK_OFFLINE:
                        // open Google Maps
                        startActivity(mapIntent);
                        break;

                }

            }

        });

        // manages the long clicks on button
        buttonCurrentState.getButton().setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // check if button long click actions are enabled in settings
                if (sharedPref.getBoolean(Constants.BUTTON_CLICK_ACTIONS, true)) {
                    // if vibrate feedback is enabled on settings, vibrate when button is clicked
                    if ((vibrator.hasVibrator()) && (sharedPref.getBoolean(Constants.BUTTON_VIBRATE, true))) {
                        vibrator.vibrate(Constants.VIBRATE_LONG_TIME);
                    }
                    // if button is YELLOW open add bookmark dialog
                    if (buttonCurrentState.getButtonStatus() == ButtonStatus.COME_BACK_HERE) {
                        lists.setBookmarkEditText("");
                        DialogFragment dialog = new BookmarkEditDialogFragment();
                        dialog.show(getParentFragmentManager(), "BookmarkEditDialogFragment");
                    } else
                        // if button is RED reset
                        if (buttonCurrentState.getButtonStatus() == ButtonStatus.OFFLINE) {
                            mListener.onReset();
                        } else { // open reset dialog
                            DialogFragment alertDialog = new ResetAlertFragment();
                            alertDialog.show(getParentFragmentManager(), "ResetAlertFragment");
                        }
                    mListener.onButtonLongPressed();
                }

                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        // get navigation option and default navigation mode from settings
        defNavOption = sharedPref.getString(Constants.NAVIGATION_OPTION, "");
        defDefNavMode = sharedPref.getString(Constants.DEFAULT_NAV_MODE, "");

        // get button saved state from memory
        buttonCurrentState.setButtonStatus(buttonCurrentState.getButtonStatus());

        // set buttons according to their saved states
        switch (buttonCurrentState.getButtonStatus()) {

            case OFFLINE:
                buttonCurrentState.setButtonOffline(requireContext());
                hideFloatingButton();
                break;

            case GETTING_LOCATION:
                if (!mapCurrentState.isNetworkEnabled() && !mapCurrentState.isGpsEnabled()) {
                    buttonCurrentState.setButtonStatus(ButtonStatus.OFFLINE);
                    buttonCurrentState.setButtonOffline(requireContext());
                } else {
                    buttonCurrentState.setButtonGetLocation(requireContext());
                }
                hideFloatingButton();
                break;

            case COME_BACK_HERE:
                buttonCurrentState.setButtonComeBack(requireContext());
                showFloatingButton();
                break;

            case GO_BACK:
                buttonCurrentState.setButtonGoBack(requireContext());
                showFloatingButton();
                break;

            case GO_BACK_CLICKED:
                buttonCurrentState.setButtonGoBackClicked(requireContext());
                showFloatingButton();
                break;

            case GO_BACK_OFFLINE:
                if (mapCurrentState.isNetworkEnabled() || mapCurrentState.isGpsEnabled()) {
                    buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK);
                    buttonCurrentState.setButtonGoBack(requireContext());
                } else {
                    buttonCurrentState.setButtonGoBackOffline(requireContext());
                }
                showFloatingButton();
                break;

        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // set that no location was get yet
        isFirstLocationGot.edit().putBoolean(Constants.MAP_FIRST_LOCATION, true).apply();

        // set that no address information was get yet
        isFirstAddressGot.edit().putBoolean(Constants.MAP_FIRST_ADDRESS, true).apply();
        addressFound = false;
        addressNotFound = false;

        // if default zoom level is high
        if (Objects.requireNonNull(sharedPref.getString(Constants.DEFAULT_ZOOM_LEVEL, getString(R.string.set_def_zoom_level_default)))
                .matches(getString(R.string.set_def_zoom_high))) {
            zoomLevel = Constants.MAP_HIGH_ZOOM_LEVEL;
        } else // if default zoom level is mid
            if (Objects.requireNonNull(sharedPref.getString(Constants.DEFAULT_ZOOM_LEVEL, getString(R.string.set_def_zoom_level_default)))
                    .matches(getString(R.string.set_def_zoom_mid))) {
                zoomLevel = Constants.MAP_MID_ZOOM_LEVEL;
            } else // if default zoom level is low
                if (Objects.requireNonNull(sharedPref.getString(Constants.DEFAULT_ZOOM_LEVEL, getString(R.string.set_def_zoom_level_default)))
                        .matches(getString(R.string.set_def_zoom_low))) {
                    zoomLevel = Constants.MAP_LOW_ZOOM_LEVEL;
                } else { // if default zoom level is auto
                    // set the map zoom level according to the default navigation mode
                    if (defDefNavMode.matches(getString(R.string.set_def_nav_mode_walk))) {
                        zoomLevel = Constants.MAP_HIGH_ZOOM_LEVEL;
                    } else if (defDefNavMode.matches(getString(R.string.set_def_nav_mode_drive))) {
                        zoomLevel = Constants.MAP_LOW_ZOOM_LEVEL;
                    } else {
                        zoomLevel = Constants.MAP_MID_ZOOM_LEVEL;
                    }
                }

        // go to the default location (0,0)
        mapCurrentState.gotoLocation(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE, 0);
        // disable "add to bookmarks" and "share" options menu item on main screen
        mListener.onUpdateMainMenuItemAddBookmarksState(false);
        mListener.onUpdateMainMenuItemShareState(false);
        // set reset/refresh action menu item to reset
        mListener.onUpdateMainMenuItemResetTitle(R.string.action_reset);
        // if button is green got to location set on map
        if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                > ButtonEnums.convertEnumToInt(ButtonStatus.COME_BACK_HERE)) {
            mapCurrentState.gotoLocation(mapCurrentState.getLatitude(), mapCurrentState.getLongitude(), zoomLevel);
            mapCurrentState.updateUI(mapCurrentState.getLatitude(), mapCurrentState.getLongitude());
            // enable "add to bookmarks" and "share" options menu item on main screen
            mListener.onUpdateMainMenuItemAddBookmarksState(true);
            mListener.onUpdateMainMenuItemShareState(true);
            // if notification is enabled start go back notification
            if (sharedPref.getBoolean(Constants.NOTIFICATION, true)) {
                notification.startGoBackNotification(requireContext());
            } else {
                notification.cancelNotification(Objects.requireNonNull(requireContext()), Constants.NOTIFICATION_ID);
            }
            // if is offline
            if (!mapCurrentState.isNetworkEnabled() && !mapCurrentState.isGpsEnabled()) {
                buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK_OFFLINE);
                buttonCurrentState.setButtonGoBackOffline(requireContext());
            }
            // show floating button
            showFloatingButton();

        } else
            // if is offline set the button to red
            if (!mapCurrentState.isNetworkEnabled() && !mapCurrentState.isGpsEnabled()) {
                buttonCurrentState.setButtonStatus(ButtonStatus.OFFLINE);
                buttonCurrentState.setButtonOffline(requireContext());
                hideFloatingButton();

            } else { // set the button to orange
                buttonCurrentState.setButtonStatus(ButtonStatus.GETTING_LOCATION);
                buttonCurrentState.setButtonGetLocation(requireContext());
                hideFloatingButton();
            }

        // request location updates
        mLocationRequest = createLocationRequest();

        // listen for location updates
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // update current location
                mapCurrentState.setLastLocation(location);
                mapCurrentState.setLatitude(location.getLatitude());
                mapCurrentState.setLongitude(location.getLongitude());

                // if this is the first location update
                if (isFirstLocationGot.getBoolean(Constants.MAP_FIRST_LOCATION, true)) {
                    // turn button YELLOW
                    buttonCurrentState.setButtonStatus(ButtonStatus.COME_BACK_HERE);
                    buttonCurrentState.setButtonComeBack(requireContext());
                    // set reset/refresh action menu item to refresh
                    mListener.onUpdateMainMenuItemResetTitle(R.string.action_refresh);
                    // set current location on map
                    mapCurrentState.gotoLocation(mapCurrentState.getLatitude(), mapCurrentState.getLongitude(), zoomLevel);
                    // show floating button
                    showFloatingButton();
                    // enable "add to bookmarks" and "share" options menu item on main screen
                    mListener.onUpdateMainMenuItemAddBookmarksState(true);
                    mListener.onUpdateMainMenuItemShareState(true);
                    // if geocoder is present show message of searching for address
                    if (Geocoder.isPresent()) {
                        toasts.showSearchAddress();
                    }
                    // register that the first update was already taken
                    isFirstLocationGot.edit().putBoolean(Constants.MAP_FIRST_LOCATION, false).apply();
                }

                // move red marker position to the current location
                mapCurrentState.updateUI(mapCurrentState.getLatitude(), mapCurrentState.getLongitude());

                // get address for the current location
                if (Geocoder.isPresent()) {
                    startIntentService(mapCurrentState.getLastLocation());
                } else {
                    MapCurrentState.setLocationAddress(Constants.MAP_NO_ADDRESS);
                }

                // show address found or not found message
                if (addressFound) {
                    toasts.setLocationAddressText(mapCurrentState.getLocationAddress());
                    toasts.showLocationAddress();
                    addressFound = false;
                }

                if (addressNotFound) {
                    toasts.setLocationAddressText(R.string.toast_no_address_found);
                    toasts.showLocationAddress();
                    addressNotFound = false;
                }

            }

        };

        // This code was extracted and modified from:
        // https://developer.android.com/training/location/change-location-settings.html

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        updateMap();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult()
                            // if button is not GREEN
                            if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                                    < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
                                hideFloatingButton();
                                stopLocationUpdates();
                                mapCurrentState.gotoLocation(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE, 0);
                                status.startResolutionForResult(
                                        requireActivity(),
                                        Constants.REQUEST_CHECK_SETTINGS);
                            } else { // Button is GREEN, so update map with saved location.
                                updateMap();
                            }

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        // if at least one location provider is available
                        // update map
                        if (mapCurrentState.isNetworkEnabled() || mapCurrentState.isGpsEnabled()) {
                            updateMap();
                        }
                        break;
                }

            }

        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        // hide floating button
        hideFloatingButton();
        // save map state on memory
        saveMapState();
        // stop requesting location updates
        stopLocationUpdates();
    }

    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapCurrentState.googleMap = googleMap;
        if (Objects.requireNonNull(sharedPref.getString(Constants.MAP_TYPE, getString(R.string.set_map_type_default)))
                .matches(getString(R.string.set_map_type_satellite))) {
            MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (Objects.requireNonNull(sharedPref.getString(Constants.MAP_TYPE, getString(R.string.set_map_type_default)))
                .matches(getString(R.string.set_map_type_hybrid))) {
            MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            MapCurrentState.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        MapCurrentState.mapMoved = false;
        MapCurrentState.googleMap.setOnCameraMoveStartedListener(this);
        MapCurrentState.googleMap.setOnCameraIdleListener(this);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            MapCurrentState.mapMoved = true;
        }
    }

    @Override
    public void onCameraIdle() {
        if (MapCurrentState.mapMoved) {
            MapCurrentState.mapMoved = false;
            mListener.onMapMoved();
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    // create location request with defined update interval and priority
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOC_REQ_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOC_REQ_FAST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void startLocationUpdates() {

        // if button is not GREEN, turn button ORANGE, disable "add to bookmarks"
        // options menu item on main screen and set default location (0,0) on map
        if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
            hideFloatingButton();
            mListener.onUpdateMainMenuItemAddBookmarksState(false);
            mListener.onUpdateMainMenuItemShareState(false);
            MapCurrentState.googleMap.clear();
            mapCurrentState.gotoLocation(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE, 0);
        }

        // register that the first update was not taken yet
        isFirstLocationGot.edit().putBoolean(Constants.MAP_FIRST_LOCATION, true).apply();

        // check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(requireContext()), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mLocationListener);
        } else {
            // check if user already denied permission request
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(requireActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                DialogFragment dialog = new NoLocPermissionAlertFragment();
                dialog.show(getParentFragmentManager(), "NoLocPermissionAlertFragment");
            } else {
                // request permissions
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.FINE_LOCATION_PERMISSION_REQUEST);
            }
        }

    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, mLocationListener);
        }
    }

    // start service that will get location address
    protected void startIntentService(Location lastLocation) {
        Intent intentAddress = new Intent(Objects.requireNonNull(requireActivity()).getApplicationContext(), FetchAddressIntentService.class);
        intentAddress.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);
        intentAddress.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, lastLocation);
        requireActivity().startService(intentAddress);
    }

    private void updateMap() {
        // if button is not GREEN, start location updates and hide floating button
        if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
            hideFloatingButton();
            startLocationUpdates();
        } else { // stop location updates, show floating button and set saved location on map
            stopLocationUpdates();
            showFloatingButton();
            mapCurrentState.gotoLocation(mapSavedState.getLatitude(), mapSavedState.getLongitude(), zoomLevel);
            mapCurrentState.updateUI(mapSavedState.getLatitude(), mapSavedState.getLongitude());
        }
    }

    // save current map state to memory
    public void saveMapState() {
        mapSavedState.setLocationStatus(mapCurrentState.getLatitude(), mapCurrentState.getLongitude(),
                mapCurrentState.getLocationAddress());
    }

    private void hideFloatingButton() {
        FloatingActionButton floatingButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        floatingButton.hide();
    }

    private void showFloatingButton() {

        // create floating button
        FloatingActionButton floatingButton = (FloatingActionButton) rootView.findViewById(R.id.fab);

        // listen for click events on floating button
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if vibrate feedback is enabled on settings, vibrate when button is clicked
                if ((vibrator.hasVibrator()) && (sharedPref.getBoolean(Constants.BUTTON_VIBRATE, true))) {
                    vibrator.vibrate(Constants.VIBRATE_SHORT_TIME);
                }
                mapCurrentState.gotoLocation(mapCurrentState.getLatitude(), mapCurrentState.getLongitude(), zoomLevel);
                mapCurrentState.updateUI(mapCurrentState.getLatitude(), mapCurrentState.getLongitude());
            }
        });

        floatingButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // check if button click action are enabled on settings
                if (sharedPref.getBoolean(Constants.BUTTON_CLICK_ACTIONS, true)) {
                    // if button is not GREEN reset the map
                    if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                            < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
                        // if vibrate feedback is enabled on settings, vibrate when button is clicked
                        if ((vibrator.hasVibrator()) && (sharedPref.getBoolean(Constants.BUTTON_VIBRATE, true))) {
                            vibrator.vibrate(Constants.VIBRATE_SHORT_TIME);
                        }
                        mListener.onReset();
                        mListener.onFloatingLongPressed();
                    }
                }

                return true;
            }
        });

        floatingButton.show();

    }

}

/*
 * Portions of this page are reproduced from work created and shared by the Android Open Source Project
 * and used according to terms described in the Creative Commons 2.5 Attribution License.
 *
 * Portions of this page are modifications based on work created and shared by the Android Open Source Project
 * and used according to terms described in the Creative Commons 2.5 Attribution License.
 */