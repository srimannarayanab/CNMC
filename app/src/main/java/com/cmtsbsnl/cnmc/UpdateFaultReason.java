package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateFaultReason extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private AlertDialog alertDialog;
    private String  log_id, bts_down_cause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }


        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_update_reason));


        //    private SharedPreferences.Editor editor;
        Intent intent = getIntent();
//        bts_id = intent.getStringExtra("bts_id");
//        fault_id = intent.getStringExtra("fault_id");
//        bts_status_dt = intent.getStringExtra("bts_status_dt");
        log_id = intent.getStringExtra("log_id");
        bts_down_cause = intent.getStringExtra("bts_down_cause");
//        pref = getApplicationContext().getSharedPreferences("CnmcPref", MODE_PRIVATE);
//        msisdn = pref.getString("msisdn","");
//        System.out.println(msisdn +" "+bts_id+" "+fault_id);
        MyTask myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<UpdateFaultReason> activityReference;
        ProgressDialog pd;

        private MyTask(UpdateFaultReason context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            UpdateFaultReason activity = activityReference.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
                conn.setRequestProperty("Context-Type","application/json; utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                JSONObject post_obj = new JSONObject();
                post_obj.put("log_id", activity.log_id);
                post_obj.put("bts_down_cause", activity.bts_down_cause);
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));

                OutputStream os = conn.getOutputStream();
                os.write(post_obj.toString().getBytes());
                os.flush();
                os.close();
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                StringBuilder res = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    res.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
                return res.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            UpdateFaultReason activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
            activity.alertDialog = new AlertDialog.Builder(activity).create();
            try {
                JSONObject obj = new JSONObject(s);
//                System.out.println(obj.getString("result"));
                if(obj.getString("result").equals("ok")){
                    activity.alertDialog.setTitle("CNMC Alert ....");
                    activity.alertDialog.setMessage("Data Updated sucessfully");
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
//                                Intent intent1 = new Intent(activity, ReasonUpdate.class);
//                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                                activity.startActivity(intent1);
                                activity.finish();
                            });
                    activity.alertDialog.show();
                } else{
                    activity.alertDialog.setTitle("CNMC Alert ....");
                    activity.alertDialog.setMessage(obj.getString("error"));
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                            (dialog, which) -> activity.alertDialog.dismiss());
                    activity.alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
