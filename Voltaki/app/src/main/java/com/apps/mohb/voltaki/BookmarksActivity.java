/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : BookmarksActivity.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.voltaki.button.ButtonCurrentState;
import com.apps.mohb.voltaki.button.ButtonEnums;
import com.apps.mohb.voltaki.button.ButtonStatus;
import com.apps.mohb.voltaki.fragments.dialogs.BackupAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.BookmarkEditDialogFragment;
import com.apps.mohb.voltaki.fragments.dialogs.BookmarksSortAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ExternalStoragePermissionsAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ItemDeleteAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ListsTipAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.ReplaceLocationAlertFragment;
import com.apps.mohb.voltaki.fragments.dialogs.RestoreAlertFragment;
import com.apps.mohb.voltaki.lists.BookmarksListAdapter;
import com.apps.mohb.voltaki.lists.Lists;
import com.apps.mohb.voltaki.lists.LocationItem;
import com.apps.mohb.voltaki.map.MapSavedState;

import java.io.IOException;
import java.util.Objects;


public class BookmarksActivity extends AppCompatActivity implements
        BookmarkEditDialogFragment.BookmarkEditDialogListener,
        BackupAlertFragment.BackupDialogListener,
        RestoreAlertFragment.RestoreDialogListener,
        ItemDeleteAlertFragment.ItemDeleteDialogListener,
        ListsTipAlertFragment.ListsTipDialogListener,
        ReplaceLocationAlertFragment.ReplaceLocationDialogListener,
        BookmarksSortAlertFragment.BookmarksSortAlertDialogListener,
        ExternalStoragePermissionsAlertFragment.ExternalStoragePermissionsDialogListener {

    private MapSavedState mapSavedState;
    private ButtonCurrentState buttonCurrentState;

    private SharedPreferences showTipPref;

    private Lists bookmarksList;
    private ListView bookmarksListView;
    private BookmarksListAdapter bookmarksAdapter;

    private AdapterView.AdapterContextMenuInfo menuInfo;

    private static MenuItem menuItemBackup;
    private static MenuItem menuItemRestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // initialize state variables
        mapSavedState = new MapSavedState(getApplicationContext());
        buttonCurrentState = new ButtonCurrentState(getApplicationContext());

        // create bookmarks list
        bookmarksList = new Lists(getApplicationContext());
        bookmarksAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarksList.getBookmarks());
        bookmarksListView = (ListView) findViewById(R.id.listBookmarks);
        bookmarksListView.setAdapter(bookmarksAdapter);

        // menu shown when a list item is long clicked
        registerForContextMenu(bookmarksListView);

        // handle clicks on list items
        bookmarksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // if a location is not already set on map, set the selected location
                if (ButtonEnums.convertEnumToInt(buttonCurrentState.getButtonStatus())
                        < ButtonEnums.convertEnumToInt(ButtonStatus.GO_BACK)) {
                    setBookmarkItemOnMap(position);
                } else { // show dialog asking if wish to replace the location
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    DialogFragment dialog = new ReplaceLocationAlertFragment();
                    dialog.setArguments(bundle);
                    dialog.show(getSupportFragmentManager(), "ReplaceLocationAlertFragment");
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // show tip if it hasn't shown before and list is not empty
        showTipPref = this.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
        if ((showTipPref.getBoolean(Constants.LISTS_TIP_SHOW, true)) && (!bookmarksList.getBookmarks().isEmpty())) {
            DialogFragment dialog = new ListsTipAlertFragment();
            dialog.show(getSupportFragmentManager(), "ListsTipAlertFragment");
        }

        // check permissions to access external storage
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            // check if user already denied permission request
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                DialogFragment dialog = new ExternalStoragePermissionsAlertFragment();
                dialog.show(getSupportFragmentManager(), "ExternalStoragePermissionsAlertFragment");
            } else {
                // request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST);
            }
        }

    }


    // OPTIONS MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmarks, menu);

        menuItemBackup = menu.findItem(R.id.action_backup_bookmarks);
        menuItemRestore = menu.findItem(R.id.action_restore_bookmarks);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            menuItemBackup.setEnabled(true);
            menuItemRestore.setEnabled(true);
        } else {
            menuItemBackup.setEnabled(false);
            menuItemRestore.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            // Sort
            case R.id.action_sort_bookmarks:
                if (bookmarksList.getBookmarks().size() > 1) {
                    DialogFragment sortAlert = new BookmarksSortAlertFragment();
                    sortAlert.show(getSupportFragmentManager(), "BookmarksSortAlertFragment");
                }
                break;

            // Backup
            case R.id.action_backup_bookmarks:
                DialogFragment backupAlert = new BackupAlertFragment();
                backupAlert.show(getSupportFragmentManager(), "BackupAlertFragment");
                break;

            // Restore
            case R.id.action_restore_bookmarks:
                DialogFragment restoreAlert = new RestoreAlertFragment();
                restoreAlert.show(getSupportFragmentManager(), "RestoreAlertFragment");
                break;

            // Help
            case R.id.action_help_bookmarks:
                Intent intent = new Intent(this, HelpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", getString(R.string.url_help_bookmarks));
                intent.putExtras(bundle);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    // CONTEXT MENU

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookmarks_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        LocationItem locationItem;
        DialogFragment dialog;

        switch (item.getItemId()) {

            // Share
            case R.id.share:
                LocationItem bookmarksItem = bookmarksList.getItemFromBookmarks(menuInfo.position);
                locationItem = new LocationItem(this, bookmarksItem.getName(), bookmarksItem.getAddress(),
                        bookmarksItem.getLatitude(), bookmarksItem.getLongitude());
                locationItem.share();
                return true;

            // Edit name
            case R.id.editName:
                locationItem = bookmarksList.getItemFromBookmarks(menuInfo.position);
                bookmarksList.setBookmarkEditText(locationItem.getName());
                bookmarksList.setEditingAddress(false);
                dialog = new BookmarkEditDialogFragment();
                dialog.show(getSupportFragmentManager(), "BookmarkEditDialogFragment");
                return true;

            // Edit address
            case R.id.editAddress:
                locationItem = bookmarksList.getItemFromBookmarks(menuInfo.position);
                if (!locationItem.getAddress().matches(Constants.MAP_NO_ADDRESS)) {
                    bookmarksList.setBookmarkEditText(locationItem.getAddress());
                } else {
                    bookmarksList.setBookmarkEditText("");
                }
                bookmarksList.setEditingAddress(true);
                dialog = new BookmarkEditDialogFragment();
                dialog.show(getSupportFragmentManager(), "BookmarkEditDialogFragment");
                return true;

            // Delete
            case R.id.delete:
                dialog = new ItemDeleteAlertFragment();
                dialog.show(getSupportFragmentManager(), "ItemDeleteAlertFragment");
                return true;

            default:
                return super.onContextItemSelected(item);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setBookmarkItemOnMap(int position) {

        // set button to GREEN
        buttonCurrentState.setButtonStatus(ButtonStatus.GO_BACK);

        // save location from item on memory
        mapSavedState.setLocationStatus(
                bookmarksList.getItemFromBookmarks(position).getLatitude(),
                bookmarksList.getItemFromBookmarks(position).getLongitude(),
                bookmarksList.getItemFromBookmarks(position).getAddress());

        // close bookmarks screen
        finish();

    }


    @Override // read result of permissions requests
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constants.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST) {// if permission is granted enable menu items
            if (grantResults.length > 0
                    && ((grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                menuItemBackup.setEnabled(true);
                menuItemRestore.setEnabled(true);
            }
        }
    }


    // BOOKMARK BACKUP DIALOG

    @Override // Yes
    public void onBackupDialogPositiveClick(DialogFragment dialog) {
        try {
            bookmarksList.backupBookmarks(getApplicationContext(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // No
    public void onBackupDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // BOOKMARK RESTORE DIALOG

    @Override // Yes
    public void onRestoreDialogPositiveClick(DialogFragment dialog) {
        try {
            bookmarksList.restoreBookmarks(getApplicationContext());
            // refresh list on screen
            // Note: notifyDataSetChanged() doesn't work properly sometimes
            bookmarksAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarksList.getBookmarks());
            bookmarksListView.setAdapter(bookmarksAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // No
    public void onRestoreDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // BOOKMARKS SORT DIALOG

    @Override
    public void onSortBookmarksDialogPositiveClick(DialogFragment dialog) {
        bookmarksList.sortBookmarks();
        // refresh list on screen
        // Note: notifyDataSetChanged() doesn't work properly sometimes
        bookmarksAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarksList.getBookmarks());
        bookmarksListView.setAdapter(bookmarksAdapter);
    }

    @Override
    public void onSortBookmarksDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // BOOKMARK EDIT DIALOG

    @Override // Ok
    public void onBookmarkEditDialogPositiveClick(DialogFragment dialog) {
        // update item and refresh list on screen
        // Note: notifyDataSetChanged() doesn't work properly sometimes
        if (bookmarksList.isEditingAddress()) {
            bookmarksList.updateLocationAddress(menuInfo.position);
        } else {
            bookmarksList.updateLocationName(menuInfo.position);
        }
        bookmarksAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarksList.getBookmarks());
        bookmarksListView.setAdapter(bookmarksAdapter);
    }

    @Override // Cancel
    public void onBookmarkEditDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // BOOKMARK DELETE DIALOG

    @Override // Yes
    public void onItemDeleteDialogPositiveClick(DialogFragment dialog) {
        // remove item and update list on screen
        // Note: notifyDataSetChanged() doesn't work properly sometimes
        bookmarksList.removeItemFromBookmarks(menuInfo.position);
        bookmarksAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarksList.getBookmarks());
        bookmarksListView.setAdapter(bookmarksAdapter);
    }

    @Override // No
    public void onItemDeleteDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // REPLACE LOCATION DIALOG

    @Override // Yes
    public void onReplaceLocationDialogPositiveClick(DialogFragment dialog) {
        Bundle bundle = dialog.getArguments();
        assert bundle != null;
        int position = bundle.getInt("position");
        // replace location on map
        setBookmarkItemOnMap(position);
    }

    @Override // No
    public void onReplaceLocationDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // LISTS TIP DIALOG

    @Override // Do not show again
    public void onListsTipDialogPositiveClick(DialogFragment dialog) {
        // tells application to do not show tip again
        showTipPref.edit().putBoolean(Constants.LISTS_TIP_SHOW, false).apply();
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }


    // WRITE EXTERNAL STORAGE PERMISSION DIALOG

    @Override // Yes
    public void onAlertExtStoragePermDialogPositiveClick(DialogFragment dialog) {
        // request permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                Constants.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST);

    }

    @Override // No
    public void onAlertExtStoragePermDialogNegativeClick(DialogFragment dialog) {
        Objects.requireNonNull(dialog.getDialog()).cancel();
    }

}