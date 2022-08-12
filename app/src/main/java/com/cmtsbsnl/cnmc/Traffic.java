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

public class Traffic extends SessionActivity {
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String user_privs = sharedPreferences.getString("user_privs","");
        String circle_id = sharedPreferences.getString("circle_id","");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(Traffic
                .this, Navigational.class)));
        Button btn = findViewById(R.id.button1);
        if(user_privs.equals("co")) {
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(Traffic.this, CircleWiseTraffic.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        } else {
            btn.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(Traffic.this, CircleWiseTraffic.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                } else {
                    intent = new Intent(Traffic.this, SsaWiseTraffic.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("circle_id", circle_id);
                }
                startActivity(intent);
            });
        }
    }
}
