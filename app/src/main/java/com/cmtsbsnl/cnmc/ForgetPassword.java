package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ForgetPassword extends AppCompatActivity {

    TableLayout tl;
    String user_msisdn, user_email, user_passwd, user_re_passwd, password;
    EditText msisdn, email, passwd, re_passwd;

//    String strUrl="http://"+ Constants.SERVER_IP+"/cnmc/reset_password.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        tl = findViewById(R.id.forget_table);
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(ForgetPassword.this).create();

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.uri_reset_password));

        msisdn = findViewById(R.id.msisdn);
        email = findViewById(R.id.email);
        passwd = findViewById(R.id.password);
        re_passwd = findViewById(R.id.repassword);
//        Verify Input


        Button btn = findViewById(R.id.btn);
        btn.setText(R.string.resetpassword);
        btn.setOnClickListener(v -> {
            user_msisdn = msisdn.getText().toString();
            user_email = email.getText().toString();
            user_passwd = passwd.getText().toString();
            user_re_passwd = re_passwd.getText().toString();
            if (user_msisdn.length() < 10) {
                msisdn.setError("Mobile Number be minimum 10 digits");
                return;
            } else if (user_passwd.length() < 8) {
                passwd.setError("Password should be minimum 8 characters");
                return;
            } else if (user_re_passwd.length() < 8) {
                re_passwd.setError("Password should be minimum 8 character");
            } else if (!user_passwd.equals(user_re_passwd)) {
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Password and Re-enter Password mismatched");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                passwd.setError("Password/RePassword is mismatched");
                return;
            }
//            password = MD5.getMd5(user_passwd);
            MyTask mytask = new MyTask(this);
            mytask.execute(builder.toString());

        });

        for (int j = 0; j < tl.getChildCount(); j++) {
            ViewGroup tabrows = (ViewGroup) tl.getChildAt(j);
//          tabrows.setBackgroundResource(R.color.blue);
            for (int k = 0; k < tabrows.getChildCount(); k++) {
                View v = tabrows.getChildAt(k);
                ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params1.rightMargin = 1;
                params1.bottomMargin = 1;
                if (k == 0) {
                    params1.width = 80;
                }
                if (v instanceof EditText) {
                    v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    ((EditText) v).setGravity(Gravity.CENTER);
//                                ((EditText) v).setBackgroundColor(Color.BLUE);
                    ((EditText) v).setTextColor(Color.BLACK);
                    v.setPadding(5, 20, 5, 20);
                    ((EditText) v).setTextSize(15);
                    ((EditText) v).getText().clear();
                } else if (v instanceof TextView) {
                    v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    ((TextView) v).setGravity(Gravity.CENTER);
                    v.setBackgroundColor(Color.BLUE);
                    ((TextView) v).setTextColor(Color.WHITE);
                    v.setPadding(5, 20, 5, 20);
                    ((TextView) v).setTextSize(15);
                }
            }
        }
    }
    public static class MyTask extends AsyncTask<String , String, String> {
//        ProgressDialog pd = new ProgressDialog(ForgetPassword.this);

        private final WeakReference<ForgetPassword> activityReference;
        ProgressDialog pd;
        public MyTask(ForgetPassword context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            ForgetPassword activity = activityReference.get();
            pd = new ProgressDialog(activity);
            pd.setTitle("New User creation");
            pd.setMessage("User is creating ....");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ForgetPassword activity = activityReference.get();

            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn" , activity.user_msisdn);
                post_obj.put("email", activity.user_email);
                post_obj.put("password", MD5.getMd5(activity.user_passwd));
                post_obj.put("auth", MD5.getMd5(Constants.getAuth()));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0],"UTF-8"), post_obj.toString());
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            System.out.println(s);
            ForgetPassword activity = activityReference.get();
            AlertDialog alertDialog;
            alertDialog = new AlertDialog.Builder(activity).create();
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String remarks = obj.getString("remarks");
                if(result.equals("true")){
                    alertDialog.setTitle("CNMC Alert ....");
                    alertDialog.setMessage(remarks);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                activity.finish();
                                System.exit(0);
                            });

                } else{
                    String alert = remarks;
                    if(remarks.startsWith("Duplicate")){
                        alert = "Already User exists";
                    }
                    alertDialog.setTitle("CNMC Alert....");
                    alertDialog.setMessage(alert);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                }
                alertDialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
