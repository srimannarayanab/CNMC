package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ChangeUserType extends SessionActivity {
    private EditText e_msisdn;
    private String username;
    private String ch_user_privs;
    private MyTask myTask;
    private Spinner sp_usertype;
    private ArrayAdapter<String> usertype_adapter;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_type);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }


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
                .appendPath(getString(R.string.url_get_user_details));

        e_msisdn = findViewById(R.id.search_msisdn);

        //        user Type spinner/Adapter
        sp_usertype = findViewById(R.id.sp_usertype);
        List<String> user_types = new ArrayList<>(Arrays.asList("Bsnl", "outSource"));
        user_types.add(0,"Select User-Type");
        usertype_adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, user_types);
        sp_usertype.setAdapter(usertype_adapter);

        Button btn_submit = findViewById(R.id.btn);
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
                myTask.execute(builder.toString());
            }
        });
    }

    private static class MyTask extends AsyncTask<String, Void, String> {
        private final WeakReference<ChangeUserType> activityReference;
        ProgressDialog pd;

        private MyTask(ChangeUserType context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Change the user type..");
            pd.setMessage("Geting the user details...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            ChangeUserType activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("username", activity.username);
                post_obj.put("circle_id", activity.sharedPreferences.getString("circle_id",""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
                        post_obj.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            ChangeUserType activity = activityReference.get();
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
                    TableLayout tl = activity.findViewById(R.id.tbl_lyt);
                    Button btn_chg_usrtype = activity.findViewById(R.id.btn_chg_userprivs);

                    tl.setVisibility(View.VISIBLE);
                    btn_chg_usrtype.setVisibility(View.VISIBLE);
                    String user_privs = obj1.getString("user_privs").equals("circle") ? "Bsnl" : obj1.getString("user_privs");
//                    table view
                    TextView tv_msisdn = activity.findViewById(R.id.msisdn);
                    TextView tv_name = activity.findViewById(R.id.name);
                    TextView tv_desg = activity.findViewById(R.id.desg);
                    TextView tv_hrms = activity.findViewById(R.id.hrms_no);
                    TextView tv_circle = activity.findViewById(R.id.circle);
                    TextView tv_email = activity.findViewById(R.id.email);
                    TextView tv_last_login = activity.findViewById(R.id.last_login);
                    TextView tv_lvl = activity.findViewById(R.id.lvl);
                    TextView tv_lvl2 = activity.findViewById(R.id.lvl2);
                    TextView tv_lvl3 = activity.findViewById(R.id.lvl3);
                    TextView tv_status = activity.findViewById(R.id.user_status);
                    TextView tv_user_type = activity.findViewById(R.id.user_type);

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
                    activity.sp_usertype.setSelection(activity.usertype_adapter.getPosition(user_privs));
                    activity.closeKeyboard();



                    btn_chg_usrtype.setOnClickListener(v -> {
                        try {
                            String user_privs_choosen = activity.sp_usertype.getSelectedItem().toString();
                            if(user_privs_choosen.equals("Select User-Type")){
                                TextView errorText = (TextView)activity.sp_usertype.getSelectedView();
                                errorText.setError("");
                                errorText.setTextColor(Color.RED);//just to highlight that this is an error
                                errorText.setText("Select User-Type");//changes the selected item text to this
                                return;
                            }
                            activity.ch_user_privs = user_privs_choosen.equals("Bsnl") ? "circle": user_privs_choosen;
                            Uri.Builder builder = new Uri.Builder()
                                    .scheme("https")
                                    .authority(Constants.getSecureBaseUrl())
                                    .appendPath(activity.getString(R.string.url_change_usertype));
//                            System.out.println(ch_user_privs);
                            ModifyUserType modilfyUserType = new ModifyUserType(activity.getApplicationContext());
                            String res = modilfyUserType.execute(builder.toString(), activity.username, activity.ch_user_privs).get();

                            JSONObject url_obj1 = new JSONObject(res);
                            if(!url_obj1.getString("result").equals("true")){
                                Toast.makeText(activity, url_obj1.getString("error"), Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, SesssionLogout.class));
                            }
                            JSONObject obj11 =new JSONObject(url_obj1.getString("data"));

                            String result1 = obj11.getString("result");
                            String error = obj11.getString("error");
                            if(result1.equals("ok")){
                                alertDialog.setTitle("Change userType..");
                                alertDialog.setMessage("sucessfully changed the user type ");


                            } else {
                                alertDialog.setTitle("Change UserType..");
                                alertDialog.setMessage("Error occured "+error+"\nContact system administrator");

                            }
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> activity.finish());
                            alertDialog.show();
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
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                ((TextView) v).setGravity(Gravity.CENTER);
                                ((TextView) v).setTextSize(15);
                            }
                            if (v instanceof Button) {
                                v.setPadding(0, 0, 0, 0);
                            }
                        }
                    }
                } else {
                    alertDialog.setTitle("Change user type..");
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