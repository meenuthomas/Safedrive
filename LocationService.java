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
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by Jobin on 1/20/2018.
 */

public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,SensorEventListener{

    private static final long INTERVAL = 8000;
    private static final long FASTEST_INTERVAL = 8000 *1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    AppPreference appPreference;
    double carSpeed;
    TextToSpeech textToSpeech;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    boolean vertical, horizontal;
    int rotation;
    long t1, t2, t,lastUpdate;
    float last_x, last_y,last_z,x,y,z;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appPreference=new AppPreference(this);
        rotation = -1;
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
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
    public void onLocationChanged(final Location location) {
        //MainActivity.locate.dismiss();

appPreference.setLatitude(String.valueOf(location.getLatitude()));
appPreference.setLongitude(String.valueOf(location.getLongitude()));
        carSpeed=location.getSpeed()*18/5;
        if(carSpeed>40){

            textToSpeech.speak("over speed", TextToSpeech.QUEUE_FLUSH, null);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("traffic");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {

                        double distance=distance(Double.parseDouble(String.valueOf( issue.child("latitude").getValue())),Double.parseDouble(String.valueOf( issue.child("longitude").getValue())),   location.getLatitude(), location.getLongitude());
                        // do something with the individual "issues"
                       // Log.d("dddd",""+distance(Double.parseDouble(String.valueOf( issue.child("latitude").getValue())),Double.parseDouble(String.valueOf( issue.child("longitude").getValue())),   location.getLatitude(), location.getLongitude()));

                        if ( distance< 1) { // if distance < 0.1

                          if(!appPreference.getPlace().equals(issue.getKey())) {
                              textToSpeech.speak("Traffic in" + issue.getKey(), TextToSpeech.QUEUE_FLUSH, null);
                              //   launch the activity
                              appPreference.setPlace(issue.getKey());
                              break;
                          }
                        }
                   ;

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        removeData();

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


            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x =sensorEvent.values[0];;
                y = sensorEvent.values[1];;
                z = sensorEvent.values[2];;

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > 10000) {
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                   sendSMS("9446306616","Emergency");
                }
                last_x = x;
                last_y = y;
                last_z = z;

        }




//        float x = sensorEvent.values[0];
//        float y = sensorEvent.values[1];
//        float z = sensorEvent.values[2];
//
//        if (rotation == 0) {
//            t1 = System.currentTimeMillis();
//        }
//
//        if (Math.abs(x)>5 && Math.abs(y)<5 && !horizontal) {
//            vertical = false;
//            horizontal = true;
//            rotation++;
//            Log.d("ddd", "horizontal");
//        }
//        if (Math.abs(x)<5 && Math.abs(y)>5 && !vertical) {
//            vertical = true;
//            horizontal = false;
//            rotation++;
//            Log.d("ddd", "vertical");
//        }
//
//        t2 = System.currentTimeMillis();
//        t = t2 - t1;
//        if (t>1000 && rotation<2) {
//            rotation = 0;
//        } else if (t<=1000 && rotation==2) {
//            Log.d("ddd", "Rotated twice in t <= 1000ms");
//            rotation = 0;
//        } else if (rotation>2) {
//            rotation = 0;
//        }

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
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }
    public void removeData(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("traffic");


        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES);
        Query oldItems = reference.orderByChild("timeStamp").endAt(cutoff);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }
    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
