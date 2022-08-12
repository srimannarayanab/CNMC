package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class update_user_level extends AppCompatActivity {
//    AlertDialog alertDialog;
    private String lvl, lvl2, lvl3, desg ;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_level);
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_update_user_level));
        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final Intent intent = getIntent();
        desg = intent.getStringExtra("desg");
        lvl = intent.getStringExtra("level");
        lvl2 = intent.getStringExtra("level2");
        lvl3 = intent.getStringExtra("level3");
        MyTask myTask = new MyTask(update_user_level.this);
        myTask.execute(uri_builder.toString());

    }

    public static class MyTask extends AsyncTask<String, Void, String>{
        private final WeakReference<update_user_level> activityReference;
        ProgressDialog pd;

        private MyTask(update_user_level context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Updating the Level details of users...");
            pd.setMessage("Level details are updating please wait");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            update_user_level activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("desg", activity.desg);
                post_obj.put("lvl", activity.lvl);
                post_obj.put("lvl2", activity.lvl2);
                post_obj.put("lvl3", activity.lvl3);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            update_user_level activity = activityReference.get();
            System.out.println(s);
            pd.dismiss();
            try {
                JSONObject obj = new JSONObject(s);
                String output = obj.getString("result");
                String error = obj.getString("error");
                if(output.equals("true")){
                    JSONObject data = new JSONObject(obj.getString("data"));
                    if(!data.getString("result").equals("ok")){
                        error = data.getString("error");
                    }

                }

                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("User Level details");
                if(output.equals("true")){
                    alertDialog.setMessage("user level details updated sucessfully");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> {
                        Intent intent1 = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent1);
//                            finish();
                    });
                } else {
                    alertDialog.setMessage(error);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Close", (dialog, which) -> activity.finish());
                }
                alertDialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}