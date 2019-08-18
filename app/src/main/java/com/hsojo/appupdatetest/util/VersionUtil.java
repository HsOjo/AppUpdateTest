package com.hsojo.appupdatetest.util;

import android.content.Context;
import android.content.SharedPreferences;

public class VersionUtil {
    private static String TAG = "Version";
    private static String key_version = "version";

    public static int toIntVer(String version) {
        try {
            return Integer.parseInt(version.replace(".", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static SharedPreferences buildSP(Context context) {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public static String getVersion(Context context) {
        return buildSP(context).getString(key_version, "0.0.0");
    }

    public static void setVersion(Context context, String value) {
        SharedPreferences.Editor edit = buildSP(context).edit();
        edit.putString(key_version, value);
        edit.apply();
    }
}
