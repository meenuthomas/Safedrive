package com.meenu.safedrive;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends Activity {

AppPreference appPreference;
EditText editName,editSpeed,editEmergency;
Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editName=findViewById(R.id.editName);
        editSpeed=findViewById(R.id.editSpeed);
        editEmergency=findViewById(R.id.editEmergency);
        buttonSave=findViewById(R.id.saveButton);

        appPreference=new AppPreference(this);
        editName.setText(appPreference.getName());
        editSpeed.setText(appPreference.getSpeed());
        editEmergency.setText(appPreference.getEmergency());

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appPreference.setName(editName.getText().toString());
                appPreference.setSpeed(editSpeed.getText().toString());
                appPreference.setEmergency(editEmergency.getText().toString());
                finish();

            }
        });


    }
}
