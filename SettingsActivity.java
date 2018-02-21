package com.meenu.safedrive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
Button addTraffic;
private DatabaseReference mDatabase;
AppPreference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FirebaseApp.initializeApp(this);
       mDatabase = FirebaseDatabase.getInstance().getReference("safedrive-b9651");
        appPreference=new AppPreference(this);
        addTraffic=findViewById(R.id.buttonAdd);
        addTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long value=System.currentTimeMillis();
                Category category=new Category(Constants.TRAFFIC_ID,Double.parseDouble(appPreference.getLatiude()),Double.parseDouble(appPreference.getLongitude()),"",System.currentTimeMillis());
                mDatabase.setValue(category);
            }
        });

    }
}
