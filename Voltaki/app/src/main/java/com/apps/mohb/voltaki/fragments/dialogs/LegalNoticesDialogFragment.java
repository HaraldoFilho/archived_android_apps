/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : LegalNoticesDialogFragment.java
 *  Last modified : 9/29/20 12:29 AM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.apps.mohb.voltaki.R;


public class LegalNoticesDialogFragment extends DialogFragment {

    public static LegalNoticesDialogFragment newInstance(String notices) {

        LegalNoticesDialogFragment legalNoticesDialogFragment = new LegalNoticesDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("notices", notices);
        legalNoticesDialogFragment.setArguments(bundle);

        return legalNoticesDialogFragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        assert getArguments() != null;
        String legalNotices = getArguments().getString("notices");

        View view = View.inflate(getContext(), R.layout.fragment_about_dialog, null);

        TextView textViewTitle = view.findViewById(R.id.txtTitle);
        TextView textView = view.findViewById(R.id.txtText);

        textViewTitle.setText(getText(R.string.action_legal_notices));
        textView.setText(legalNotices);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(view);

        return alertDialogBuilder.create();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}