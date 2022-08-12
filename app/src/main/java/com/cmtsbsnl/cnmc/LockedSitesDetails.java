package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

public class LockedSitesDetails extends AppCompatActivity {
    String circle_id, ssa_id, vendor_id;
    TableLayout tl;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked_sites_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        circle_id = intent.getStringExtra("circle_id");
        ssa_id = intent.getStringExtra("ssa_id");
        vendor_id = intent.getStringExtra("vendor_id");

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_locked_sites_details));

        TextView header = findViewById(R.id.textView1);
        String htxt = vendor_id;
        if(vendor_id.equals("%")){
            htxt = "All Vendors";
        }
        header.setText(getString(R.string.header_locked_details,circle_id,htxt));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
//                finish();
            });
        }

        ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(LockedSitesDetails.this, Navigational.class)));

//        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        MyTask mytask = new MyTask(this);
        mytask.execute(uri_builder.toString());
    }


    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<LockedSitesDetails> activityReference;
        ProgressDialog pd;

        private MyTask(LockedSitesDetails context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching Alarms");
            pd.setMessage("Processing...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            LockedSitesDetails activity = activityReference.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
                conn.setRequestProperty("Context-Type","application/json; utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                JSONObject post_obj = new JSONObject();
                post_obj.put("circle_id", activity.circle_id);
                post_obj.put("ssa_id",activity.ssa_id);
                post_obj.put("vendor_id",activity.vendor_id);
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
            LockedSitesDetails activity = activityReference.get();
            pd.dismiss();

            CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            param.setMargins(10,10,10,10);
            try {
                JSONObject flts_obj = new JSONObject(s);
                JSONArray arr = new JSONArray(flts_obj.getString("data"));
                if(arr.length()>0) {
                    for (int i = 0; i < arr.length(); i++) {
                        final JSONObject obj = new JSONObject(arr.getString(i));
                        String reason = !obj.getString("fault_type").equals("null") ? obj.getString("fault_type") : "";
                        StringBuilder str = new StringBuilder();
                        str.append("Bts Name :").append(obj.getString("bts_name"));
                        str.append("\n");
                        str.append("Ssa Name :").append(obj.getString("ssa_name"));
                        str.append("\n");
                        str.append("Make :").append(obj.getString("make"));
                        str.append("\n");
                        str.append("Reason :").append(reason);
                        str.append("\n");
                        /*if (!obj.getString("fault_updated_by").isEmpty() &&
                                sfdt.parse(obj.getString("bts_status_dt")).before(sfdt.parse(obj.getString("fault_update_date")))) {
                            str.append("updated_by :" + obj.getString("fault_updated_by"));
                            str.append("\n");
                            str.append("updated_date:" + obj.getString("fault_update_date"));
                        }*/

                        LinearLayout ll = new LinearLayout(activity);
                        CardView card = new CardView(activity);
                        card.setMaxCardElevation(5);
                        card.setCardElevation(5);
                        card.setLayoutParams(param);
                        card.setPadding(10, 10, 10, 10);
                        card.setRadius(30);
                        card.setUseCompatPadding(true);
//                    String site_category = obj.getString("site_category");
                        TextView tv = new TextView(activity);
//                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
                        tv.setTextColor(Color.BLACK);
                        tv.setGravity(Gravity.CENTER);
                        tv.setPadding(15, 15, 15, 15);
                        tv.setText(str);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        tv.setTypeface(Typeface.MONOSPACE);
                        card.addView(tv);
                        ll.addView(card);
                        activity.tl.addView(ll);
                    }
                } else{
                    AlertDialog alertDialog ;
                    alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("Info...");
                    alertDialog.setMessage("No Bts are down");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> activity.finish());
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
