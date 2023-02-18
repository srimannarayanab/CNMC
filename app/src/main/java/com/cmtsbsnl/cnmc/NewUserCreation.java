package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NewUserCreation extends AppCompatActivity {

    private AlertDialog alertDialog;
    private String user_name ,user_desg, user_hrms, user_email, user_msisdn, passwd, user_circle, user_ssaname, access_key, user_type;
    private Spinner sp_desg,sp_ssas, sp_circles, sp_usertype;
    private List<String> desgs;
    private List<String> circles;
    private List<String> ssas;
    private ArrayAdapter<String> ssa_adapter;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_creation);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.uri_add_new_user));

        sp_circles = findViewById(R.id.sp_circle);
        sp_desg = findViewById(R.id.sp_desg);
        sp_ssas = findViewById(R.id.sp_ssa);
        sp_usertype = findViewById(R.id.sp_usertype);
//        sp_circles.setOnItemSelectedListener(NewUserCreation.this);

//        final SharedPreferences preferences = getApplicationContext().getSharedPreferences("CnmcPref", Context.MODE_PRIVATE);

        //        User designations
        GetUserDesignations userdesg = new GetUserDesignations(getApplicationContext());
        try {
            String designations = userdesg.execute().get();
            JSONObject user_desgs = new JSONObject(String.valueOf(designations));
            JSONArray arr = new JSONArray(user_desgs.getString("data"));
            desgs = new ArrayList<>();
            desgs.add("Select");
            for(int a=0; a<arr.length(); a++){
                desgs.add((String) arr.get(a));
            }
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

//        Get the Circles from NTMSDB Database
        GetCircles getCircles = new GetCircles(getApplicationContext());
        try {
            String strCircles = getCircles.execute().get();
            JSONObject circles_obj = new JSONObject(strCircles);
            JSONArray circles_arr = new JSONArray(circles_obj.getString("data"));
            circles = new ArrayList<>();
            circles.add("Select Circle");
            for(int c=0; c<circles_arr.length(); c++){
                circles.add((String) circles_arr.get(c));
            }
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }


        alertDialog = new AlertDialog.Builder(NewUserCreation.this).create();
//        https://stackoverflow.com/questions/9370293/add-a-remember-me-checkbox

        ssas = new ArrayList<>();
        ssas.add(0, "Select SSA");

        TableLayout tl = findViewById(R.id.userdetails_table);
        final EditText ed = findViewById(R.id.username);
//        final EditText ed1 = findViewById(R.id.desg);
//        Spinner sp1 = findViewById(R.id.sp_desg);
        final EditText ed2 = findViewById(R.id.hrms_no);
        final EditText ed3 = findViewById(R.id.email);
        final EditText ed4 = findViewById(R.id.msisdn);
        final EditText ed5 = findViewById(R.id.password);
        final EditText ed6 = findViewById(R.id.repassword);

//        Designation adapter
        ArrayAdapter<String> desg_adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item,desgs);
        sp_desg.setAdapter(desg_adapter);
//        desg is designation of the user if data is already available
//        sp_desg.setSelection(desg_adapter.getPosition(preferences.getString("desg","")));


//        user Type spinner/Adapter
        List<String> user_types = new ArrayList<>(Arrays.asList("Bsnl", "OutSourcing", "IpVendor"));
        user_types.add(0,"Select User-Type");
        ArrayAdapter<String> usertype_adapter = new ArrayAdapter<>(NewUserCreation.this, R.layout.support_simple_spinner_dropdown_item, user_types);
        sp_usertype.setAdapter(usertype_adapter);

//        Circle Spinner/Adapter
        ArrayAdapter<String> circle_adapter = new ArrayAdapter<>(NewUserCreation.this, R.layout.support_simple_spinner_dropdown_item, circles);
        sp_circles.setAdapter(circle_adapter);
//      Print all the shared preferences
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }

        sp_circles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sp1= String.valueOf(sp_circles.getSelectedItem());
