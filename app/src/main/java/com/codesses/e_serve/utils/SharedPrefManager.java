
package com.codesses.e_serve.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.codesses.e_serve.R;
import com.codesses.e_serve.enums.SharedPrefKey;


public class SharedPrefManager {

    //    TODO: Data Members
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        return new SharedPrefManager(context);
    }

    public void storeSharedData(String key, String value) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public void storeSharedData(SharedPrefKey key, Double value) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key.toString(), value.toString());
        editor.apply();
    }

    public void storeSharedData(String key, Boolean value) {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(key, value);
        editor.apply();

    }

    public void storeSharedData(SharedPrefKey key, String value) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key.toString(), value);
        editor.apply();
    }

    public String getSharedData(String key) {

        SharedPreferences preferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        return preferences.getString(key, null);

    }

    public Boolean getBooleanData(String key) {

        SharedPreferences preferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);

    }

    public void clearString(String key) {
        SharedPreferences preferences = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_preference), Context.MODE_PRIVATE);
        preferences.edit().remove(key).apply();
    }

}
