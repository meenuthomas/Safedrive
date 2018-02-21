package com.meenu.safedrive;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationSettingsResult;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
ToggleButton toggleButton;
ImageView settings;
    private int PERMISSION_CODE = 1;
    AppPreference appPreference;
    LocationManager manager;
    TextToSpeech textToSpeech;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toggleButton=findViewById(R.id.toggleButton);
        settings=findViewById(R.id.settings);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ispermissionAllowed()) {

            } else {
                requestStoragePermission();
            }
        }else{

        }


        appPreference=new AppPreference(this);

        toggleButton.setChecked(appPreference.isSwitchOn());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ispermissionAllowed()) {

                      setDriveMode(b);

                    } else {
                        requestStoragePermission();
                    }
                }else{
                    setDriveMode(b);

                }



            }
        });
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent n=new Intent(HomeActivity.this,SettingsActivity.class);
                startActivity(n);
            }
        });

    }
    private boolean ispermissionAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED) 
            return true;

        //If permission is not granted returning false
        return false;
    }
    private void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == PERMISSION_CODE){

            //If permission is granted
          startService(new Intent(this,LocationService.class));
        }
    }

    private void alertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

public void setDriveMode(boolean b){
    appPreference.setSwitchStatus(b);
    if(b){
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            alertMessageNoGps();
            toggleButton.setChecked(false);
        }else{
            textToSpeech.speak("Hello , Please wear your seat belt. and  " +
                    " wish you a safe journey", TextToSpeech.QUEUE_FLUSH, null);
            startService(new Intent(HomeActivity.this,LocationService.class));
        }

    }else{

        stopService(new Intent(HomeActivity.this,LocationService.class));
    }
}
    @Override
    protected void onResume() {
//
//        if (broadcastReceiver ==null) {
//            broadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    String messege = String.valueOf(intent.getExtras().get("msg"));
//                    textToSpeech.speak(messege, TextToSpeech.QUEUE_FLUSH, null);
//
//                }
//            };
//        }
        super.onResume();
        if(appPreference.isSwitchOn()) {
            manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                toggleButton.setChecked(true);



            }
        }
    }
}
