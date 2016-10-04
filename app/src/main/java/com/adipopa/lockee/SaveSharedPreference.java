/****************************************************************************************
 *                                                                                       *
 *   Copyright (C) 2016 Glimpse Team                                                     *
 *                                                                                       *
 *       This file is part of the Lockee project and is hereby protected by copyright    *
 *   and can not be copied and/or distributed without the express permission of all      *
 *   the Glimpse Team members.                                                           *
 *                                                                                       *
 ****************************************************************************************/

package com.adipopa.lockee;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class SaveSharedPreference {
    private static final String PREF_LOGIN_STATUS = "logged in";
    private static final String PREF_SHARED_ID = "shared";
    private static final String PREF_SHARED_NAME = "name";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    static void setLoginStatus(Context ctx, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGIN_STATUS, userName);
        editor.apply();
    }

    static String getLoginStatus(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_LOGIN_STATUS, "not logged");
    }

    static void setSharedID(Context ctx, String shareID){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_SHARED_ID, shareID);
        editor.apply();
    }

    static String getSharedID(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SHARED_ID, "not shared");
    }

    static void setSharedName(Context ctx, String shareName){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_SHARED_NAME, shareName);
        editor.apply();
    }

    static String getSharedName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SHARED_NAME, "no name");
    }
}
