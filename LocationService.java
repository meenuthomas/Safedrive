package com.meenu.safedrive;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;



public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,SensorEventListener{

    private static final long INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000 *1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    AppPreference appPreference;
    TextToSpeech textToSpeech;
    double speed;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        appPreference = new AppPreference(this);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }



    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppPreference appPreference=new AppPreference(getApplicationContext());
        if(appPreference.isSwitchOn()) {
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();


            //sensor
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI, new Handler());

        }else{
            stopSelf();

        }
        return 0;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        catch (SecurityException e){}
    }

    protected void startLocationUpdates() {
        try {
            @SuppressLint("MissingPermission") PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        catch (SecurityException e){}
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

    }


    @Override
    public void onConnectionSuspended(int i) {

    }




    @Override
    public void onLocationChanged(Location location) {
        //MainActivity.locate.dismiss();

appPreference.setLatitude(String.valueOf(location.getLatitude()));
appPreference.setLongitude(String.valueOf(location.getLongitude()));
        speed=location.getSpeed()*18/5;
       if(speed>2){
  Toast.makeText(getApplicationContext(),"Over Speed",Toast.LENGTH_LONG).show();
//     Intent intent= new Intent("messeges");
//     intent.putExtra("msg","Over Speed over speed over speed");
//     sendBroadcast(intent);



            textToSpeech.speak("Over Speed  Over Speed  Over Speed", TextToSpeech.QUEUE_FLUSH, null);

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



    public void connect()
    {
        mGoogleApiClient.connect();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
       // Toast.makeText(getApplicationContext(),"x "+x+" y "+y+" z "+z,Toast.LENGTH_SHORT).show();
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter
        //Toast.makeText(getApplicationContext(),""+mAccel,Toast.LENGTH_SHORT).show();
        if (mAccel > 18) {
            showNotification();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private void showNotification() {
        final NotificationManager mgr = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder note = new NotificationCompat.Builder(this);
        note.setContentTitle("Emergency");
        note.setTicker("New Message Alert!");
        note.setAutoCancel(true);
        // to set default sound/light/vibrate or all
        note.setDefaults(Notification.DEFAULT_ALL);
        // Icon to be set on Notification
        note.setSmallIcon(R.mipmap.ic_launcher);
        // This pending intent will open after notification click
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
                HomeActivity.class), 0);
        // set pending intent to notification builder
        note.setContentIntent(pi);
        mgr.notify(101, note.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSensorManager!=null){
            mSensorManager.unregisterListener(this);
        }
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }

    }
}
