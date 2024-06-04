/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : MapsNotInstalledAlertFragment.java
 *  Last modified : 9/29/20 12:29 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.fragments.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.voltaki.R;


public class MapsNotInstalledAlertFragment extends DialogFragment {

    public interface MapsNotInstalledAlertDialogListener {
        void onMapsAlertDialogPositiveClick(DialogFragment dialog);
    }

    private MapsNotInstalledAlertDialogListener mListener;


    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.alert_title_no_maps).setMessage(R.string.alert_title_need_maps)
                .setPositiveButton(R.string.alert_button_install, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onMapsAlertDialogPositiveClick(MapsNotInstalledAlertFragment.this);
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host context implements the callback interface
        try {
            // Instantiate the MapsNotInstalledAlertDialogListener so we can send events to the host
            mListener = (MapsNotInstalledAlertDialogListener) context;
        } catch (ClassCastException e) {
            // The context doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement MapsNotInstalledAlertDialogListener");
        }
    }

}
