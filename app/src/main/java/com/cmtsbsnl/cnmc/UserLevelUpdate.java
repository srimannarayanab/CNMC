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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class UserLevelUpdate extends SessionActivity {
    private SharedPreferences sharedPreferences ;
    private MyTask myTask;
    private TableLayout tl;
    private Button btn_submit1, btn_submit;
    private String username;
    private String lvl;
    private String lvl2;
    private String lvl3;
    private String user_privs;
    private TextView tv_msisdn, tv_name, tv_desg, tv_hrms, tv_circle, tv_email,tv_last_login, tv_status, tv_user_type;
    private EditText e_msisdn , e_lvl2, e_lvl3;
    private Spinner sp_level;
    private String user_level;
    private UpdateLevel updateLevel;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_level_update);
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
        homeBtn.setOnClickListener(v -> startActivity(new Intent(UserLevelUpdate.this, Navigational.class)));
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_user_details));



//        circle_id = pref.getString("circle_id","");
        e_msisdn = (EditText) findViewById(R.id.search_msisdn);
        btn_submit = (Button) findViewById(R.id.btn);
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
                myTask = new MyTask(UserLevelUpdate.this);
                myTask.execute(uri_builder.toString());
            }
        });
    }

    private static class MyTask extends AsyncTask<String, Void, String> {
        private final WeakReference<UserLevelUpdate> activityReference;
        ProgressDialog pd;

        private MyTask(UserLevelUpdate context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("User Level Update ..");
            pd.setMessage("Getting the user details...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            UserLevelUpdate activity = activityReference.get();
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
            UserLevelUpdate activity = activityReference.get();
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
//                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String remarks = obj.getString("remarks");
                if(result.equals("true")){
                    JSONObject obj1 = new JSONObject(remarks);
//                    making the view visible
                    activity.tl = (TableLayout) activity.findViewById(R.id.tbl_lyt);
                    activity.btn_submit1 = (Button) activity.findViewById(R.id.btn_submit1);

                    activity.tl.setVisibility(View.VISIBLE);
                    activity.btn_submit1.setVisibility(View.VISIBLE);

                    List<String> levels = new ArrayList<>(Arrays.asList("Select-Level","Level-0","Level-1","Level-2","Level-3","Co/Circle"));
//                    user_level = pref.getString("lvl","");
                    activity.user_level = obj1.getString("lvl");
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(activity,R.layout.support_simple_spinner_dropdown_item, levels);
                    activity.sp_level = (Spinner) activity.findViewById(R.id.sp_level);
                    activity.sp_level.setAdapter(adapter1);
//                    System.out.println(activity.user_level);
                    if(activity.user_level.equals("")){
                        activity.sp_level.setSelection(adapter1.getPosition("Select-Level"));
                    } else {
                        activity.sp_level.setSelection(adapter1.getPosition(activity.user_level));
                    }

                    activity.user_privs = obj1.getString("user_privs").equals("circle") ? "Bsnl" :obj1.getString("user_privs");

//                    table view
                    activity.tv_msisdn = (TextView) activity.findViewById(R.id.msisdn);
                    activity.tv_name = (TextView) activity.findViewById(R.id.name);
                    activity.tv_desg = (TextView) activity.findViewById(R.id.desg);
                    activity.tv_hrms =(TextView) activity.findViewById(R.id.hrms_no);
                    activity.tv_circle =(TextView) activity.findViewById(R.id.circle);
                    activity.tv_email = (TextView) activity.findViewById(R.id.email);
                    activity.tv_last_login =(TextView) activity.findViewById(R.id.last_login);
                    activity.e_lvl2 = (EditText) activity.findViewById(R.id.lvl2);
                    activity.e_lvl3 = (EditText) activity.findViewById(R.id.lvl3);
                    activity.tv_status = (TextView) activity.findViewById(R.id.user_status);
                    activity.tv_user_type = (TextView) activity.findViewById(R.id.user_type);

                    activity.tv_msisdn.setText(obj1.getString("msisdn"));
                    activity.tv_name.setText(obj1.getString("name"));
                    activity.tv_desg.setText(obj1.getString("desg"));
                    activity.tv_hrms.setText(obj1.getString("hrms_no"));
                    activity.tv_circle.setText(obj1.getString("circle"));
                    activity.tv_email.setText(obj1.getString("email"));
                    activity.tv_last_login.setText(obj1.getString("last_login"));
                    activity.e_lvl2.setText(obj1.getString("lvl2"));
                    activity.e_lvl3.setText(obj1.getString("lvl3"));
                    activity.tv_status.setText(obj1.getString("user_status"));
                    activity.tv_user_type.setText(activity.user_privs);
                    activity.closeKeyboard();

                    activity.btn_submit1.setOnClickListener(v -> {
                    try {
                        activity.lvl2 = activity.e_lvl2.getText().toString().trim();
                        activity.lvl3 = activity.e_lvl3.getText().toString().trim();
                        activity.lvl = activity.sp_level.getSelectedItem().toString();
                        if(activity.lvl.equals("Level-1") && activity.lvl2.isEmpty()){
                            activity.e_lvl2.setError("Level-2 is compulsory");
                            return;
                        } else if(activity.lvl.equals("Level-1") && activity.lvl3.isEmpty()){
                            activity.e_lvl3.setError("Level-3 is compulsory");
                            return;
                        }
                        Uri.Builder builder2 = new Uri.Builder()
                                .scheme("https")
                                .authority(Constants.getSecureBaseUrl())
                                .appendPath(activity.getString(R.string.url_update_user_level));
                        MyTaskParams params = new MyTaskParams(builder2.toString(), activity.username, activity.lvl, activity.lvl2, activity.lvl3);
                        activity.updateLevel = new UpdateLevel(this.activityReference.get(), params);
                        String res = activity.updateLevel.execute().get();
//                        System.out.println(res);

                        JSONObject url_obj1 = new JSONObject(res);
                        if(!url_obj1.getString("result").equals("true")){
                            Toast.makeText(activity, url_obj1.getString("error"), Toast.LENGTH_SHORT).show();
                            activity.startActivity(new Intent(activity, SesssionLogout.class));
                        }
                        JSONObject obj11 =new JSONObject(url_obj1.getString("data"));
                        String result1 = obj11.getString("result");
                        String error = obj11.getString("error");
                        if(result1.equals("ok")){
                            alertDialog.setTitle("Update user level");
                            alertDialog.setMessage("User level is updated sucessfully");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                            alertDialog.show();
                        } else {
                            alertDialog.setTitle("Update user level");
                            alertDialog.setMessage("Error occured "+error+"\nContact system administrator");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                            alertDialog.show();

                        }
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }

                    });

                    for (int j = 0; j < activity.tl.getChildCount()-3; j++) {
                        ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
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
                    alertDialog.setTitle("User Level Update ..");
                    alertDialog.setMessage("No user exits check wheather the mobile no is correct or not");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                    alertDialog.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view !=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public static class MyTaskParams {
        String lvl, lvl2, lvl3, username, url;

        MyTaskParams(String url, String username, String lvl, String lvl2, String lvl3) {
            this.url = url;
            this.username = username;
            this.lvl = lvl;
            this.lvl2 = lvl2;
            this.lvl3 = lvl3;
        }
    }

}