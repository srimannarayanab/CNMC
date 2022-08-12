package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;

public class Availability extends SessionActivity {
    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor editor;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(Availability.this, Navigational.class)));

//        Get the User privileges
        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String user_privs = sharedPreferences.getString("user_privs","");
        final String circle_id = sharedPreferences.getString("circle_id","");

        if(user_privs.equals("co")){
            Button btn2 = findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent = new Intent(Availability.this, CircleAvailability.class);
                startActivity(intent);
            });

            Button btn3 = findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent = new Intent(Availability.this, CircleMttr.class);
                startActivity(intent);
            });

        } else if(user_privs.equals("circle")) {
            Button btn2 = findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(Availability.this, CircleAvailability.class);
                } else {
                    intent = new Intent(Availability.this, SsaAvailability.class);
                    intent.putExtra("circle_id", circle_id);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });

            Button btn3 = findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(Availability.this, CircleMttr.class);
                } else {
                    intent = new Intent(Availability.this, SsaMttr.class);
                    intent.putExtra("circle_id", circle_id);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });

        }


    }
}