//        Toast.makeText(this, "Circle selected is "+sp1, Toast.LENGTH_SHORT).show();
                GetCircleSSAs getssas = new GetCircleSSAs(getApplicationContext());
                try {
                    String cnmc_ssas = getssas.execute(sp1).get();
                    JSONObject ssa_obj = new JSONObject(cnmc_ssas);
                    JSONArray arr = new JSONArray(ssa_obj.getString("data"));
                    ssas = new ArrayList<>();
//            circles.add("Select");
                    for(int a=0; a<arr.length(); a++){
                        ssas.add((String) arr.get(a));
                    }
                    ssas.add(0, "Select SSA");
                    ssa_adapter = new ArrayAdapter<>(NewUserCreation.this, R.layout.support_simple_spinner_dropdown_item, ssas);
                    sp_ssas.setAdapter(ssa_adapter);
                } catch (ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //        Get the user details from the EditBox
        final Button updtBtn = findViewById(R.id.btn);
        updtBtn.setOnClickListener(v -> {
            user_name = ed.getText().toString();
            user_desg = sp_desg.getSelectedItem().toString();
            user_hrms = ed2.getText().toString();
            user_email = ed3.getText().toString();
            user_msisdn = ed4.getText().toString();
            String user_passwd = ed5.getText().toString();
            passwd = MD5.getMd5(user_passwd);
            UUID uid = UUID.randomUUID();
            access_key = MD5.getMd5(user_msisdn+uid);
            String user_re_passwd = ed6.getText().toString();
//               Log.i("Passwd", user_passwd);
//               Log.i("Passwd_1",user_re_passwd);
//               Spinner sp1 = findViewById(R.id.sp_circle);
//               user_circle = sp1.getSelectedItem().toString();
//               Spinner sp2 = findViewById(R.id.)
            user_circle = sp_circles.getSelectedItem().toString();
            user_ssaname = sp_ssas.getSelectedItem().toString();
            user_type = sp_usertype.getSelectedItem().toString();


            if(user_name.trim().equalsIgnoreCase("") || user_name.trim().length()<5){
                ed.setError("Enter the Name");
                return;
            }else if(user_desg.equals("Select")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No Designation is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            } else if (user_hrms.length()<9){
                ed2.setError("HRMS No should be 9 digits");
                return;
            } else if (user_msisdn.length()<10){
                ed4.setError("Msisdn should be 10 Digits");
                return;
            }else if (user_passwd.length()<8){
                ed5.setError("Password should be minimum 8 characters");
                return;
            } else if (user_re_passwd.length()<8){
                ed6.setError("Password should be minimum 8 characters");
                return;
            } else if (! user_passwd.equals(user_re_passwd)) {
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Password and Re-enter Password mismatched");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                ed5.setError("Password/RePassword is mismatched");
                return;
            } else if(user_type.equals("Select User-Type")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No User type is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            } else if(user_circle.equals("Select Circle")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No Circle is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            } else if(user_ssaname.equals("Select SSA")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No ssa is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            }

             MyTask mytask = new MyTask(this);
             mytask.execute(builder.toString());
        });

// Form Properties
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

    public static class MyTask extends AsyncTask<String , String, String>{
        private final WeakReference<NewUserCreation> activityReference;
        ProgressDialog pd;
        public MyTask(NewUserCreation context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            NewUserCreation activity = activityReference.get();
            pd = new ProgressDialog(activity);
            pd.setTitle("New User creation");
            pd.setMessage("User is creating ....");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            NewUserCreation activity = activityReference.get();
            try {
                JSONObject post_obj= new JSONObject();
                post_obj.put("name", activity.user_name);
                post_obj.put("desg",activity.user_desg);
                post_obj.put("hrms",activity.user_hrms);
                post_obj.put("email", activity.user_email);
                post_obj.put("msisdn",activity.user_msisdn);
                post_obj.put("password", activity.passwd);
                post_obj.put("circle", activity.user_circle);
                post_obj.put("ssaname", activity.user_ssaname);
                post_obj.put("user_type",activity.user_type);
                post_obj.put("access_key",activity.access_key);
                post_obj.put("auth", MD5.getMd5(Constants.getAuth()));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0],"UTF-8"), post_obj.toString());
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            NewUserCreation activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
//            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String remarks = obj.getString("remarks");
                if(result.equals("true")){
                    activity.alertDialog.setTitle("CNMC Alert ....");
                    activity.alertDialog.setMessage(remarks);
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                activity.finish();
                                System.exit(0);
                            });

                } else{
                    String alert = remarks;
                    if(remarks.startsWith("Duplicate")){
                        alert = "Already User exists";
                    }
                    activity.alertDialog.setTitle("CNMC Alert....");
                    activity.alertDialog.setMessage(alert);
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                }
                activity.alertDialog.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
