package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

public class DeleteBtsDatabase extends SessionActivity {
    String btsDeleteList;
    SharedPreferences sharedPreferences;
    MyTask myTask;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_bts_database);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        ArrayList<String> btsids = intent.getStringArrayListExtra("deletebts");
        Gson gson = new Gson();
        btsDeleteList = gson.toJson(btsids);

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_delete_bts));

        myTask = new MyTask(this);
        myTask.execute(builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<DeleteBtsDatabase> activityReference;
        ProgressDialog pd ;

        private MyTask(DeleteBtsDatabase context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Deleting Bts...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            DeleteBtsDatabase activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("bts_ids", activity.btsDeleteList);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            DeleteBtsDatabase activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String error = obj.getString("error");
                String message;
                if(result.equals("true")){
                    message = "Sucessfully deleted the sites";
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
