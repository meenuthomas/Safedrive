package com.meenu.safedrive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Jobin on 1/20/2018.
 */

public class AppPreference {


    public static String SWITCH_STATUS="switch_status";
    public static String LATITUDE="latitude";
    public static String LONGITUDE="longitude";
    public static String PLACE="place";
    public static String NAME="name";
    public static String SPEED="speed";
    public static String EMERGENCY="emergency";

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



    public void setPlace(String value) {

        prefsEditor.putString(PLACE, value);
        prefsEditor.commit();

    }
    public String getPlace() {
        return sharedPreferences.getString(PLACE,"");
    }

    //name
    public void setName(String value) {

        prefsEditor.putString(NAME, value);
        prefsEditor.commit();

    }
    public String getName() {
        return sharedPreferences.getString(NAME,"");
    }

    //speed
    public void setSpeed(String value) {

        prefsEditor.putString(SPEED, value);
        prefsEditor.commit();

    }
    public String getSpeed() {
        return sharedPreferences.getString(SPEED,"");
    }

    //emergency
    public void setEmergency(String value) {

        prefsEditor.putString(EMERGENCY, value);
        prefsEditor.commit();

    }
    public String getEmergency() {
        return sharedPreferences.getString(EMERGENCY,"");
    }

}
