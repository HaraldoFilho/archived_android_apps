/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : BookmarksListAdapter.java
 *  Last modified : 9/29/20 12:29 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;

import java.util.List;


// Adapter to connect Bookmarks Array List to ListView

public class BookmarksListAdapter extends ArrayAdapter<LocationItem> {

    public BookmarksListAdapter(Context context, List<LocationItem> list) {
        super(context, Constants.LIST_ADAPTER_RESOURCE_ID, list);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LocationItem locationItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_bookmarks_item, parent, false);
        }

        TextView txtLocationName = convertView.findViewById(R.id.txtLocationName);
        TextView txtLocationAddress = convertView.findViewById(R.id.txtLocationAddress);

        assert locationItem != null;
        txtLocationName.setText(locationItem.getName());
        txtLocationAddress.setText(locationItem.getAddressText());

        return convertView;

    }
}
