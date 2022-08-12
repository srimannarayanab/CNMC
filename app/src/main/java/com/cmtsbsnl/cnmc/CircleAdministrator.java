package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;

import androidx.core.content.ContextCompat;

public class CircleAdministrator extends SessionActivity {
    private TableLayout tl;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_administrator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        strUrl = "http://"+Constants.SERVER_IP+"/cnmc/circle_administrators.php";
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_circle_administrators));
        tl= findViewById(R.id.tbl_lyt);
        MyTask myTask = new MyTask(this);
        myTask.execute(builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String>{
        private final WeakReference<CircleAdministrator> activityReference;
        ProgressDialog pd;

        private MyTask(CircleAdministrator context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Executing...");
            pd.setMessage("Fetching Circle administrators list");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            CircleAdministrator activity = activityReference.get();
            pd.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
//            System.out.println(s);
            TableRow tr = new TableRow(activity);
            TextView tv_circle_id = new TextView(activity);
            tv_circle_id.setText(R.string.header_circle);
            tr.addView(tv_circle_id);

            TextView tv_name = new TextView(activity);
            tv_name.setText(R.string.header_username);
            tr.addView(tv_name);

            TextView tv_msisdn = new TextView(activity);
            tv_msisdn.setText(R.string.header_msisdn);
            tr.addView(tv_msisdn);

            TextView tv_desg = new TextView(activity);
            tv_desg.setText(R.string.header_userdesg);
            tr.addView(tv_desg);

            activity.tl.addView(tr);

//          Read the response


//            Iterating over the rows
            try {
                JSONObject resp = new JSONObject(s);
                System.out.println(resp);
                if(resp.getString("result").equals("true")){
                    JSONArray arr = new JSONArray(resp.getString("data"));
                    for(int i=0; i<arr.length(); i++){
                        JSONObject obj = new JSONObject(arr.getString(i));
                        TableRow tr1 = new TableRow(activity);
                        TextView tv1 = new TextView(activity);
                        tv1.setText(obj.getString("circle_id"));
                        tr1.addView(tv1);

                        TextView tv2 = new TextView(activity);
                        tv2.setText(obj.getString("name"));
                        tr1.addView(tv2);

                        TextView tv3 = new TextView(activity);
                        tv3.setText(obj.getString("msisdn"));
                        tr1.addView(tv3);

                        TextView tv4 = new TextView(activity);
                        tv4.setText(obj.getString("desg"));
                        tr1.addView(tv4);
                        activity.tl.addView(tr1);
                    }
                } else{
                    alertDialog.setTitle("Circle Administrators..");
                    alertDialog.setMessage(resp.getString("error"));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            for(int j =0; j<activity.tl.getChildCount(); j++) {
                ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
                for (int i = 0; i < tabrows.getChildCount(); i++) {
                    View v = tabrows.getChildAt(i);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.rightMargin = 1;
                    params.bottomMargin = 1;
                    if (v instanceof TextView) {
                        v.setPadding(0, 0, 0, 0);
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                        v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        ((TextView) v).setGravity(Gravity.CENTER);
                        ((TextView) v).setTextSize(15);
                    }
                }
            }


        }

        @Override
        protected String doInBackground(String... strings) {
            CircleAdministrator activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("auth",MD5.getMd5(Constants.getAuth()));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}