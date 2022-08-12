package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {
    private EditText ed, ed1;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        boolean saveLogin;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
            editor = sharedPreferences.edit();

        } catch (GeneralSecurityException |IOException e) {
            e.printStackTrace();
        }


// use the shared preferences and editor as you normally would

        final int version_no = BuildConfig.VERSION_CODE;
        TextView tv = findViewById(R.id.textView1);
        tv.setText(getString(R.string.app_version,BuildConfig.VERSION_NAME));

//        Verify the Internet connection
        boolean inet = isNetworkConnected();
        if(!inet){
            Toast.makeText(getApplicationContext(), "No Internet Access", Toast.LENGTH_SHORT).show();
        }
//        System.out.println(version_no);

        ed  = findViewById(R.id.username);
        ed1 = findViewById(R.id.password);
        saveLoginCheckBox = findViewById(R.id.saveLoginCheckBox);
        editor.putString("version_code", Integer.toString(version_no));
        saveLogin = sharedPreferences.getBoolean("saveLogin", false);
        if(saveLogin){
            ed.setText(sharedPreferences.getString("msisdn",""));
            ed1.setText(sharedPreferences.getString("pd",""));
            saveLoginCheckBox.setChecked(true);
        }

       /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                *//* Create an Intent that will start the MainActivity. *//*
                Intent mainIntent = new Intent(MainActivity.this, Navigational.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);*/
        Button login = findViewById(R.id.login);
        login.setOnClickListener((View v)->{
                String username = ed.getText().toString();
                String password = ed1.getText().toString();
                if(username.length()!=10 || username==null){
                  ed.setError("Msisdn lenght should be 10");
                  return;
                }
                if(saveLoginCheckBox.isChecked()){
                    editor.putString("msisdn", ed.getText().toString());
                    editor.putString("pd",ed1.getText().toString());
                    editor.putBoolean("saveLogin",true);
                } else {
                    editor.putBoolean("saveLogin", false);
                    editor.putString("msisdn", null);
                    editor.putString("pd", null);
                }
                editor.apply();
//                System.out.println("savelogin->"+pref.getBoolean("saveLogin",true));
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                intent.putExtra("version_no", Integer.toString(version_no));
                startActivity(intent);
        });

        TextView tv6 = findViewById(R.id.textView6);
        tv6.setOnClickListener((View v)->{
                Intent intent = new Intent(MainActivity.this, NewUserCreation.class);
                startActivity(intent);
        });

        TextView tv7 =  findViewById(R.id.textView7);
        tv7.setOnClickListener((View v)->{
                Intent intent = new Intent(MainActivity.this, ForgetPassword.class);
                startActivity(intent);
        });

        TextView tv8 = findViewById(R.id.textView8);
        tv8.setOnClickListener((View v)->{
                Intent intent = new Intent(MainActivity.this, CircleAdministrator.class);
                startActivity(intent);
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }
}
