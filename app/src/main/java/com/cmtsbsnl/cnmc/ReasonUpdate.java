package com.cmtsbsnl.cnmc;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ReasonUpdate extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private TableLayout tl;
    private String bts_id ,faults, log_id;
    private List<String> fault_types;
    private Spinner spinner;
    private String faultReason;
    private HashMap<String, String> faultMap;
    private String bts_down_cause;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reason_update);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
            faults  = sharedPreferences.getString("faults","");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        bts_id = intent.getStringExtra("bts_id");
        Gson gson = new Gson();
        Map map = gson.fromJson(faults, Map.class);
        fault_types = new ArrayList<>();
        Iterator itr = map.keySet().iterator();
        faultMap = new HashMap<>();
        while(itr.hasNext()){
            String flt = (String) itr.next();
            fault_types.add(flt);
            faultMap.put(flt, map.get(flt).toString());
//            System.out.println(map.get(itr.next()));
        }

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_bts_down_cause));

        fault_types.add(0,"Select Reason");
        tl = findViewById(R.id.table_lyt);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)-> onBackPressed());
        }

        ImageButton homeBtn =  toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener((View v)->
                startActivity(new Intent(this, Navigational.class)));

        MyTask myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());
    }
    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<ReasonUpdate> activityReference;
        ProgressDialog pd;

        private MyTask(ReasonUpdate context) {
            activityReference = new WeakReference<>(context);
        }

        //        ProgressDialog pd = new ProgressDialog(ReasonUpdate.this);
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
            ReasonUpdate activity = activityReference.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
                conn.setRequestProperty("Context-Type","application/json; utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                JSONObject post_obj = new JSONObject();
                post_obj.put("bts_id", activity.bts_id);
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
            ReasonUpdate activity = activityReference.get();
            pd.dismiss();
            activity.alertDialog = new AlertDialog.Builder(activity).create();
            try {
//                System.out.println(s);
//                Config util = new Config();
                final JSONObject bts_obj = new JSONObject(s);
                JSONObject obj = new JSONObject(bts_obj.getString("data"));

                final String bts_name = obj.getString("bts_name");
//                activity.bts_status_dt = obj.getString("bts_status_dt");
                activity.log_id = obj.getString("log_id");
                activity.tl.setVisibility(View.VISIBLE);
                TableRow tr = new TableRow(activity);
                TextView tv_header = new TextView(activity);
                tv_header.setText(activity.getString(R.string.header_reason_update));
                tv_header.setTextSize(20);
                tr.addView(tv_header);
                activity.tl.addView(tr);

                TableRow tr1 = new TableRow(activity);
                TextView tv1 = new TextView(activity);
                tv1.setText(obj.getString("bts_name"));
                tr1.addView(tv1);
                activity.tl.addView(tr1);

                TableRow tr2 = new TableRow(activity);
                TextView tv2 = new TextView(activity);
                tv2.setText(obj.getString("CIRCLE_NAME")+" / "+obj.getString("ssa_name"));
                tr2.addView(tv2);
                activity.tl.addView(tr2);

                TableRow tr3 = new TableRow(activity);
                TextView tv3 = new TextView(activity);
                tv3.setText(obj.getString("bts_type")+" / "+obj.getString("sitetype")+" / "+obj.getString("vendor_name")+"/ "+obj.getString("site_category"));
                tr3.addView(tv3);
                activity.tl.addView(tr3);

                TableRow tr4 = new TableRow(activity);
                TextView tv4 = new TextView(activity);
                tv4.setText(obj.getString("bts_status_dt") +" ("+ Config.calculateTime(obj.getInt("cumm_down_time"))+")");
                tr4.addView(tv4);
                activity.tl.addView(tr4);

                if(!obj.getString("outsrc_name").equals("NOT APPLICABLE")) {
                    TableRow tr5 = new TableRow(activity);
                    TextView tv5 = new TextView(activity);
                    tv5.setText(obj.getString("outsrc_name"));
                    tr5.addView(tv5);
                    activity.tl.addView(tr5);
                }



                final String lat = obj.getString("bts_latitude");
                final String lng = obj.getString("bts_longitude");
                TableRow tr6 = new TableRow(activity);
                Button btn = new Button(activity);
                btn.setText(activity.getString(R.string.google_map_text));
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> {
                    String geoUriString="geo:"+lat+","+lng+"?q=("+bts_name+")@"+lat+","+lng;
                    Uri geoUri = Uri.parse(geoUriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(mapIntent);
                    }
                });
                tr6.addView(btn);
                activity.tl.addView(tr6);
                final String fault_type=obj.getString("fault_type");

//                TableRow tr7 = new TableRow(ReasonUpdate.this);
//                spinner = new Spinner(ReasonUpdate.this);
                activity.spinner = activity.findViewById(R.id.spinner_fault_reason);
                activity.spinner.setVisibility(View.VISIBLE);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, activity.fault_types);
                activity.spinner.setAdapter(adapter);
//                tr7.addView(spinner);
//                tl.addView(tr7);

                if(fault_type.equals("null") || fault_type.equals("NOT UPDATED")){
                    activity.spinner.setSelection(adapter.getPosition("Select Reason"));
                } else {
                    activity.spinner.setSelection(adapter.getPosition(fault_type));
                }

                Button btn1 = activity.findViewById(R.id.btn);
                btn1.setVisibility(View.VISIBLE);
                btn1.setOnClickListener(v -> {
                    activity.faultReason = activity.spinner.getSelectedItem().toString();
                    activity.bts_down_cause = activity.faultMap.get(activity.faultReason);
//                        System.out.println("Fault->"+faultReason+" "+bts_down_cause);
                    if(activity.faultReason.equals("Select Reason")){
                        activity.alertDialog.setTitle("CNMC Alert ....");
                        activity.alertDialog.setMessage("No reason is selected");
                        activity.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                                (dialog, which) -> activity.alertDialog.dismiss());
                        activity.alertDialog.show();
                    } else{
                        Intent intent1 = new Intent(activity,UpdateFaultReason.class);
                        intent1.putExtra("log_id", activity.log_id);
                        intent1.putExtra("bts_down_cause",activity.bts_down_cause);
                        activity.startActivity(intent1);
                        activity.finish();
                    }
                });


            } catch (JSONException e) {
                activity.alertDialog.setTitle("Error details !");
                activity.alertDialog.setMessage("Proper outage details are not available in the log table\nITPC team is in process of resolving.");
                activity.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.alertDialog.dismiss());
                activity.alertDialog.dismiss();

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
                    params.height = 90;
                    if(v instanceof TextView){
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.blue));
                        v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        ((TextView) v).setGravity(Gravity.CENTER);
//                        ((TextView) v).setPadding(20, 0 ,0 ,0 );
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