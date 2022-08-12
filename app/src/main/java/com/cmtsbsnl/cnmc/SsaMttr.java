package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class SsaMttr extends SessionActivity {
    private TableLayout tl;
    private String ym;
    private MyTask mytask;
    private SharedPreferences sharedPreferences;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssa_mttr);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String circle_id = intent.getStringExtra("circle_id");
        TextView header;
        header = findViewById(R.id.textView1);
        header.setText(getString(R.string.header_ssa_mttr, circle_id));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

        tl = findViewById(R.id.tbl_lyt);
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_ssa_mttr));

//        Get the 3 Month Calaneder data
        SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        ym = df1.format(cal.getTime());
//        System.out.println(ym);
        final HashMap<String, String> dts = new HashMap<>();
        final HashMap<String, String> f_yms = new HashMap<>();
        int mcnt =0;
        while(mcnt<3) {
            String dt = df.format(cal.getTime());
            String dt1 = df1.format(cal.getTime());
            cal.add(Calendar.MONTH, -1);
            dts.put(dt1, dt);
            f_yms.put(dt, dt1);
            mcnt++;
        }

        List<String> yms = new ArrayList<>(dts.keySet());
        Collections.sort(yms, Collections.reverseOrder());
        List<String> dropdown = new ArrayList<>();
        for(String k: yms){
            dropdown.add(dts.get(k));
        }

//        System.out.println(dropdown);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected  = parent.getItemAtPosition(position).toString();
                ym = f_yms.get(selected);
                tl.removeViews(1, tl.getChildCount() - 1);
//                System.out.println(ym);
                mytask = new MyTask(SsaMttr.this);
                mytask.execute(builder.toString(), ym, circle_id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<SsaMttr> activityReference;
        ProgressDialog pd ;

        private MyTask(SsaMttr context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching Report...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            SsaMttr activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("ym", params[1]);
                post_obj.put("circle_id", params[2]);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            SsaMttr activity = activityReference.get();
//            System.out.println(s);
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_instances);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_duration_hrs);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_unique_bts);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_mttr);
            tr1.addView(tv5);
            /*TextView tv6 = new TextView(activity);
            tv6.setText("Rank");
            tr1.addView(tv6);*/
            activity.tl.addView(tr1);

            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray obj =new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
                for(int i=0; i<obj.length(); i++){
                    TableRow tr = new TableRow(activity);
                    JSONObject cat_data = new JSONObject(obj.getString(i));
                    final String ssaid = cat_data.getString("ssaid");
                    String count = cat_data.getString("site_count");
                    String dur = cat_data.getString("dur");
                    String instances = cat_data.getString("instance");
                    String uniq_bts = cat_data.getString("distinct_btsid");
                    String  mttr = cat_data.getString("mttr");

                    TextView tv_ssa = new TextView(activity);
                    tv_ssa.setText(ssaid);
                    tr.addView(tv_ssa);

                    TextView tv_sc = new TextView(activity);
                    tv_sc.setText(instances);
                    tr.addView(tv_sc);

                    TextView tv_dur = new TextView(activity);
                    tv_dur.setText(dur);
                    tr.addView(tv_dur);

//                    Here where are assing the sites who are contributing the outages
                    TextView tv_ubts = new TextView(activity);
                    tv_ubts.setText(activity.getString(R.string.down_wrt_total,uniq_bts,count));
                    tr.addView(tv_ubts);

                    TextView tv_mttr = new TextView(activity);
                    tv_mttr.setText(mttr);
                    tr.addView(tv_mttr);

                    activity.tl.addView(tr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //        View Properties
            for(int j =0; j<activity.tl.getChildCount(); j++){
                ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
                for(int i=0; i<tabrows.getChildCount(); i++){
                    View v = tabrows.getChildAt(i);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.rightMargin = 1;
                    params.bottomMargin = 1;
                    if(j==0){
                        params.height = 150;
                    } else {
                        params.height = 80;
                    }
                    if(v instanceof TextView){
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                        v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        ((TextView) v).setGravity(Gravity.CENTER);
                        ((TextView) v).setTextSize(15);
                    }
                    if(v instanceof Button){
                        v.setPadding(0,0,0,0);
                    }
                }
            }
        }
    }
}
