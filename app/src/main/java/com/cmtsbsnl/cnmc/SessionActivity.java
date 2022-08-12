package com.cmtsbsnl.cnmc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SessionActivity extends AppCompatActivity {
    public static final long DISCONNECT_TIMEOUT = 900000;
    private Handler handler;
    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), SesssionLogout.class);
                startActivity(intent);
                finish();
//                Toast.makeText(SessionActivity.this, "Logged out after 3 minutes on inactivity.", Toast.LENGTH_SHORT).show();
            }
        };

        startHandler();

    }

    public void stopHandler() {
        handler.removeCallbacks(r);
    }

    public void startHandler() {
        handler.postDelayed(r, DISCONNECT_TIMEOUT);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();
        startHandler();
    }

    @Override
    protected void onPause() {
//        stopHandler();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startHandler();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHandler();
    }
}
