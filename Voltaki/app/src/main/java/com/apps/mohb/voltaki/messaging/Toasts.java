/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Toasts.java
 *  Last modified : 9/29/20 12:29 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.messaging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.apps.mohb.voltaki.R;


// This class manages all the toasts in the application

public class Toasts {

    private static Toast backupBookmarks;
    private static Toast bookmarkAdded;
    private static Toast locationAddress;
    private static Toast searchAddress;

    private Context context;

    public Toasts(Context context) {
        this.context = context;
    }

// Toast to notify that a bookmarks were backed up

    @SuppressLint("ShowToast")
    public void createBackupBookmarks() {
        backupBookmarks = Toast.makeText((context), "", Toast.LENGTH_SHORT);
    }

    public void setBackupBookmarksText(String text) {
        backupBookmarks.setText(text);
    }

    public void showBackupBookmarks() {
        backupBookmarks.show();
    }

    public void cancelBackupBookmarks() {
        if (backupBookmarks != null) {
            backupBookmarks.cancel();
        }
    }


    // Toast to notify that a bookmark was added

    @SuppressLint("ShowToast")
    public void createBookmarkAdded() {
        bookmarkAdded = Toast.makeText((context), R.string.toast_added_bookmark, Toast.LENGTH_SHORT);
    }

    public void showBookmarkAdded() {
        bookmarkAdded.show();
    }

    public void cancelBookmarkAdded() {
        if (bookmarkAdded != null) {
            bookmarkAdded.cancel();
        }
    }

    // Toast to notify that an address has been found or not

    @SuppressLint("ShowToast")
    public void createLocationAddress() {
        locationAddress = Toast.makeText((context), "", Toast.LENGTH_SHORT);
    }

    public void setLocationAddressText(String text) {
        locationAddress.setText(text);
    }

    public void setLocationAddressText(int textId) {
        locationAddress.setText(textId);
    }


    public void showLocationAddress() {
        locationAddress.show();
    }

    public void cancelLocationAddress() {
        if (locationAddress != null) {
            locationAddress.cancel();
        }
    }

    // Toast to notify that is searching for address

    @SuppressLint("ShowToast")
    public void createSearchAddress() {
        searchAddress = Toast.makeText((context), R.string.toast_search_address, Toast.LENGTH_SHORT);
    }

    public void showSearchAddress() {
        searchAddress.show();
    }

    public void cancelSearchAddress() {
        if (searchAddress != null) {
            searchAddress.cancel();
        }
    }


    // Cancel all toasts

    public void cancelAllToasts() {
        cancelBackupBookmarks();
        cancelBookmarkAdded();
        cancelLocationAddress();
        cancelSearchAddress();
    }

}
