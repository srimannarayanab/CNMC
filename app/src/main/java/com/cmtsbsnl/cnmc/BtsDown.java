package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.security.GeneralSecurityException;

import androidx.appcompat.widget.Toolbar;

public class BtsDown extends SessionActivity {
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bts_down);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(BtsDown.this, Navigational.class)));

//        Get the User privileges
        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String user_privs = sharedPreferences.getString("user_privs","");
        final String circle_id = sharedPreferences.getString("circle_id","");
        final String msisdn = sharedPreferences.getString("msisdn","");
//        System.out.println(user_privs);
//        System.out.println(circle_id);
        if(user_privs.equals("co"))
        {
            Button btn1 = (Button) findViewById(R.id.button1);
            btn1.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, CategoryWise.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });

            Button btn_techwise = (Button) findViewById(R.id.button_techwise);
            btn_techwise.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, TechWise.class);
                startActivity(intent);
            });

            Button btn2 = (Button) findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, DurationWise.class);
                startActivity(intent);
            });


            Button btn3 = (Button) findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, SitetypeWise.class);
                startActivity(intent);
            });

            Button btn3_1 = (Button) findViewById(R.id.button3_1);
            btn3_1.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, Outsourced.class);
                startActivity(intent);
            });

            Button btn4 = (Button) findViewById(R.id.button4);
            btn4.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, PartialDown.class);
                startActivity(intent);
            });

            Button btn5 = (Button) findViewById(R.id.button5);
            btn5.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, LeasedOut.class);
                startActivity(intent);
            });

            Button btn6 = (Button) findViewById(R.id.button6);
            ViewGroup cl = (ViewGroup) btn6.getParent();
            for(int v =0; v<cl.getChildCount(); v++){
                if(cl.getChildAt(v) instanceof Button){
                    if(((TextView) cl.getChildAt(v)).equals(btn6)) {
                        cl.removeView(cl.getChildAt(v));
                    }
                }
            }
            Button btn7 = (Button) findViewById(R.id.button7);
            btn7.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, CommerciallyLocked.class);
                startActivity(intent);
            });

            View omcr_process = (Button) findViewById(R.id.button8);
            ViewGroup viewGroup = (ViewGroup) omcr_process.getParent();
            if(viewGroup !=null) {
                viewGroup.removeView(omcr_process);
            }


        } else if(user_privs.equals("circle")) {
            Button btn1 = (Button) findViewById(R.id.button1);
            btn1.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")){
                    intent = new Intent(BtsDown.this, CategoryWise.class);

                } else {
                    intent = new Intent(BtsDown.this, CategoryWiseSsa.class);
                }
                intent.putExtra("circle_id", circle_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });

            Button btn_techwise = (Button) findViewById(R.id.button_techwise);
            btn_techwise.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, TechWise.class);
                } else {
                    intent = new Intent(BtsDown.this, TechWiseSsa.class);
                }
                intent.putExtra("circle_id", circle_id);
                intent.putExtra("ssa_id", "%");
                intent.putExtra("bts_type", "%");
                startActivity(intent);
            });

            Button btn2 = (Button) findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, DurationWise.class);
                } else {
                    intent = new Intent(BtsDown.this, DurationWiseSsa.class);
                }
                intent.putExtra("circle_id", circle_id);
                startActivity(intent);
            });

            Button btn3 = (Button) findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, SitetypeWise.class);
                } else {
                    intent = new Intent(BtsDown.this, SitetypeSsa.class);
                }
                intent.putExtra("circle_id", circle_id);
                startActivity(intent);
            });

            Button btn3_1 = (Button) findViewById(R.id.button3_1);
            btn3_1.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, Outsourced.class);
                } else {
                    intent = new Intent(BtsDown.this, SsaWiseOutsourced.class);
                    intent.putExtra("circle_id", circle_id);
                }
                startActivity(intent);
            });

            Button btn4 = (Button) findViewById(R.id.button4);
            btn4.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, PartialDown.class);
                } else {
                    intent = new Intent(BtsDown.this, PartialDownSsa.class);
                    intent.putExtra("circle_id", circle_id);
                }
                startActivity(intent);
            });

            Button btn5 = (Button) findViewById(R.id.button5);
            btn5.setOnClickListener(v -> {
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(BtsDown.this, LeasedOut.class);
                } else {
                    intent = new Intent(BtsDown.this, LeasedOutSsa.class);
                    intent.putExtra("circle_id", circle_id);
                }
                startActivity(intent);
            });

            Button btn6 = (Button) findViewById(R.id.button6);
            btn6.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, MyBtsDown.class);
                intent.putExtra("msisdn", msisdn);
                startActivity(intent);
            });

            Button btn7 = (Button) findViewById(R.id.button7);
            btn7.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, CommerciallyLocked.class);
                intent.putExtra("circle_id", circle_id);
                startActivity(intent);
            });

            Button btn8 = (Button) findViewById(R.id.button8);
            btn8.setOnClickListener(v -> {
                Intent intent = new Intent(BtsDown.this, OmcrProcess.class);
                intent.putExtra("circle_id", circle_id);
                startActivity(intent);
            });
        }
    }
}
