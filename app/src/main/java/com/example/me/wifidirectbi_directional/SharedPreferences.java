package com.example.me.wifidirectbi_directional;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ME on 04-Dec-16.
 */


/*
        This file is used to store the application data. Data is stored and retrieve
        for client and server.


 */

public class SharedPreferences {

    private static android.content.SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setStringValues(Context ctx, String key,String DataToSave) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(key, DataToSave);
        editor.apply();
    }

    public static String getStringValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, null);
    }

    public static void setIntValues(Context ctx, String key, int DataToSave) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(key, DataToSave);
        editor.apply();
    }

    public static int getIntValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getInt(key, 0);
    }

    public static void setBooleanValues(Context ctx, String key, Boolean DataToSave) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(key, DataToSave);
        editor.apply();
    }

    public static boolean getBooleanValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getBoolean(key, false);
    }

    public static long getLongValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getLong(key, 0L);
    }

    public static void setLongValues(Context ctx, String key, Long DataToSave) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(key, DataToSave);
        editor.apply();
    }
}
