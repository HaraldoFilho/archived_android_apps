/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MainActivity.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonEnums;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.apps.mohb.voltaki.fragments.MainFragment;
import com.apps.mohb.voltaki.fragments.NoMapsFragment;
import com.apps.mohb.voltaki.fragments.NoPlayServicesFragment;
import com.apps.mohb.voltaki.fragments.dialogs.BookmarkEditDialogFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ButtonAddBookmarkTipAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ButtonRefreshTipAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ButtonResetTipAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.FloatingButtonTipAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.LocServDisabledAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.MapsNotInstalledAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.NoLocPermissionAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ResetAlertFragment;
import com.apps.mohb.voltaki.lists.Lists;
import com.apps.mohb.voltaki.lists.LocationItem;
import com.apps.mohb.voltaki.map.MapCurrentState;
import com.apps.mohb.voltaki.messaging.GoBackNotificationActivity;
import com.apps.mohb.voltaki.messaging.Notification;
import com.apps.mohb.voltaki.messaging.Toasts;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        ResetAlertFragment.ResetDialogListener,
        BookmarkEditDialogFragment.BookmarkEditDialogListener,
        MapsNotInstalledAlertFragment.MapsNotInstalledAlertDialogListener,
        LocServDisabledAlertFragment.LocServDisabledDialogListener,
        NoLocPermissionAlertFragment.NoLocPermissionDialogListener,
        ButtonRefreshTipAlertFragment.ButtonRefreshTipDialogListener,
        ButtonResetTipAlertFragment.ButtonResetTipDialogListener,
        ButtonAddBookmarkTipAlertFragment.ButtonAddBookmarkTipDialogListener,
        FloatingButtonTipAlertFragment.FloatingButtonTipDialogListener {

    private DrawerLayout drawer;
    private static MenuItem menuItemReset;
    private static MenuItem menuItemAddBookmark;
    private static MenuItem menuItemShare;

    private Fragment mainFragment;
    private FragmentManager mainFragmentManager;

    private MapCurrentState mapCurrentState;
    private ButtonCurrentState buttonCurrentState;

    private SharedPreferences showRefreshTipPref;
    private SharedPreferences showResetTipPref;
    private SharedPreferences showFloatingButtonTipPref;
    private SharedPreferences showAddBookmarkPref;
    private SharedPreferences showNoLocServWarnPref;

    private boolean okPlayServices;
    private boolean okMaps;

    private Lists lists;
    private Toasts toasts;

    private DialogFragment locServDisabledDialog;

    protected GoogleApiAvailability googleApiAvailability;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// check if Google API is available
        checkGoogleAppsApiAvailability();

        // check if Google Maps or Google Play Services is not installed on device
        if ((!okPlayServices) || (!okMaps)) {

            // if Play Service is not installed show warning message on screen
            if (!okPlayServices) {
                NoPlayServicesFragment fragment = new NoPlayServicesFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            } else { // show Maps not installed warning message
                NoMapsFragment fragment = new NoMapsFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            }

            // do to not show main screen
            return;
        }

        // create app toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create toasts instance
        toasts = new Toasts(getApplicationContext());

        // create navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // if drawer is opened cancel all toasts
                toasts.cancelAllToasts();
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // create items on navigation drawer and listen for clicks
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // create instance of map current state
        mapCurrentState = new MapCurrentState(this);
        // create instance of button current state
        buttonCurrentState = new ButtonCurrentState(this);

        // get shared preferences_old variables
        showRefreshTipPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        showResetTipPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        showFloatingButtonTipPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        showAddBookmarkPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        showNoLocServWarnPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);

        // get system language
        String systemLanguage = Locale.getDefault().getLanguage().toString();
        // get last system language set on device
        SharedPreferences lastSystemLanguagePref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        String lastSystemLanguage = lastSystemLanguagePref.getString(Constants.SYSTEM_LANGUAGE, "");

        assert lastSystemLanguage != null;
        if (!systemLanguage.matches(lastSystemLanguage)) {
            // if system language has changed, clear settings because they changed to the new language
            lastSystemLanguagePref.edit().putString(Constants.SYSTEM_LANGUAGE, systemLanguage).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
            // and update notification if button is green
            if ((buttonCurrentState.getButtonStatus() == ButtonStatus.GO_BACK)
                    || (buttonCurrentState.getButtonStatus() == ButtonStatus.GO_BACK_CLICKED)
                    || (buttonCurrentState.getButtonStatus() == ButtonStatus.GO_BACK_OFFLINE)) {
                // intent that will open Google Maps when notification is clicked
                Intent intent = new Intent(this, GoBackNotificationActivity.class);
                Notification notification = new Notification();
                notification.createNotificationChannel(this);
                // update notification
                notification.cancelNotification(this, Constants.NOTIFICATION_ID);
                notification.startNotification(intent, this, getString(R.string.info_app_name),
                        getString(R.string.notification_go_back), Constants.NOTIFICATION_ID);
            }
        }

        // if language has changed set user preferences_old to default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // create instance of bookmarks and history lists
        lists = new Lists(this);

        // set context for all toasts and create added to bookmarks toast
        toasts.createBookmarkAdded();

        // create location services disabled dialog
        if (locServDisabledDialog == null) {
            locServDisabledDialog = new LocServDisabledAlertFragment();
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        // if Google Play Services or Google Maps is not available stop execution
        if ((!okPlayServices) || (!okMaps)) {
            return;
        }

        // check if all location providers are disabled
        if (!mapCurrentState.isNetworkEnabled() && !mapCurrentState.isGpsEnabled()) {
            // if location services dialog is enabled in preferences_old show it
            if (showNoLocServWarnPref.getBoolean(Constants.LOC_SERV_CHECK, true)) {
                locServDisabledDialog.setCancelable(false);
                locServDisabledDialog.show(getSupportFragmentManager(), "LocServDisabledAlertFragment");
            } else { // create map and button
                createMainFragment();
            }
        } else { // create map and button
            createMainFragment();
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        // if Google Play Services or Google Maps is not available stop execution
        if ((!okPlayServices) || (!okMaps)) {
            return;
        }

        // if at least one of the location providers is enabled cancel
        // location services disabled dialog
        if (mapCurrentState.isGpsEnabled() || mapCurrentState.isNetworkEnabled()) {
            Dialog dialogLocServ = locServDisabledDialog.getDialog();
            if (dialogLocServ != null) {
                dialogLocServ.setCancelable(true);
                dialogLocServ.cancel();
            }
        }

        // set title of refresh/reset action menu item
        try {
            updateActionResetTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {
        super.onAttachFragment(fragment);

    }

    public void createMainFragment() {

        if (mainFragment == null) {
            // if all location providers are disabled and button is not GREEN set button to RED
            if (!mapCurrentState.isNetworkEnabled() && !mapCurrentState.isGpsEnabled()
                    && (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                    < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK))) {
                buttonCurrentState.setButtonStatus(ButtonStatus.OFFLINE);
            }
            // create map and button
            mainFragment = new MainFragment();
            mainFragmentManager = getSupportFragmentManager();
            mainFragmentManager.beginTransaction().replace(R.id.container, mainFragment).commit();
        }

    }


    // OPTIONS MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // if Google Play Services or Google Maps is not available stop execution
        // and do not create menu
        if ((!okPlayServices) || (!okMaps)) {
            return false;
        }

        getMenuInflater().inflate(R.menu.main, menu);

        // create "add to bookmarks" menu item
        menuItemAddBookmark = menu.findItem(R.id.action_add_bookmark);

        // create "share" menu item
        menuItemShare = menu.findItem(R.id.action_share);

        if (buttonCurrentState.getButtonStatus() == ButtonStatus.OFFLINE) {
            menuItemAddBookmark.setEnabled(false);
            menuItemShare.setEnabled(false);
        }

        // create reset menu item and set text according to button state
        menuItemReset = menu.findItem(R.id.action_reset);
        updateActionResetTitle();

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            // Add to bookmarks
            case R.id.action_add_bookmark:
                if (showAddBookmarkPref.getBoolean(Constants.BUTTON_ADD_BOOKMARK_TIP_SHOW, true)) {
                    DialogFragment tipDialog = new ButtonAddBookmarkTipAlertFragment();
                    tipDialog.show(getSupportFragmentManager(), "ButtonAddBookmarkTipAlertFragment");
                }
                lists.setBookmarkEditText("");
                DialogFragment dialog = new BookmarkEditDialogFragment();
                dialog.show(getSupportFragmentManager(), "BookmarkEditDialogFragment");
                break;

            // Share
            case R.id.action_share:
                LocationItem locationItem = new LocationItem(this);
                if ((buttonCurrentState.getButtonStatus() == ButtonStatus.COME_BACK_HERE)) {
                    locationItem.setName(getString(R.string.action_share_here));
                } else {
                    locationItem.setName(getString(R.string.action_share_location));
                }
                locationItem.setLatitude(mapCurrentState.getLatitude());
                locationItem.setLongitude(mapCurrentState.getLongitude());
                locationItem.setAddress(mapCurrentState.getLocationAddress());
                locationItem.share();
                break;

            // Reset / Refresh
            case R.id.action_reset:
                // if button is YELLOW show map refresh tip dialog
                if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                        == ButtonEnums.convertEnumToInt(ButtonStatus.COME_BACK_HERE)) {
                    if (showRefreshTipPref.getBoolean(Constants.BUTTON_REFRESH_TIP_SHOW, true)) {
                        DialogFragment tipDialog = new ButtonRefreshTipAlertFragment();
                        tipDialog.show(getSupportFragmentManager(), "ButtonRefreshTipAlertFragment");
                    }
                    reset();
                } else { // if it is GREEN or RED show reset tip dialog
                    if (showResetTipPref.getBoolean(Constants.BUTTON_RESET_TIP_SHOW, true)) {
                        DialogFragment tipDialog = new ButtonResetTipAlertFragment();
                        tipDialog.show(getSupportFragmentManager(), "ButtonResetTipAlertFragment");
                    }
                    // if it is RED reset
                    if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                            == ButtonEnums.convertEnumToInt(ButtonStatus.OFFLINE)) {
                        reset();
                    } else { // if it is GREEN show reset dialog
                        DialogFragment alertDialog = new ResetAlertFragment();
                        alertDialog.show(getSupportFragmentManager(), "ResetAlertFragment");
                    }
                }
                break;

            // Help
            case R.id.action_help_main:
                Intent intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", getString(R.string.url_help_main));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);

    }


    /// NAVIGATION DRAWER MENU

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        Intent intent = null;

        switch (id) {

            // Bookmarks
            case R.id.nav_bookmarks:
                intent = new Intent(this, BookmarksActivity.class);
                break;

            // History
            case R.id.nav_history:
                intent = new Intent(this, HistoryActivity.class);
                break;

            // Settings
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;

            // Help
            case R.id.nav_help:
                intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", getString(R.string.url_help_index));
                intent.putExtras(bundle);
                break;

            // About
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
        }

        // go to item clicked
        startActivity(intent);

        // close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    @Override
    public void onBackPressed() {

        // if Google Play Services or Google Maps is not available close the application
        if ((!okPlayServices) || (!okMaps)) {
            super.onBackPressed();
            return;
        }

        // if navigation drawer is opened close it
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else { // cancel all toasts and close application
            toasts.cancelAllToasts();
            super.onBackPressed();
        }

    }

    // Reset
    public void reset() {

        // if at least one location provider is enabled set button to GETTING LOCATION
        if (mapCurrentState.isGpsEnabled() || mapCurrentState.isNetworkEnabled()) {
            buttonCurrentState.setButtonStatus(ButtonStatus.GETTING_LOCATION);
            buttonCurrentState.setButtonGetLocation(this);
            buttonCurrentState.setButtonStatus(ButtonStatus.GETTING_LOCATION);
        } else { // set the button to OFFLINE
            buttonCurrentState.setButtonStatus(ButtonStatus.OFFLINE);
            buttonCurrentState.setButtonOffline(this);
            buttonCurrentState.setButtonStatus(ButtonStatus.OFFLINE);
        }

        // cancel status bar notification
        Notification notification = new Notification();
        notification.cancelNotification(this, Constants.NOTIFICATION_ID);

        // set location to 0,0 with zoom=0
        mapCurrentState.setLatitude(Constants.DEFAULT_LATITUDE);
        mapCurrentState.setLatitude(Constants.DEFAULT_LONGITUDE);
        mapCurrentState.gotoLocation(mapCurrentState.getLatitude(), mapCurrentState.getLongitude(), 0);

        // restart Google Map API client
        MainFragment fragment = (MainFragment) mainFragmentManager.findFragmentById(R.id.container);
        assert fragment != null;
        fragment.getGoogleApiClient().disconnect();
        fragment.getGoogleApiClient().connect();

        // change reset menu item
        updateActionResetTitle();

    }


    // update text of reset item on options menu
    public void updateActionResetTitle() {
        if ((buttonCurrentState.getButtonStatus() == ButtonStatus.GETTING_LOCATION)
                || (buttonCurrentState.getButtonStatus() == ButtonStatus.COME_BACK_HERE)) {
            menuItemReset.setTitle(R.string.action_refresh);
        } else {
            menuItemReset.setTitle(R.string.action_reset);
        }
    }


    public void startLocationUpdates() {
        MainFragment fragment = (MainFragment) mainFragmentManager.findFragmentById(R.id.container);
        assert fragment != null;
        fragment.startLocationUpdates();
    }

    private void checkGoogleAppsApiAvailability() {

        int googlePlayServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        // Check if Google Play Services is installed
        if (googlePlayServicesAvailability == ConnectionResult.SUCCESS) {

            okPlayServices = true;

            try {
                // Checks is Google Maps is installed
                if (getPackageManager().getApplicationInfo("com.google.android.gms", 0).enabled) {
                    okMaps = true;
                } else {
                    okMaps = false;
                    // and ask user to install Maps
                    DialogFragment dialog = new MapsNotInstalledAlertFragment();
                    dialog.show(getSupportFragmentManager(), "MapsNotInstalledAlertFragment");
                }

            } catch (PackageManager.NameNotFoundException e) {
                // if not trows exception
                e.printStackTrace();

            }

        } else {

            okPlayServices = false;
            // shows error dialog to ask user to install Play Services
            GoogleApiAvailability.getInstance().getErrorDialog(this, googlePlayServicesAvailability, 0).show();

        }
    }


    @Override // System location settings dialog result
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {

                // user choose to turn on location provider that was off
                case Activity.RESULT_OK:
                    // if button is not green reset map
                    if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                            < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
                        reset();
                    }
                    break;
                // user choose to not turn on location provider that is off
                case Activity.RESULT_CANCELED:
                    // if at least one location provider is available start location updates
                    if (mapCurrentState.isGpsEnabled() || mapCurrentState.isNetworkEnabled()) {
                        startLocationUpdates();
                    }
                    break;
            }
        }
    }


    // MAIN FRAGMENT INTERACTION METHODS

    @Override // reset
    public void onReset() {
        reset();
    }

    @Override // update text of reset menu item on options menu
    public void onUpdateMainMenuItemResetTitle(int stringId) {
        menuItemReset.setTitle(stringId);
    }

    @Override // update state of add bookmark menu item on options menu
    public void onUpdateMainMenuItemAddBookmarksState(boolean state) {
        menuItemAddBookmark.setEnabled(state);
    }

    @Override // update state of share menu item on options menu
    public void onUpdateMainMenuItemShareState(boolean state) {
        menuItemShare.setEnabled(state);
    }

    @Override // show tip when map is moved
    public void onMapMoved() {
        if (showFloatingButtonTipPref.getBoolean(Constants.FLOATING_BUTTON_TIP_SHOW, true)) {
            DialogFragment tipDialog = new FloatingButtonTipAlertFragment();
            tipDialog.show(getSupportFragmentManager(), "FloatingButtonTipAlertFragment");
        }
    }

    @Override // disable tip when button is long pressed
    public void onButtonLongPressed() {
        if (buttonCurrentState.getButtonStatus() == ButtonStatus.COME_BACK_HERE) {
            showAddBookmarkPref.edit().putBoolean(Constants.BUTTON_ADD_BOOKMARK_TIP_SHOW, false).apply();
        } else {
            showResetTipPref.edit().putBoolean(Constants.BUTTON_RESET_TIP_SHOW, false).apply();
        }
    }

    @Override // disable tip when floating button is long pressed
    public void onFloatingLongPressed() {
        showRefreshTipPref.edit().putBoolean(Constants.BUTTON_REFRESH_TIP_SHOW, false).apply();
    }

    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST) {// if permission is granted reset
            if (grantResults.length > 0
                    && ((grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                reset();
            }
        }
    }


    // LOCATION REQUEST DIALOG

    @Override  // Yes
    public void onAlertNoLocPermDialogPositiveClick(DialogFragment dialog) {
        // request permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                Constants.FINE_LOCATION_PERMISSION_REQUEST);

    }

    @Override // No
    public void onAlertNoLocPermDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // MAPS ALERT DIALOG

    @Override // Do not show again
    public void onMapsAlertDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
        startActivity(intent);
        finish();
    }


    // BOOKMARK EDIT DIALOG

    @Override // Ok
    public void onBookmarkEditDialogPositiveClick(DialogFragment dialog) {
        LocationItem locationItem;

        try {
            // create location item with current latitude and longitude
            locationItem = new LocationItem(this);
            locationItem.setLatitude(mapCurrentState.getLatitude());
            locationItem.setLongitude(mapCurrentState.getLongitude());

            // if a location name was entered in the dialog set it as location name
            if (!lists.getBookmarkEditText().isEmpty()) {
                locationItem.setName(lists.getBookmarkEditText());
            } else { // set current date and time as location name
                locationItem.setTimeAsName();
            }

            locationItem.setAddress(mapCurrentState.getLocationAddress());

            // add item to bookmarks list and show toast
            lists.addItemToBookmarks(this, locationItem);
            toasts.showBookmarkAdded();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override // Cancel
    public void onBookmarkEditDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // RESET DIALOG

    @Override // Yes
    public void onResetDialogPositiveClick(DialogFragment dialog) {
        reset();
    }

    @Override // No
    public void onResetDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // LOCATION SERVICES DISABLED DIALOG

    @Override // Yes
    public void onAlertLocServDialogPositiveClick(DialogFragment dialog) {
        // open locations settings
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override // No
    public void onAlertLocServDialogNegativeClick(DialogFragment dialog) {
        createMainFragment();
    }

    @Override // Do not show again
    public void onAlertLocServDialogNeutralClick(DialogFragment dialog) {
        showNoLocServWarnPref.edit().putBoolean(Constants.LOC_SERV_CHECK, false).apply();
        createMainFragment();
    }


    // BUTTON ADD BOOKMARK TIP DIALOG

    @Override // Do not show again
    public void onButtonAddBookmarkTipDialogPositiveClick(DialogFragment dialog) {
        showAddBookmarkPref.edit().putBoolean(Constants.BUTTON_ADD_BOOKMARK_TIP_SHOW, false).apply();
    }


    // BUTTON REFRESH TIP DIALOG

    @Override // Do not show again
    public void onButtonRefreshTipDialogPositiveClick(DialogFragment dialog) {
        showRefreshTipPref.edit().putBoolean(Constants.BUTTON_REFRESH_TIP_SHOW, false).apply();
    }


    // BUTTON RESET TIP DIALOG

    @Override
    public void onButtonResetTipDialogPositiveClick(DialogFragment dialog) {
        showResetTipPref.edit().putBoolean(Constants.BUTTON_RESET_TIP_SHOW, false).apply();
    }


    // FLOATING BUTTON TIP DIALOG

    @Override
    public void onFloatingButtonTipDialogPositiveClick(DialogFragment dialog) {
        showFloatingButtonTipPref.edit().putBoolean(Constants.FLOATING_BUTTON_TIP_SHOW, false).apply();
    }


}