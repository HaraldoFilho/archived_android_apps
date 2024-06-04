/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : Lists.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.lists;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.messaging.Toasts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


// This class manages the Bookmarks and History lists

public class Lists {

    private static ArrayList<LocationItem> history;
    private static ArrayList<LocationItem> bookmarks;

    private int historyMaxItems;
    private static String bookmarkEditText;
    private static boolean editingAddress;

    private SharedPreferences sharedPref;
    private ListsSavedState listsSavedState;

    private Toasts toasts;


    public Lists(Context context) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        listsSavedState = new ListsSavedState(context);

        try { // get history list saved state
            history = listsSavedState.getHistoryState();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try { // get bookmarks list saved state
            bookmarks = listsSavedState.getBookmarksState();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get maximum number of history items from settings
        String maxItems = sharedPref.getString(Constants.HISTORY_MAX_ITEMS,
                context.getString(R.string.set_max_history_items_default));

        // if list is not unlimited, delete old items
        // that exceed the maximum number
        assert maxItems != null;
        if ((maxItems.matches(Constants.HISTORY_MAX_ITEMS_10))
                || (maxItems.matches(Constants.HISTORY_MAX_ITEMS_100))) {
            setHistoryMaxItems(Integer.parseInt(maxItems));
            pruneHistory();
        } else {
            historyMaxItems = Constants.UNLIMITED;
        }

        toasts = new Toasts(context);
        toasts.createBackupBookmarks();

    }

    public void saveState() {
        try {
            listsSavedState.setBookmarksState(bookmarks);
            listsSavedState.setHistoryState(history);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LocationItem> getHistory() {
        return history;
    }

    public ArrayList<LocationItem> getBookmarks() {
        return bookmarks;
    }

    public void sortBookmarks() {
        Collections.sort(bookmarks, new Comparator<LocationItem>() {
            @Override
            public int compare(LocationItem lhs, LocationItem rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        saveState();
    }

    public void addItemToHistory(LocationItem item) {
        // remove old items that exceed maximum number before add new item
        if ((historyMaxItems != Constants.UNLIMITED) && (getHistorySize() >= historyMaxItems)) {
            while (getHistorySize() > getHistoryMaxItems() - 1) {
                removeItemFromHistory(getHistorySize() - 1);
            }
        }
        history.add(Constants.LIST_HEAD, item);
        saveState();
    }

    public void addItemToBookmarks(Context context, LocationItem item) {
        bookmarks.add(Constants.LIST_HEAD, item);
        saveState();
        // check if bookmarks auto back is enabled
        if (sharedPref.getBoolean(Constants.AUTO_BACKUP, false)) {
            try {
                backupBookmarks(context, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateLocationName(int position) {
        bookmarks.get(position).setName(bookmarkEditText);
        saveState();
    }

    public void updateLocationAddress(int position) {
        if (!bookmarkEditText.isEmpty()) {
            bookmarks.get(position).setAddress(bookmarkEditText);
        }
        saveState();
    }

    public void removeItemFromHistory(int position) {
        history.remove(position);
        saveState();
    }

    public void removeItemFromBookmarks(int position) {
        bookmarks.remove(position);
        saveState();
    }

    public LocationItem getItemFromHistory(int position) {
        return history.get(position);
    }

    public LocationItem getItemFromBookmarks(int position) {
        return bookmarks.get(position);
    }

    public void clearHistory() {
        history.clear();
        saveState();
    }

    public void pruneHistory() {
        // remove old items that exceed maximum number
        if ((historyMaxItems != Constants.UNLIMITED) && (history.size() >= historyMaxItems)) {
            while (history.size() > historyMaxItems) {
                history.remove(history.size() - 1);
            }
        }
        saveState();
    }

    public int getHistorySize() {
        return history.size();
    }

    public boolean isHistoryEmpty() {
        return history.isEmpty();
    }

    public void setHistoryMaxItems(int maxItems) {
        historyMaxItems = maxItems;
    }

    public int getHistoryMaxItems() {
        return historyMaxItems;
    }

    public String getBookmarkEditText() {
        return bookmarkEditText;
    }

    public void setBookmarkEditText(String bookmarkEditText) {
        Lists.bookmarkEditText = bookmarkEditText;
    }

    public boolean isEditingAddress() {
        return editingAddress;
    }

    public void setEditingAddress(boolean value) {
        editingAddress = value;
    }

    public void backupBookmarks(Context context, boolean auto) throws IOException {

        // check if it is possible to write on external storage
        if (isExternalStorageWritable()) {
            // if backup directory doesn't exist create it
            if (!getBackupDirectory(context).exists()) {
                getBackupDirectory(context).mkdir();
            }
            // full path backup file name
            File backupFile = new File(getBackupDirectory(context) + "/" + Constants.BOOKMARKS_BACKUP_FILE);
            // if file already exists delete it
            if (backupFile.exists()) {
                backupFile.delete();
            }

            // check if file is created successfully
            if (backupFile.createNewFile()) {
                // get json string from memory
                String jsonString = listsSavedState.getBookmarksJsonState();
                // write json string to backup file
                writeStringToFile(jsonString, backupFile);
                // if auto backup is not enabled show toast with confirmation and the backup file full path
                if (!auto) {
                    toasts.setBackupBookmarksText(context.getString(R.string.toast_backup_bookmarks) + backupFile.toString());
                    toasts.showBackupBookmarks();
                }
            }

        } else { // if auto backup is not enabled show toast informing that external storage is unavailable
            if (!auto) {
                toasts.setBackupBookmarksText(context.getString(R.string.toast_store_unavailable));
                toasts.showBackupBookmarks();
            }
        }
    }

    public void restoreBookmarks(Context context) throws IOException {
        // check if it is possible to read from external storage
        if (isExternalStorageReadable()) {
            // check if backup directory exists
            if ((getBackupDirectory(context).exists())) {
                // full path backup file name
                File backupFile = new File(getBackupDirectory(context) + "/" + Constants.BOOKMARKS_BACKUP_FILE);
                // check if backup file exists
                if (backupFile.exists()) {
                    toasts.setBackupBookmarksText(context.getString(R.string.toast_restore_bookmarks) + backupFile.toString());
                    toasts.showBackupBookmarks();
                    String jsonString = readStringFromFile(backupFile);
                    // save json bookmarks list in memory
                    listsSavedState.setBookmarksState(jsonString);
                    // get bookmarks list from memory
                    bookmarks = listsSavedState.getBookmarksState();
                } else { // show toast informing that backup file has not been found
                    toasts.setBackupBookmarksText(context.getString(R.string.toast_file_not_found));
                    toasts.showBackupBookmarks();
                }
            } else { // show toast informing that backup file has not been found
                toasts.setBackupBookmarksText(context.getString(R.string.toast_file_not_found));
                toasts.showBackupBookmarks();
            }
        } else { // show toast informing that external storage is unavailable
            toasts.setBackupBookmarksText(context.getString(R.string.toast_store_unavailable));
            toasts.showBackupBookmarks();

        }
    }

    private void writeStringToFile(String string, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        // write string to file
        fileWriter.write(string);
        fileWriter.close();
    }

    private String readStringFromFile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        int fileLength = (int) file.length();
        char[] charArray = new char[fileLength];
        // read file and put data in the array of characters
        fileReader.read(charArray, 0, fileLength);
        fileReader.close();
        // convert the array of characters into a string and return it
        return String.valueOf(charArray);
    }

    private File getBackupDirectory(Context context) {
        return new File(String.valueOf(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS)));
    }


    // These two methods were extracted from:
    // https://developer.android.com/guide/topics/data/data-storage.html#filesExternal

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}

/*
 * Portions of this page are reproduced from work created and shared by the Android Open Source Project
 * and used according to terms described in the Creative Commons 2.5 Attribution License.
 */