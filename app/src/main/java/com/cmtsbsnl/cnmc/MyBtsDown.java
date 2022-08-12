package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class MyBtsDown extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private TableLayout tl;
    private HashMap<String, String> operators;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bts_down);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

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

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_mybts_down));

        ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(MyBtsDown.this, Navigational.class)));

//        sharedPreferences = getApplicationContext().getSharedPreferences("CnmcPref",MODE_PRIVATE);
//        msisdn  = sharedPreferences.getString("msisdn","");
//        api_key = sharedPreferences.getString("api_key","");
//        access_key = sharedPreferences.getString("acces_key","");

        //        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        String optrnames = sharedPreferences.getString("optrs","hello");
//        System.out.println(optrnames);
        Gson gson = new Gson();
        Map map = gson.fromJson(optrnames, Map.class);
//        System.out.println(map);
        operators = new HashMap<>();
        for(Object opr :map.keySet()){
            String optr_id = opr.toString();
            String optr_name = Objects.requireNonNull(map.get(optr_id)).toString();
            operators.put( optr_id, optr_name);
        }

        MyTask mytask = new MyTask(this);
        mytask.execute(uri_builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String>{
        private final WeakReference<MyBtsDown> activityReference;
        ProgressDialog pd;

        private MyTask(MyBtsDown context) {
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
            MyBtsDown activity = activityReference.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
                conn.setRequestProperty("Context-Type","application/json; utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                JSONObject post_obj = new JSONObject();
//                post_obj.put("msisdn", activity.circle_id);
//                post_obj.put("ssa_id",activity.ssa_id);
//                post_obj.put("criteria",activity.criteria);
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


                /*URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                JSONObject obj = new JSONObject();
                obj.put("msisdn", msisdn);
                obj.put("api_key",api_key);
                obj.put("access_key", access_key);

                String input = obj.toString();
                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();
                os.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String line = "";
                String res = "";
                while ((line = bufferedReader.readLine()) != null) {
                    res += line;
                }
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();
                return res;*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            MyBtsDown activity = activityReference.get();
            pd.dismiss();
            CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            param.setMargins(10,10,10,10);
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                    activity.finish();
                }
                JSONArray arr = new JSONArray(url_obj.getString("data"));
                if(arr.length()>0) {
                    for (int i = 0; i < arr.length(); i++) {
                        final JSONObject obj = new JSONObject(arr.getString(i));
                        String str = Constants.getBtsDownInfo(obj, activity.operators);
                        LinearLayout ll = new LinearLayout(activity);
                        CardView card = new CardView(activity);
                        card.setMaxCardElevation(5);
                        card.setCardElevation(5);
                        card.setLayoutParams(param);
                        card.setPadding(10, 10, 10, 10);
                        card.setRadius(30);
                        card.setUseCompatPadding(true);
                        String site_category = obj.getString("site_category");
                        switch (site_category) {
                            case "SUPER_CRITICAL":
                                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.super_critical));
                                break;
                            case "CRITICAL":
                                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.critical));
                                break;
                            case "IMPORTANT":
                                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.important));
                                break;
                            default:
                                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.normal));
                        }
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
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(activity, ReasonUpdate.class);
                            try {
                                intent.putExtra("bts_id", obj.getString("bts_id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            activity.startActivity(intent);
                        });
                        ll.addView(card);
                        activity.tl.addView(ll);
                    }
                } else {
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
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
