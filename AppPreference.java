package com.meenu.safedrive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class AppPreference {


    public static String SWITCH_STATUS="switch_status";
    public static String LATITUDE="latitude";
    public static String LONGITUDE="longitude";
    private static AppPreference instance;

    Context context;
    SharedPreferences.Editor prefsEditor;

    public static SharedPreferences sharedPreferences;


    public AppPreference(Context context){
        this.context=context;
        sharedPreferences = context.getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE);
        prefsEditor = sharedPreferences.edit();

    }
    //set switch status
    public void setSwitchStatus(boolean value) {

        prefsEditor.putBoolean(SWITCH_STATUS, value);
        prefsEditor.commit();

    }
    public boolean isSwitchOn() {
        return sharedPreferences.getBoolean(SWITCH_STATUS, false);
    }
    //set latitude
    public void setLatitude(String value) {

        prefsEditor.putString(LATITUDE, value);
        prefsEditor.commit();

    }
    public String getLatiude() {
        return sharedPreferences.getString(LATITUDE,"");
    }
    //set longitude
    public void setLongitude(String value) {

        prefsEditor.putString(LONGITUDE, value);
        prefsEditor.commit();

    }
    public String getLongitude() {
        return sharedPreferences.getString(LONGITUDE,"");
    }
}
