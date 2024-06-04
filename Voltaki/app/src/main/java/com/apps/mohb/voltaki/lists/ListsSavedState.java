/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ListsSavedState.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.lists;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.apps.mohb.voltaki.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;


// This class manages the lists saved states

public class ListsSavedState {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;


    public ListsSavedState(Context context) {
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        this.context = context;
    }

    // save bookmarks list on memory through a json string
    public void setBookmarksState(ArrayList<LocationItem> bookmarks) throws IOException {
        String jsonBookmarks = writeJsonString(bookmarks);
        editor = preferences.edit();
        editor.putString(Constants.BOOKMARKS, jsonBookmarks);
        editor.apply();
    }

    public void setBookmarksState(String jsonBookmarks) {
        editor = preferences.edit();
        editor.putString(Constants.BOOKMARKS, jsonBookmarks);
        editor.apply();
    }

    // get bookmarks list from memory through a json string
    // if list was not saved yet creates a new array list
    public ArrayList<LocationItem> getBookmarksState() throws IOException {
        String jsonBookmarks = preferences.getString(Constants.BOOKMARKS, null);
        if (jsonBookmarks == null) {
            return new ArrayList<>();
        } else {
            return readJsonString(jsonBookmarks);
        }
    }

    // get a json string of bookmarks list from memory
    public String getBookmarksJsonState() {
        return preferences.getString(Constants.BOOKMARKS, "");
    }

    // save history list on memory through a json string
    public void setHistoryState(ArrayList<LocationItem> history) throws IOException {
        String jsonHistory = writeJsonString(history);
        editor.putString(Constants.HISTORY, jsonHistory);
        editor.commit();
    }

    // get history list from memory through a json string
    // if list was not saved yet creates a new array list
    public ArrayList<LocationItem> getHistoryState() throws IOException {
        String jsonHistory = preferences.getString(Constants.HISTORY, null);
        if (jsonHistory == null) {
            return new ArrayList<>();
        } else {
            return readJsonString(jsonHistory);
        }
    }

    // create a json string of a list of location items
    public String writeJsonString(ArrayList<LocationItem> locationItems) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");
        writeLocationsArrayList(jsonWriter, locationItems);
        jsonWriter.close();
        return stringWriter.toString();
    }

    // write all locations to json string
    public void writeLocationsArrayList(JsonWriter writer, ArrayList<LocationItem> locationItems) throws IOException {
        writer.beginArray();
        for (LocationItem locationItem : locationItems) {
            writeLocationItem(writer, locationItem);
        }
        writer.endArray();
    }

    // write a single location to json string
    public void writeLocationItem(JsonWriter writer, LocationItem locationItem) throws IOException {
        writer.beginObject();
        writer.name(Constants.JSON_NAME).value(locationItem.getName());
        writer.name(Constants.JSON_ADDRESS).value(locationItem.getAddress());
        writer.name(Constants.JSON_LATITUDE).value(locationItem.getLatitude());
        writer.name(Constants.JSON_LONGITUDE).value(locationItem.getLongitude());
        writer.endObject();
    }

    // read a json string containing a list of location items
    public ArrayList<LocationItem> readJsonString(String jsonString) throws IOException {
        try (JsonReader jsonReader = new JsonReader(new StringReader(jsonString))) {
            return readLocationsArrayList(jsonReader);
        }
    }

    // read a list of location items from a json string
    public ArrayList<LocationItem> readLocationsArrayList(JsonReader jsonReader) throws IOException {
        ArrayList<LocationItem> locationItems = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            locationItems.add(readLocationItem(jsonReader));
        }
        jsonReader.endArray();
        return locationItems;
    }

    // read a single location item from a json string
    public LocationItem readLocationItem(JsonReader jsonReader) throws IOException {
        String locationName = "";
        String locationAddress = "";
        double locationLatitude = Constants.DEFAULT_LATITUDE;
        double locationLongitude = Constants.DEFAULT_LONGITUDE;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case Constants.JSON_NAME:
                    locationName = jsonReader.nextString();
                    break;
                case Constants.JSON_ADDRESS:
                    locationAddress = jsonReader.nextString();
                    break;
                case Constants.JSON_LATITUDE:
                    locationLatitude = jsonReader.nextDouble();
                    break;
                case Constants.JSON_LONGITUDE:
                    locationLongitude = jsonReader.nextDouble();
                    break;
                default:
                    jsonReader.skipValue();
            }

        }
        jsonReader.endObject();
        return new LocationItem(this.context, locationName,
                locationAddress, locationLatitude, locationLongitude);
    }

}
