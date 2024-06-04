/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : BookmarkEditDialogFragment.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.voltaki.R;
import com.apps.mohb.voltaki.lists.Lists;


public class BookmarkEditDialogFragment extends DialogFragment {

    public interface BookmarkEditDialogListener {
        void onBookmarkEditDialogPositiveClick(DialogFragment dialog);

        void onBookmarkEditDialogNegativeClick(DialogFragment dialog);
    }

    private BookmarkEditDialogListener mListener;
    private Lists lists;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = View.inflate(getContext(), R.layout.fragment_bookmark_edit_dialog, null);

        lists = new Lists(getContext());

        final EditText text = view.findViewById(R.id.txtEdit);
        text.setText(lists.getBookmarkEditText());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        if (lists.isEditingAddress()) {
            builder.setTitle(R.string.dialog_title_location_address);
        } else {
            builder.setTitle(R.string.dialog_title_location_name);
        }
        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                lists.setBookmarkEditText(text.getText().toString());
                mListener.onBookmarkEditDialogPositiveClick(BookmarkEditDialogFragment.this);
            }
        })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onBookmarkEditDialogNegativeClick(BookmarkEditDialogFragment.this);
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host context implements the callback interface
        try {
            // Instantiate the BookmarkEditDialogListener so we can send events to the host
            mListener = (BookmarkEditDialogListener) context;
        } catch (ClassCastException e) {
            // The context doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement BookmarkEditDialogListener");
        }
    }

}
