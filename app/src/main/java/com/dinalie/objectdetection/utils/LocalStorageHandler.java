package com.dinalie.objectdetection.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorageHandler {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public LocalStorageHandler(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, 0);
        editor = sharedPreferences.edit();
    }

    public static String ReadData(String key) {
        return sharedPreferences.getString(key, "");
    }

    public static void WriteData(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }


}
