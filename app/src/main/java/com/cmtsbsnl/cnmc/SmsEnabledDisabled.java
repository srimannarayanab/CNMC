package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SmsEnabledDisabled extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Intent intent;
    AlertDialog alertDialog;
    String strUrl="http://"+ Constants.SERVER_IP+"/cnmc/enable_disable_sms.php";
    String msisdn, sms_status;
    MyTask mytask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences("CnmcPref",MODE_PRIVATE);
        msisdn = pref.getString("msisdn","");

        intent = getIntent();
        sms_status = intent.getStringExtra("sms_status");

        mytask = new MyTask();
        mytask.execute(strUrl);

    }

    private class MyTask extends AsyncTask<String, String, String> {
        ProgressDialog pd = new ProgressDialog(SmsEnabledDisabled.this);
        @Override
        protected void onPreExecute() {
            pd.setTitle("Updating the Notifications status...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStream outputStream = con.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("msisdn", "UTF-8") + "=" + URLEncoder.encode(msisdn, "UTF-8")+"&"+
                        URLEncoder.encode("sms_status", "UTF-8") + "=" + URLEncoder.encode(sms_status, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String line = "";
                String res = "";
                while ((line = bufferedReader.readLine()) != null) {
                    res += line;
                }
                bufferedReader.close();
                inputStream.close();
                con.disconnect();
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            System.out.println(s);
            alertDialog = new AlertDialog.Builder(SmsEnabledDisabled.this).create();
            try {
                JSONObject obj = new JSONObject(s);
//                System.out.println(obj.getString("result"));
                if(obj.getString("result").equals("ok")){
                    alertDialog.setTitle("CNMC Alert ....");
                    alertDialog.setMessage("SMS status toggled sucessfully");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    editor = pref.edit();
                                    editor.putString("notifications",sms_status);
                                    editor.commit();
                                    finish();
                                }
                            });
                    alertDialog.show();
                } else{
                    alertDialog.setTitle("CNMC Alert ....");
                    alertDialog.setMessage(obj.getString("error")+"\n Try again ");
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    finish();
                                }
                            });
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}