package com.cmtsbsnl.cnmc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class AddBtsAdministrator extends SessionActivity {

    private SharedPreferences sharedPreferences;
    private MyTask myTask;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bts_administrator);

//        EditText ed_msisdn1, ed_msisdn2, ed_msisdn3;
//        String msisdn1, msisdn2, msisdn3;
        Button btn;
        String bts_id, bts_name, circle_name, ssa_name, bts_type, site_type, vendor_name, site_category;
        TableLayout tl;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

//        Get all the intent extras
        Intent intent = getIntent();
        bts_id = intent.getExtras().getString("bts_id","");
        bts_name = intent.getExtras().getString("bts_name","");
        circle_name = intent.getExtras().getString("circle_name","");
        ssa_name = intent.getExtras().getString("ssa_name","");
        bts_type = intent.getExtras().getString("bts_type","");
        site_type = intent.getExtras().getString("site_type","");
        vendor_name = intent.getExtras().getString("vendor_name","");
        site_category = intent.getExtras().getString("site_category","");

        String usr = sharedPreferences.getString("msisdn", "");



//        strUrl = "http://"+Constants.SERVER_IP+"/cnmc/add_bts_admin.php";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)->onBackPressed());
        }

        ImageButton homeBtn =  toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener((View v)->
                startActivity(new Intent(AddBtsAdministrator.this, Navigational.class))
        );

        tl =  findViewById(R.id.table_lyt);
        TableRow tr = new TableRow(AddBtsAdministrator.this);
        TextView tv_header = new TextView(AddBtsAdministrator.this);
        tv_header.setText(R.string.header_mybts_add_site_details);
        tv_header.setTextSize(20);
        tr.addView(tv_header);
        tl.addView(tr);

        TableRow tr1 = new TableRow(AddBtsAdministrator.this);
        TextView tv1 = new TextView(AddBtsAdministrator.this);
        tv1.setText(bts_name);
        tr1.addView(tv1);
        tl.addView(tr1);

        TableRow tr2 = new TableRow(AddBtsAdministrator.this);
        TextView tv2 = new TextView(AddBtsAdministrator.this);
//        tv2.setText(circle_name+" / "+ssa_name);
        tv2.setText(getString(R.string.circle_ssaname, circle_name, ssa_name));
        tr2.addView(tv2);
        tl.addView(tr2);

        TableRow tr3 = new TableRow(AddBtsAdministrator.this);
        TextView tv3 = new TextView(AddBtsAdministrator.this);
//        tv3.setText(bts_type+" / "+site_type+" / "+vendor_name+"/ "+site_category);
        tv3.setText(getString(R.string.bts_site_verndor_cat, bts_type, site_type, vendor_name, site_category));

        tr3.addView(tv3);
        tl.addView(tr3);

        btn =  findViewById(R.id.btn);
        btn.setOnClickListener((View v)->{
                boolean valid = true;
                final EditText ed_msisdn1 = findViewById(R.id.msisdn1);
                final EditText ed_msisdn2 = findViewById(R.id.msisdn2);
                final EditText ed_msisdn3 = findViewById(R.id.msisdn3);

                final String msisdn1 = ed_msisdn1.getText().toString().trim();
                final String msisdn2 = ed_msisdn2.getText().toString().trim();
                final String msisdn3 = ed_msisdn3.getText().toString().trim();

//                System.out.println("Hello->"+bts_id+";"+msisdn1+";"+msisdn2+";"+msisdn3);

                if(msisdn1.isEmpty()){
                    valid = false;
                    ed_msisdn1.setError("Atleast one number to be mapped");
                } else if(msisdn1.length()<10){
                    valid=false;
                    ed_msisdn1.setError("Mobile number can not be <10 digits");
                } else if(!msisdn2.equals("") && msisdn2.length()<10){
                    valid= false;
                    ed_msisdn2.setError("Mobile number can not be <10 digits");
                } else if(!msisdn3.isEmpty() && msisdn3.length()<10){
                    valid=false;
                    ed_msisdn3.setError("Mobile number can not be <10 digits");
                }

                if(valid){
                    Uri.Builder builder = new Uri.Builder()
                            .scheme("https")
                            .authority(Constants.getSecureBaseUrl())
                            .appendPath(getString(R.string.url_add_bts_admin));

                    JSONObject input_jsonobj = new JSONObject();
                  try {
                    input_jsonobj.put("msisdn", sharedPreferences.getString("msisdn",""));
                    input_jsonobj.put("msisdn1", msisdn1);
                    input_jsonobj.put("msisdn2", msisdn2);
                    input_jsonobj.put("msisdn3", msisdn3);
                    input_jsonobj.put("bts_id", bts_id);
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }


                  myTask = new MyTask(AddBtsAdministrator.this);
                    myTask.execute(builder.toString(), input_jsonobj.toString());
                }
        });

        for(int j =0; j<tl.getChildCount(); j++){
            ViewGroup tabrows = (ViewGroup) tl.getChildAt(j);
            for(int i=0; i<tabrows.getChildCount(); i++){
                View v = tabrows.getChildAt(i);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.rightMargin = 1;
                params.bottomMargin = 1;
                params.height = 90;
                if(v instanceof TextView){
                    ((TextView) v).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
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

    public static class MyTask extends AsyncTask<String, Void, String>{
        private final WeakReference<AddBtsAdministrator> activityReference;

        public MyTask(AddBtsAdministrator context) {
            activityReference = new WeakReference<>(context);
        }

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Add to MyBTS");
            pd.setMessage("MyBts Configuration is going.. please wait");
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            AddBtsAdministrator activity = activityReference.get();
          try {
            return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"), strings[1]);
          } catch (Exception e) {
            e.printStackTrace();
          }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
          System.out.println(s);
            AddBtsAdministrator activity = activityReference.get();
            pd.dismiss();
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String error = obj.getString("error");
                activity.alertDialog = new AlertDialog.Builder(activity).create();
                activity.alertDialog.setTitle("Bts add by Admin");
                if(result.equals("true")){
                    activity.alertDialog.setMessage("Successfully added");
                    activity.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> {
                        Intent intent = new Intent(activity, Mybts_Leftout.class);
                        activity.startActivity(intent);
                        activity.finish();
                    });

                } else {
                    activity.alertDialog.setMessage("Error occured while adding the bts, try once again !\n"+error);
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ok", (dialog, which) -> activity.alertDialog.dismiss());
                }
                activity.alertDialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}