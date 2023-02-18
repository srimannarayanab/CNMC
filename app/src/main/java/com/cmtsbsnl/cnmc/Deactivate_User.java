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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class Deactivate_User extends SessionActivity {
    private EditText e_msisdn;
    private String username;
    private MyTask myTask;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivate_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(Deactivate_User.this, Navigational.class)));

//        strUrl="http://"+ Constants.SERVER_IP+"/cnmc/getUserDetails.php";
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_user_details));

        e_msisdn = (EditText) findViewById(R.id.search_msisdn);
        Button btn_submit = (Button) findViewById(R.id.btn);
        btn_submit.setOnClickListener(v -> {
            username = e_msisdn.getText().toString().trim();
            boolean valid = true;
            if(username.isEmpty()){
                e_msisdn.setError("msisdn can not be null");
                valid = false;

            } else if(username.length()<10){
                e_msisdn.setError("msisdn length should be >=10");
                valid = false;
            }
            if(valid){
                myTask = new MyTask(this);
                myTask.execute(uri_builder.toString());

            }
        });

    }

    private static class MyTask extends AsyncTask<String, Void, String>{
        private final WeakReference<Deactivate_User> activityReference;
        ProgressDialog pd;

        private MyTask(Deactivate_User context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Deactive User..");
            pd.setMessage("Geting the user details...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            Deactivate_User activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                post_obj.put("username", activity.username);
                post_obj.put("circle_id", activity.sharedPreferences.getString("circle_id", ""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString() );
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Deactivate_User activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONObject obj =new JSONObject(url_obj.getString("data"));
                String result = obj.getString("result");
                String remarks = obj.getString("remarks");
                if(result.equals("true")){
                    JSONObject obj1 = new JSONObject(remarks);
//                    making the view visible
                    TableLayout tl = (TableLayout) activity.findViewById(R.id.tbl_lyt);
                    Button btn_submit1 = (Button) activity.findViewById(R.id.btn_submit1);
                    Button btn_submit2 = (Button) activity.findViewById(R.id.btn_submit2);

                    tl.setVisibility(View.VISIBLE);

                    if(obj1.getString("status").equals("B")
                        || obj1.getString("status").equals("N")
                        || obj1.getString("status").equals("D")){
//                        System.out.println(obj1.getString("status"));
                        btn_submit2.setVisibility(View.VISIBLE);
                    } else {
                        btn_submit1.setVisibility(View.VISIBLE);
                        btn_submit2.setVisibility(View.INVISIBLE);
                    }

                    String user_privs = obj1.getString("user_privs").equals("circle") ? "Bsnl" : obj1.getString("user_privs");

//                    table view
                    TextView tv_msisdn = (TextView) activity.findViewById(R.id.msisdn);
                    TextView tv_name = (TextView) activity.findViewById(R.id.name);
                    TextView tv_desg = (TextView) activity.findViewById(R.id.desg);
                    TextView tv_hrms = (TextView) activity.findViewById(R.id.hrms_no);
                    TextView tv_circle = (TextView) activity.findViewById(R.id.circle);
                    TextView tv_email = (TextView) activity.findViewById(R.id.email);
                    TextView tv_last_login = (TextView) activity.findViewById(R.id.last_login);
                    TextView tv_lvl = (TextView) activity.findViewById(R.id.lvl);
                    TextView tv_lvl2 = (TextView) activity.findViewById(R.id.lvl2);
                    TextView tv_lvl3 = (TextView) activity.findViewById(R.id.lvl3);
                    TextView tv_status = (TextView) activity.findViewById(R.id.user_status);
                    TextView tv_user_type = (TextView) activity.findViewById(R.id.user_type);

                    tv_msisdn.setText(obj1.getString("msisdn"));
                    tv_name.setText(obj1.getString("name"));
                    tv_desg.setText(obj1.getString("desg"));
                    tv_hrms.setText(obj1.getString("hrms_no"));
                    tv_circle.setText(obj1.getString("circle"));
                    tv_email.setText(obj1.getString("email"));
                    tv_last_login.setText(obj1.getString("last_login"));
                    tv_lvl.setText(obj1.getString("lvl"));
                    tv_lvl2.setText(obj1.getString("lvl2"));
                    tv_lvl3.setText(obj1.getString("lvl3"));
                    tv_status.setText(obj1.getString("user_status"));
                    tv_user_type.setText(user_privs);
                    activity.closeKeyboard();

//                    Deactivate the user
                    btn_submit1.setOnClickListener(v -> {
                        try {
                            Uri.Builder builder1 = new Uri.Builder()
                                    .scheme("https")
                                    .authority(Constants.getSecureBaseUrl())
                                    .appendPath(activity.getString(R.string.url_deactivate_user));
                            UserActivateDeactivate userActivateDeactivate = new UserActivateDeactivate(activity);
                            String res = userActivateDeactivate.execute( builder1.toString(), activity.username).get();
                            JSONObject url_obj1 = new JSONObject(res);
                            if(!url_obj1.getString("result").equals("true")){
                                Toast.makeText(activity, url_obj1.getString("error"), Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, SesssionLogout.class));
                            }
                            JSONObject obj11 =new JSONObject(url_obj1.getString("data"));
//                            JSONObject obj11 = new JSONObject(res);
                            String result1 = obj11.getString("result");
                            String error = obj11.getString("error");
                            if(result1.equals("ok")){
                                alertDialog.setTitle("Deactive Users..");
                                alertDialog.setMessage("sucessfully deactivated user ");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                                alertDialog.show();
                            } else {
                                alertDialog.setTitle("Deactive Users..");
                                alertDialog.setMessage("Error occured "+error+"\nContact system administrator");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                                alertDialog.show();

                            }
                        } catch (ExecutionException | InterruptedException | JSONException e) {
                            e.printStackTrace();
                        }

                    });

//                    Activate the user

                    btn_submit2.setOnClickListener(v -> {
                        try {
                            Uri.Builder builder1 = new Uri.Builder()
                                    .scheme("https")
                                    .authority(Constants.getSecureBaseUrl())
                                    .appendPath(activity.getString(R.string.url_activate_user));
                            UserActivateDeactivate userActivateDeactivate = new UserActivateDeactivate(activity.getApplicationContext());
                            String res = userActivateDeactivate.execute(builder1.toString(), activity.username).get();
                            JSONObject url_obj1 = new JSONObject(res);
                            if(!url_obj1.getString("result").equals("true")){
                                Toast.makeText(activity, url_obj1.getString("error"), Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, SesssionLogout.class));
                            }
                            JSONObject obj112 =new JSONObject(url_obj1.getString("data"));
                            String result12 = obj112.getString("result");
                            String error = obj112.getString("error");
                            if(result12.equals("ok")){
                                alertDialog.setTitle("Activate Users..");
                                alertDialog.setMessage("sucessfully Activated the user ");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                                alertDialog.show();
                            } else {
                                alertDialog.setTitle("Activate Users..");
                                alertDialog.setMessage("Error occured "+error+"\nContact system administrator");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                                alertDialog.show();

                            }
                        } catch (ExecutionException | InterruptedException | JSONException e) {
                            e.printStackTrace();
                        }

                    });


                    for (int j = 0; j < tl.getChildCount(); j++) {
                        ViewGroup tabrows = (ViewGroup) tl.getChildAt(j);
                        for (int i = 0; i < tabrows.getChildCount(); i++) {
                            View v = tabrows.getChildAt(i);
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                            params.rightMargin = 1;
                            params.bottomMargin = 1;
                            params.height = 70;
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                                ((TextView) v).setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                                ((TextView) v).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                ((TextView) v).setGravity(Gravity.CENTER);
                                ((TextView) v).setTextSize(15);
                            }
                            if (v instanceof Button) {
                                ((Button) v).setPadding(0, 0, 0, 0);
                            }
                        }
                    }
                } else {
                    alertDialog.setTitle("Activate/Deactive Users..");
                    alertDialog.setMessage("No user exits check wheather the mobile no is correct or not");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                    alertDialog.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //        View Properties


        }
    }

    public void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view !=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
}