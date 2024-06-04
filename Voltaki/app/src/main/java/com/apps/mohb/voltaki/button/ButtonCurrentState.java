/*
 *  Copyright (c) 2020 mohb apps - All Rights Reserved
 *
 *  Project       : Voltaki
 *  Developer     : Haraldo Albergaria Filho, a.k.a. mohb apps
 *
 *  File          : ButtonCurrentState.java
 *  Last modified : 9/29/20 3:04 PM
 *
 *  -----------------------------------------------------------
 */

package com.apps.mohb.voltaki.button;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Button;

import com.apps.mohb.voltaki.Constants;
import com.apps.mohb.voltaki.R;


// This class manages the button current states

public class ButtonCurrentState {

    private Button button;
    private SharedPreferences preferences;

    public ButtonCurrentState(Context context) {
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Constants.PRIVATE_MODE);
    }

    public void setButton(Button b) {
        button = b;
    }

    public Button getButton() {
        return button;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setButtonProperties(Context context, int color, int textColor, int text, float textSize, boolean enabled) {
        if (button != null) {
            // check sdk version to apply correct methods
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Lollipop or older
                button.setBackgroundColor(context.getResources().getColor(color));
                button.setTextColor(context.getResources().getColor(textColor));
            } else { // Marshmallow or newer
                button.setBackgroundColor(context.getResources().getColor(color, context.getTheme()));
                button.setTextColor(context.getResources().getColor(textColor, context.getTheme()));
            }
            button.setTextSize(textSize);
            button.setText(text);
            button.setEnabled(enabled);
        }
    }

    public void setButtonOffline(Context context) {
        setButtonProperties(context, R.color.colorOfflineButton,
                R.color.colorWhiteTextButton, R.string.button_offline,
                Constants.TEXT_LARGE, true);
    }

    public void setButtonGetLocation(Context context) {
        setButtonProperties(context, R.color.colorGetLocationButton,
                R.color.colorBlackTextButton, R.string.button_get_location, Constants.TEXT_LARGE, false);
    }

    public void setButtonComeBack(Context context) {
        setButtonProperties(context, R.color.colorComeBackHereButton,
                R.color.colorBlackTextButton, R.string.button_come_back_here, Constants.TEXT_LARGE, true);
    }

    public void setButtonGoBack(Context context) {
        setButtonProperties(context, R.color.colorGoBackButton,
                R.color.colorBlackTextButton, R.string.button_go_back, Constants.TEXT_LARGE, true);
    }

    public void setButtonGoBackClicked(Context context) {
        setButtonProperties(context, R.color.colorGoBackButton,
                R.color.colorYellowTextButton, R.string.button_go_back, Constants.TEXT_LARGE, true);
    }

    public void setButtonGoBackOffline(Context context) {
        setButtonProperties(context, R.color.colorGoBackButton,
                R.color.colorOfflineButton, R.string.button_go_back, Constants.TEXT_LARGE, true);
    }

    public void setButtonStatus(ButtonStatus status) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.BUTTON_STATUS, ButtonEnums.convertEnumToInt(status));
        editor.apply();
    }

    public ButtonStatus getButtonStatus() {
        int status = preferences.getInt(Constants.BUTTON_STATUS, Constants.DEFAULT_BUTTON_STATUS);
        return ButtonEnums.convertIntToEnum(status);
    }


}
