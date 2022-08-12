package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class SsaWiseTraffic extends SessionActivity {
    private TableLayout tl;
    private String circle_id;
    private MyTask myTask;
    private String kpiDate;
    private SharedPreferences sharedPreferences;
    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssa_wise_traffic);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        tl = findViewById(R.id.tbl_lyt);
        Intent intent = getIntent();
        circle_id = intent.getStringExtra("circle_id");
        TextView tv_header = findViewById(R.id.textView1);

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
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_ssa_traffic));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String dt = df.format(cal.getTime());
        System.out.println(dt);
        List<String> dts = new ArrayList<>();
        int mcnt =0;
        while(mcnt<3) {
//            String dt = df.format(cal.getTime());
            cal.add(Calendar.DATE, -1);
            String dt1 = df.format(cal.getTime());
            dts.add(dt1);
//            f_yms.put(dt, dt1);
            mcnt++;
        }


//        System.out.println(dropdown);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dts);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected  = parent.getItemAtPosition(position).toString();
                kpiDate = selected;
                tl.removeViews(1, tl.getChildCount() - 1);
                tv_header.setText(getString(R.string.header_name_text_value,circle_id," Circle Traffic -",selected));
                myTask = new MyTask(SsaWiseTraffic.this);
                myTask.execute(builder.toString(), circle_id, kpiDate);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        MyTask mytask = new MyTask();
//        mytask.execute(strUrl);
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<SsaWiseTraffic> activityReference;
        ProgressDialog pd;

        private MyTask(SsaWiseTraffic context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching Traffic Report...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            SsaWiseTraffic activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("circle_id", params[1]);
                post_obj.put("kpi_date", params[2]);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            SsaWiseTraffic activity = activityReference.get();
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_traffic_erlang);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_traffic_datavol);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_traffic_cnt);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_master_cnt);
            tr1.addView(tv5);
            activity.tl.addView(tr1);
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray obj =new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
                if(obj.length()==0){
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("CNMC Traffic reprot alert ....");
                    alertDialog.setMessage("Yesterday's traffic report will be available after 12:PM\nChoose the different dates from dropdown");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, "OK",
                            (dialog, which) -> alertDialog.dismiss());
                    alertDialog.show();

                } else {
                    df2.setRoundingMode(RoundingMode.UP);
                    for (int i = 0; i < obj.length(); i++) {
                        TableRow tr = new TableRow(activity);
//                    Button btn = new Button(activity);
                        JSONObject erl_data = new JSONObject(obj.getString(i));
                        final String ssa_id = erl_data.getString("ssaid");
                        String erl = erl_data.getString("erl");
                        String data_vol = erl_data.getString("data_vol");
                        String traffic_count = erl_data.getString("traffic_cnt");
                        String master_count = erl_data.getString("master_cnt");

                        TextView tv_ssaid = new TextView(activity);
                        tv_ssaid.setText(ssa_id);
                        tr.addView(tv_ssaid);

                        TextView tv_erl = new TextView(activity);
                        tv_erl.setText(df2.format(Double.parseDouble(erl) / 1000));
                        tr.addView(tv_erl);

                        TextView tv_data = new TextView(activity);
                        tv_data.setText(df2.format(Double.parseDouble(data_vol) / 1024));
                        tr.addView(tv_data);

                        TextView tv_traffic = new TextView(activity);
                        tv_traffic.setText(traffic_count);
                        tr.addView(tv_traffic);

                        TextView tv_master = new TextView(activity);
                        tv_master.setText(master_count);
                        tr.addView(tv_master);

                        activity.tl.addView(tr);
                    }
                /*TableRow tr_f = new TableRow(activity);
                TextView tv_f = new TextView(activity);
                tv_f.setText("");
                tr_f.addView(tv_f);

                TextView tv_t_erl = new TextView(activity);
                tv_t_erl.setText(df2.format(total_erl/1000));
                tr_f.addView(tv_t_erl);

                TextView tv_t_data = new TextView(activity);
                tv_t_data.setText(df2.format(total_data/1024));
                tr_f.addView(tv_t_data);
                tl.addView(tr_f);*/
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //        View Properties
            for(int j =1; j<activity.tl.getChildCount(); j++){
                ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
                for(int i=0; i<tabrows.getChildCount(); i++){
                    View v = tabrows.getChildAt(i);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.rightMargin = 1;
                    params.bottomMargin = 1;
                    if(i ==1 || i==2 || i==3 ||i==4) {
                        params.width = 200;
                    } else {
                        params.width=180;
                    }
                    params.height = 80;
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
