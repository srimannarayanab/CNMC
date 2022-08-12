package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

public class AddBtsDatabase extends SessionActivity {
    String btsAddList;
    SharedPreferences sharedPreferences;
    MyTask mytask;
    String msisdn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bts_database);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_add_bts));

//        sharedPreferences = getApplicationContext().getSharedPreferences("CnmcPref",MODE_PRIVATE);
        msisdn = sharedPreferences.getString("msisdn","Test");
        Intent intent = getIntent();
        ArrayList<String> btsids = intent.getStringArrayListExtra("addbts");
        Gson gson = new Gson();
        btsAddList = gson.toJson(btsids);
//        System.out.println(btsAddList);
        mytask = new MyTask(this);
        mytask.execute(builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String>{
        private final WeakReference<AddBtsDatabase> activityReference;
        ProgressDialog pd ;

        private MyTask(AddBtsDatabase context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Adding Bts...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            AddBtsDatabase activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("bts_ids", activity.btsAddList);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            AddBtsDatabase activity = activityReference.get();
            pd.dismiss();
            System.out.println(s);
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONObject obj = new JSONObject(url_obj.getString("data"));
                String result = obj.getString("result");
                String error = obj.getString("error");
                String message;
                if(result.equals("true")){
                    message = "Sucessfully added the sites";
                } else {
                    message = error;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(activity, MyBts.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            activity.startActivity(intent);
                            activity.finish();
                        });
                alertDialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}