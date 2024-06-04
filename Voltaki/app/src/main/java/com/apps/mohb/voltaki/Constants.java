/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Constants.java
 *  Last modified : 9/29/20 1:16 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki;


public class Constants {

    // App Shared Preferences
    public static final int PRIVATE_MODE = 0;
    public static final String PREF_NAME = "Voltaki";

    // Settings preferences_old
    public static final String MAP_TYPE = "MAP_TYPE";
    public static final String NAVIGATION_OPTION = "NAVIGATION_OPTION";
    public static final String DEFAULT_NAV_MODE = "DEFAULT_NAV_MODE";
    public static final String DEFAULT_ZOOM_LEVEL = "DEFAULT_ZOOM_LEVEL";
    public static final String BUTTON_VIBRATE = "BUTTON_VIBRATE";
    public static final String BUTTON_CLICK_ACTIONS = "BUTTON_CLICK_ACTIONS";
    public static final String HISTORY_MAX_ITEMS = "HISTORY_MAX_ITEMS";
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String AUTO_BACKUP = "AUTO_BACKUP";
    public static final String LOC_SERV_CHECK = "LOC_SERV_CHECK";

    public static final String HISTORY_MAX_ITEMS_10 = "10";
    public static final String HISTORY_MAX_ITEMS_100 = "100";

    // System language
    public static final String SYSTEM_LANGUAGE = "SYSTEM_LANGUAGE";
    public static final String LANGUAGE_PORTUGUESE = "pt";

    // Permissions request
    public static final int FINE_LOCATION_PERMISSION_REQUEST = 0;
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;

    // Log
    public static final String LOG_TAG = "DEBUG_VOLTAKI";

    // Button saved preferences_old
    public static final String BUTTON_STATUS = "ButtonStatus";
    public static final int DEFAULT_BUTTON_STATUS = 0;
    public static final String BUTTON_RESET_TIP_SHOW = "ButtonResetTipShow";
    public static final String BUTTON_REFRESH_TIP_SHOW = "ButtonRefreshTipShow";
    public static final String BUTTON_ADD_BOOKMARK_TIP_SHOW = "ButtonAddBookmarkTipShow";
    public static final String FLOATING_BUTTON_TIP_SHOW = "FloatingButtonTipShow";

    // Map Saved Preferences
    public static final String MAP_FIRST_LOCATION = "MapFirstLocation";
    public static final String MAP_FIRST_ADDRESS = "MapFirstAddress";
    public static final String MAP_LATITUDE = "MapLatitude";
    public static final String MAP_LONGITUDE = "MapLongitude";
    public static final String MAP_ADDRESS = "MapAddress";
    public static final String MAP_NO_ADDRESS = "NoAddressFound";

    // Lists Saved Preferences
    public static final String BOOKMARKS = "Bookmarks";
    public static final String HISTORY = "History";
    public static final String LISTS_TIP_SHOW = "ListsTipShow";
    public static final String JSON_NAME = "name";
    public static final String JSON_ADDRESS = "address";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String BOOKMARKS_BACKUP_FILE = "bookmarks.json";

    // Location
    public static final int MAP_HIGH_ZOOM_LEVEL = 16;
    public static final int MAP_MID_ZOOM_LEVEL = 14;
    public static final int MAP_LOW_ZOOM_LEVEL = 13;

    public static final int LAT_LNG_MAX_LENGTH = 15;

    public static final double DEFAULT_LATITUDE = 0;
    public static final double DEFAULT_LONGITUDE = 0;

    public static final int LOC_REQ_INTERVAL = 1000;
    public static final int LOC_REQ_FAST_INTERVAL = 500;

    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Lists
    public static final int LIST_ADAPTER_RESOURCE_ID = 0;
    public static final int LIST_HEAD = 0;
    public static final int STRING_HEAD = 0;
    public static final int UNLIMITED = 0;

    // Text
    public static final int TEXT_LARGE = 20;
    public static final String SPACE = " ";

    // Feedback and question
    public static final int QUESTION_ARRAY_SIZE = 1;

    // Vibrator
    public static final int VIBRATE_SHORT_TIME = 30;
    public static final int VIBRATE_LONG_TIME = 70;

    // Notification
    public static final int NOTIFICATION_ID = 0;
    public static final String CHANNEL_ID = "NOTIFY";
    public static final int INTENT_REQUEST_CODE = 0;

}