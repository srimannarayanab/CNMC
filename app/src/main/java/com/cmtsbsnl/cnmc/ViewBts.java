package com.cmtsbsnl.cnmc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;

public class ViewBts extends SessionActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        if(intent.hasExtra("msisdn")){
        } else {
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

        ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_view_bts));

        MyTask mytask = new MyTask(this);
        mytask.execute(builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<ViewBts> activityReference;
        ProgressDialog pd;

        private MyTask(ViewBts context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching Bts...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ViewBts activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ViewBts activity = activityReference.get();
            pd.dismiss();
//            https://www.journaldev.com/14171/android-checkbox
            ListView lview = activity.findViewById(R.id.listView);
            ArrayList<String> alist = new ArrayList<>();
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray arr = new JSONArray(url_obj.getString("data"));
                if(arr.length()>0) {
                    for (int x = 0; x < arr.length(); x++) {
                        JSONObject obj = arr.getJSONObject(x);
                        String bts_name = obj.getString("bts_name");
                        String ssa_id = obj.getString("ssa_id");
                        String bts_type = obj.getString("bts_type");
                        String v = bts_name + "\r\n" + ssa_id + " - " + bts_type;
                        alist.add(v);
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("MyBts");
                    alertDialog.setMessage("No Sites are configured.\nGo to ADD BTS and add the sites.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> {
                        alertDialog.dismiss();
                        activity.finish();
                    });
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.list_items_2, alist);
            lview.setAdapter(adapter);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            ViewGroup.LayoutParams params = lview.getLayoutParams();
            params.height= height -200;
            params.width = width;
            lview.setLayoutParams(params);
            lview.requestLayout();
        }
    }
}
