package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
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
import java.util.Objects;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.cmtsbsnl.cnmc.Authentication_Old.checkInternetConnection;

public class Login extends AppCompatActivity {
    private String username, password;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private String version_no;
    private SharedPreferences.Editor editor;

    static {
        System.loadLibrary("keys");
    }

    public static native String getBaseApi();

   /* public static Context ctx;
    public static final int PERMISSIONS = 10;
    private static final String TAG = "Permissions";
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    }; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        ctx = getApplicationContext();

        Intent intent;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
            editor = sharedPreferences.edit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        alertDialog = new AlertDialog.Builder(Login.this).create();

        intent = getIntent();
        username = intent.getStringExtra("username");
        String passwd = intent.getStringExtra("password");
        version_no = intent.getStringExtra("version_no");
        password = MD5.getMd5(Objects.requireNonNull(passwd));

        if(checkInternetConnection(this)) {
//            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Login.this,
                    instanceIdResult -> {
                        String newToken = instanceIdResult.getToken();
                        editor.putString("FCMId", newToken);
                        editor.apply();
                    });
            /*if (!hasPermissions(getApplicationContext(), permissions)) {
                ActivityCompat.requestPermissions(Login.this, permissions, PERMISSIONS);
            }*/
        } else {
            Toast.makeText(getApplicationContext(), "Unable to connect to Server ", Toast.LENGTH_SHORT).show();
            (new Handler()).postDelayed(this::exitApp, 2000);
        }
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_user_login));

        MyTask mytask = new MyTask(this);
        mytask.execute(builder.toString());
    }

    public void exitApp(){
        finishAffinity();
        System.exit(0);
    }

    private static class MyTask extends AsyncTask<String, String, String>{
        private final WeakReference<Login> activityReference;
        MyTask(Login context){
            activityReference = new WeakReference<>(context);
        }

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Login ...");
            pd.setMessage("Verifying credentials...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            Login activity = activityReference.get();
            try {
                String fmcId = activity.sharedPreferences.getString("FCMId","");

                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
//                For all the trusted certificates
//                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnectionTrustAll(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("X-API_KEY",getBaseApi());
                conn.setRequestProperty("Authorization",Constants.getBasiAuth());
                JSONObject obj = new JSONObject();
                obj.put("username", activity.username);
                obj.put("password", activity.password);
                obj.put("version_no", activity.version_no);
                obj.put("firebase_id",fmcId);
                String input = obj.toString();
                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();
                os.close();
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
            pd.dismiss();
            Login activity = activityReference.get();
//            System.out.println(s);
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String error = obj.getString("error");

                if(result.equals("true")){
                    String web_token = obj.getString("web_token");
                    Integer app_version = obj.getInt("app_version");
                    if(app_version.compareTo(BuildConfig.VERSION_CODE) >0) {
                        // Linkify the message
                        String msg = "A new version has relased , kindly download from the path below\n\nhttps://tinyurl.com/y32cfcqa";
                        final SpannableString spannableString = new SpannableString(msg); // msg should have url to enable clicking
                        Linkify.addLinks(spannableString, Linkify.ALL);

                        final AlertDialog d = new AlertDialog.Builder(activity)
                                .setPositiveButton(android.R.string.ok, null)
                                .setMessage( spannableString )
                                .setCancelable(false)
                                .create();
                        d.setCanceledOnTouchOutside(false);
                        d.show();

                        // Make the textview clickable. Must be called after show()
                        ((TextView) Objects.requireNonNull(d.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());


                    } else {
                        JSONObject userdetails = new JSONObject(obj.getString("remarks"));
                        SharedPreferences.Editor editor = activity.editor;
                        editor.putString("msisdn", activity.username);
                        editor.putString("name", userdetails.getString("name"));
                        editor.putString("desg", userdetails.getString("desg"));
                        editor.putString("hrms_no", userdetails.getString("hrms_no"));
                        editor.putString("circle", userdetails.getString("circle"));
                        editor.putString("email", userdetails.getString("email"));
                        editor.putString("user_privs", userdetails.getString("user_privs"));
                        editor.putString("update_profile", userdetails.getString("update_profile"));
                        editor.putString("status", userdetails.getString("status"));
                        editor.putString("app_version", userdetails.getString("app_version"));
                        editor.putString("circle_id", userdetails.getString("circle_id"));
                        editor.putString("notifications", userdetails.getString("notifications"));
                        editor.putString("sms_notifications", userdetails.getString("sms_notifications"));
                        editor.putString("lvl", userdetails.getString("lvl"));
                        editor.putString("lvl2", userdetails.getString("lvl2"));
                        editor.putString("lvl3", userdetails.getString("lvl3"));
                        editor.putString("admin", userdetails.getString("admin"));
                        editor.putString("ssaname", userdetails.getString("ssaname"));
                        editor.putString("last_login", userdetails.getString("last_login"));
                        editor.putString("web_token", web_token);
                        editor.apply();
//                  User  SSA Details
                        Gson gson = new Gson();
                        JSONArray ssaids = new JSONArray(obj.getString("ssa_ids"));
                        HashMap<String, String> ssas = new HashMap<>();
                        for (int ssaid_index = 0; ssaid_index < ssaids.length(); ssaid_index++) {
                            JSONObject obj1 = new JSONObject(ssaids.getString(ssaid_index));
                            String ssa_id = obj1.getString("ssa_id");
                            String ssa_name = obj1.getString("ssa_name");
                            ssas.put(ssa_id, ssa_name);
                        }
                        //convert to string using gson
                        String hashMapString = gson.toJson(ssas);
                        editor.putString("ssa_ids", hashMapString);

//                    Fault Master details
                        JSONArray faultmaster = new JSONArray(obj.getString("fault_master"));
                        TreeMap<String, String> faults = new TreeMap<>();
                        for (int fault_idx = 0; fault_idx < faultmaster.length(); fault_idx++) {
                            JSONObject obj2 = new JSONObject(faultmaster.getString(fault_idx));
                            String fault_id = obj2.getString("fault_id");
                            String fault_type = obj2.getString("fault_type").trim();
                            faults.put(fault_type, fault_id);
                        }
//                    Faults GSON
                        String faultsMap = gson.toJson(faults);
                        editor.putString("faults", faultsMap);
                        editor.apply();

//                    Operator ID details
                        JSONArray optrmaster = new JSONArray(obj.getString("optr_master"));
                        HashMap<String, String> optrs = new HashMap<>();
                        for (int optridx = 0; optridx < optrmaster.length(); optridx++) {
                            JSONObject obj3 = new JSONObject(optrmaster.getString(optridx));
                            String optr_id = obj3.getString("optr_id");
                            String optr_name = obj3.getString("operator_name");
                            optrs.put(optr_id, optr_name);
                        }
                        String optrMap = gson.toJson(optrs);
                        editor.putString("optrs", optrMap);
                        editor.apply();
                        if(userdetails.getString("user_privs").equals("outSource")){
                            activity.startActivity(new Intent(activity, NavigationalOutsource.class));
                        } else {
                            activity.startActivity(new Intent(activity, Navigational.class));
                        }
                        activity.finish();
                    }
                } else {
                    String remarks = obj.getString("remarks");
                    activity.alertDialog.setTitle("CNMC Alert...");
                    activity.alertDialog.setMessage(remarks+"\n"+error);
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            ((DialogInterface dialog, int which) -> activity.finish()));
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    finish();
//                                }
//                            });
                    activity.alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

