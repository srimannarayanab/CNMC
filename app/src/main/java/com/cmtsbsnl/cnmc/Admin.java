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

import java.io.IOException;
import java.security.GeneralSecurityException;

import androidx.appcompat.widget.Toolbar;

public class Admin extends SessionActivity {
    private SharedPreferences sharedPreferences;

//    public Admin() {
//    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        String user_privs = sharedPreferences.getString("user_privs","");
        String admin = sharedPreferences.getString("admin","");
        //        System.out.println("sms->"+sms_notifications);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(Admin.this, Navigational.class)));

        Button btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(Admin.this, MyBtsUsers.class);
            startActivity(intent);
        });

        if(user_privs.equals("circle") && admin.equals("Y")){
            Button btn2 = findViewById(R.id.button2);
            btn2.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, ResetPassword.class);
                startActivity(intent);
            });

            Button btn3 = findViewById(R.id.button3);
            btn3.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, Deactivate_User.class);
                startActivity(intent);
            });

            Button btn4 = findViewById(R.id.button4);
            btn4.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, Mybts_Leftout.class);
                startActivity(intent);
            });

            Button btn5 = findViewById(R.id.button5);
            btn5.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, UnlinkBts.class);
                startActivity(intent);
            });

            Button deleteuser = findViewById(R.id.del_user);
            deleteuser.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, DeleteUser.class);
                startActivity(intent);
            });

            Button userlevelupdate = findViewById(R.id.update_user_level);
            userlevelupdate.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, UserLevelUpdate.class);
                startActivity(intent);
            });

            Button usertype_update = findViewById(R.id.update_user_type);
            usertype_update.setOnClickListener(v -> {
                Intent intent = new Intent(Admin.this, ChangeUserType.class);
                startActivity(intent);
            });

            Button btn_chg_mybts_msisdn = findViewById(R.id.change_mybts_msisdn);
            btn_chg_mybts_msisdn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Admin.this, ChangeMyBtsMsisdn.class);
                    startActivity(intent);
                }
            });

            Button btn_ipsite_admin = findViewById(R.id.ipsite_admin);
            btn_ipsite_admin.setOnClickListener(view -> {
                Intent intent = new Intent(Admin.this, IpSitesAdministration.class);
                startActivity(intent);
            });
        } else {
//            Removing the button view who are not admin and move the constraint layout of
            View tv1 = findViewById(R.id.button2);
            ViewGroup cl = (ViewGroup) tv1.getParent();
            cl.removeView(tv1);

            View tv2 = findViewById(R.id.button3);
            cl.removeView(tv2);

            View tv3 = findViewById(R.id.button4);
            cl.removeView(tv3);

            View tv4 = findViewById(R.id.button5);
            cl.removeView(tv4);

            View tv5 = findViewById(R.id.del_user);
            cl.removeView(tv5);

            View tv6 = findViewById(R.id.update_user_level);
            cl.removeView(tv6);

            View tv7 = findViewById(R.id.update_user_type);
            cl.removeView(tv7);

            cl.removeView(findViewById(R.id.change_mybts_msisdn));
            cl.removeView(findViewById(R.id.ipsite_admin));



//            mConstraintSet.clone(mConstraintLayout);
//            mConstraintSet.connect(R.id.switch1,ConstraintSet.TOP,R.id.button1, ConstraintSet.BOTTOM);
//            mConstraintSet.applyTo(mConstraintLayout);
        }

        /*sw = (Switch) findViewById(R.id.switch1);
        sw.setTextSize(20);
        sw.setTextColor(Color.BLUE);
        if(notifications.equals("ON")){
            sw.setChecked(true);
        } else {
            sw.setChecked(false);
        }
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String notifications_status;
                if(isChecked) notifications_status = sw.getTextOn().toString();
                else notifications_status = sw.getTextOff().toString();
                System.out.println(notifications_status);
                Intent intent1 = new Intent(Admin.this, NotificationsEnabledDisabled.class);
                intent1.putExtra("notifications_status", notifications_status);
                startActivity(intent1);
            }
        });*/

        /*sw2 = (Switch) findViewById(R.id.switch2);
        sw2.setTextSize(20);
        sw2.setTextColor(Color.BLUE);
        if(sms_notifications.equals("Y")){
            sw2.setChecked(true);
        } else {
            sw2.setChecked(false);
        }
        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String sms_status;
                if(isChecked) sms_status = sw2.getTextOn().toString();
                else sms_status = sw2.getTextOff().toString();
//                System.out.println(notifications_status);
                Intent intent1 = new Intent(Admin.this, SmsEnabledDisabled.class);
                intent1.putExtra("sms_status", sms_status);
                startActivity(intent1);
            }
        });
        sw2.setVisibility(View.INVISIBLE);*/

    }
}
