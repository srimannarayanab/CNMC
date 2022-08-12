package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class OmcrProcess extends SessionActivity {
    private TableLayout tl;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omcr_process);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        TextView hometextview = findViewById(R.id.textView1);
        hometextview.setText(getString(R.string.notification,"Last OMCR Process dates"));

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_omcr_process));

        tl = findViewById(R.id.tbl_lyt);
        MyTask myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
        pullToRefresh.setOnRefreshListener(() -> {
            tl.removeAllViews();
            new MyTask(this).execute(uri_builder.toString());
            pullToRefresh.setRefreshing(false);
        });
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<OmcrProcess> activityReference;
        ProgressDialog pd ;
        private MyTask(OmcrProcess context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching processed dates ...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            OmcrProcess activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("circle_id", activity.sharedPreferences.getString("circle_id",""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            OmcrProcess activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_omcr);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_startdate);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_enddate);
            tr1.addView(tv4);
            activity.tl.addView(tr1);
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray obj =new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
                for (int i = 0; i < obj.length(); i++) {
//                    TableRow tr = new TableRow(activity);
//                    Button btn = new Button(activity);
                    JSONObject obj1 = new JSONObject(obj.getString(i));
                    final String circle_id = obj1.getString("circle_id");
                    String omcr_name = obj1.getString("omcr_name");
                    String start_date = obj1.getString("start_dt");
                    String end_date = obj1.getString("end_dt");
                    TableRow tr_1 = new TableRow(activity);
                    TextView tv1_1 = new TextView(activity);
                    tv1_1.setText(circle_id);
                    tr_1.addView(tv1_1);
                    TextView tv1_2 = new TextView(activity);
                    tv1_2.setText(omcr_name);
                    tr_1.addView(tv1_2);
                    TextView tv1_3 = new TextView(activity);
                    tv1_3.setText(start_date);
                    tr_1.addView(tv1_3);
                    TextView tv1_4 = new TextView(activity);
                    tv1_4.setText(end_date);
                    tr_1.addView(tv1_4);
                    activity.tl.addView(tr_1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //        View Properties
            for (int j = 0; j < activity.tl.getChildCount(); j++) {
                ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
                for (int i = 0; i < tabrows.getChildCount(); i++) {
                    View v = tabrows.getChildAt(i);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.rightMargin = 1;
                    params.bottomMargin = 1;
                    params.width = 200;
                    params.height = 120;
                    if (v instanceof TextView) {
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                        v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                        ((TextView) v).setGravity(Gravity.CENTER);
                        ((TextView) v).setTextSize(14);
                    }
                }
            }
        }
    }
}