package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;

public class MyBts extends SessionActivity {
    private SharedPreferences sharedPreferences;
    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        String user_privs = sharedPreferences.getString("user_privs","");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(MyBts.this, Navigational.class)));

        if(user_privs.contains("co")){
            View tv1 = findViewById(R.id.button6);
            ViewGroup cl = (ViewGroup) tv1.getParent();
            cl.removeAllViews();

            TextView tv = new TextView(MyBts.this);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setPadding(50, 120, 50, 40);
            tv.setTextColor(Color.MAGENTA);
            tv.setTextSize(20);
            tv.setText(getString(R.string.notification,"Sorry This option is enabled for Circle users"));
            cl.addView(tv);
        } else if(user_privs.contains("circle") || user_privs.contains("outSource")){
            Button btn1 = findViewById(R.id.button1);
            btn1.setOnClickListener(v -> {
                Intent intent = new Intent(MyBts.this, AddBts.class);
                startActivity(intent);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
            });

            Button btn2 = findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent = new Intent(MyBts.this, DeleteBts.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();
            });


            Button btn3 = findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent = new Intent(MyBts.this, ViewBts.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();
            });
        }
    }
}
