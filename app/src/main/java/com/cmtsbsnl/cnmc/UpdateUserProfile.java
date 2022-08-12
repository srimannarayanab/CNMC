package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class UpdateUserProfile extends Fragment {
    private AlertDialog alertDialog;
    private String user_name;
    private String user_desg;
    private String user_hrms;
    private String user_email;
    private String user_circle;
    private String user_msisdn;
    private String password;
    private String user_level, user_lvl2, user_lvl3;
    private SharedPreferences sharedPreferences;
    private Spinner sp_desg;
    private EditText name;
    private EditText hrms;
    private EditText email;
    private EditText msisdn;
    private EditText passwd;
    private EditText repasswd;
    private EditText lvl2;
    private EditText lvl3;
    private List<String> desgs;
    private List<String> circles;
    private List<String> ssas;
    private Object user_ssaname;
    private ArrayAdapter<String> ssa_adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.activity_update_user_profile, container, false);

        Spinner spinner = rootview.findViewById(R.id.spinner);
        Spinner ssa_spinner = rootview.findViewById(R.id.spinner_ssa);
        try {
            sharedPreferences = new Preferences(rootview.getContext()).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        TableLayout tl = rootview.findViewById(R.id.userinfo_tbl);

        //        Build URI
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_update_userdetails));

        GetUserDesignations userdesg = new GetUserDesignations(getContext());
        try {
            String designations = userdesg.execute().get();
            JSONObject user_desg_obj = new JSONObject(designations);
            JSONArray arr = new JSONArray(user_desg_obj.getString("data"));
            desgs = new ArrayList<>();
            desgs.add("Select");
            for(int a=0; a<arr.length(); a++){
                desgs.add((String) arr.get(a));
            }
//            System.out.println(desgs);
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        GetCircles getCircles = new GetCircles(getContext());
        try {
            String strCircles = getCircles.execute().get();
            JSONObject circles_obj = new JSONObject(strCircles);
            JSONArray circles_arr = new JSONArray(circles_obj.getString("data"));
            circles = new ArrayList<>();
            circles.add("select");
            for(int c=0; c<circles_arr.length(); c++){
                circles.add((String) circles_arr.get(c));
            }
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        alertDialog = new AlertDialog.Builder(requireActivity()).create();


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
//        Put the existing values

        name = rootview.findViewById(R.id.username);
        name.setText(sharedPreferences.getString("name",""));

        sp_desg = rootview.findViewById(R.id.sp_desg);
        ArrayAdapter<String> desg_adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item,desgs);
        sp_desg.setAdapter(desg_adapter);
        sp_desg.setSelection(desg_adapter.getPosition(sharedPreferences.getString("desg","")));
//        sp_desg.setBackground();


        hrms = rootview.findViewById(R.id.hrms_no);
        hrms.setText(sharedPreferences.getString("hrms_no",""));

        email = rootview.findViewById(R.id.email);
        email.setText(sharedPreferences.getString("email",""));

        lvl2 = rootview.findViewById(R.id.lvl2);
        lvl2.setText(sharedPreferences.getString("lvl2",""));

        lvl3 = rootview.findViewById(R.id.lvl3);
        lvl3.setText(sharedPreferences.getString("lvl3",""));

        msisdn = rootview.findViewById(R.id.msisdn);
        msisdn.setText(sharedPreferences.getString("msisdn",""));
        msisdn.setEnabled(false);

        passwd = rootview.findViewById(R.id.password);
        repasswd = rootview.findViewById(R.id.repassword);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, circles);
        spinner.setAdapter(adapter);
        String circle = sharedPreferences.getString("circle","");
//        System.out.println(circle);
        for (int position = 0; position < adapter.getCount(); position++) {
//            System.out.println(adapter.getItem(position));
            if(adapter.getItem(position).equals(circle)) {
                spinner.setSelection(position);
            }
        }

//        Get the ssaname from the user profile circle
        GetCircleSSAs getCircleSSAs = new GetCircleSSAs(getContext());
        try {
            String getssas = getCircleSSAs.execute(circle).get();
            System.out.println(getssas);
            JSONObject ssa_obj = new JSONObject(getssas);
            JSONArray ssa_arr = new JSONArray(ssa_obj.getString("data"));
            ssas = new ArrayList<>();
            ssas.add("select ssa");
            for(int c=0; c<ssa_arr.length(); c++){
                ssas.add((String) ssa_arr.get(c));
            }
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        ssa_adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, ssas);
        ssa_spinner.setAdapter(ssa_adapter);
        String ssaname = sharedPreferences.getString("ssaname","");
        for(int position=0; position<ssa_adapter.getCount(); position++){
            if(ssa_adapter.getItem(position).equals(ssaname)){
                ssa_spinner.setSelection(position);
            }
        }

//        Change of item in circles
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected_circle = spinner.getSelectedItem().toString();
                GetCircleSSAs getCircleSSAs = new GetCircleSSAs(getContext());
                try {
                    String getssas = getCircleSSAs.execute(selected_circle).get();
                    System.out.println(getssas);
                    JSONObject ssa_obj = new JSONObject(getssas);
                    JSONArray ssa_arr = new JSONArray(ssa_obj.getString("data"));
                    ssas = new ArrayList<>();
                    ssas.add("select ssa");
                    for(int c=0; c<ssa_arr.length(); c++){
                        ssas.add((String) ssa_arr.get(c));
                    }
                    ssa_adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, ssas);
                    ssa_spinner.setAdapter(ssa_adapter);
                    for(int position=0; position<ssa_adapter.getCount(); position++){
                        if(ssa_adapter.getItem(position).equals(ssaname)){
                            ssa_spinner.setSelection(position);
                        }
                    }
                } catch (ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


//        String circle_name = spinner.getSelectedItem().toString();
        List<String> levels = new ArrayList<>(Arrays.asList("Select-Level","Level-0","Level-1","Level-2","Level-3","Co/Circle"));
        user_level = sharedPreferences.getString("lvl","");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item, levels);
        final Spinner spinner1 = rootview.findViewById(R.id.spinner1);
        spinner1.setAdapter(adapter1);
        if(user_level.equals("")){
            spinner1.setSelection(adapter1.getPosition("Select-Level"));
        } else {
            spinner1.setSelection(adapter1.getPosition(user_level));
        }

        final Button updtBtn = rootview.findViewById(R.id.btn);
        updtBtn.setOnClickListener(v -> {
            user_name = name.getText().toString();
            user_desg = sp_desg.getSelectedItem().toString();
            user_hrms = hrms.getText().toString();
            user_email = email.getText().toString();
            user_msisdn = msisdn.getText().toString();
            user_lvl2 = lvl2.getText().toString();
            user_lvl3 = lvl3.getText().toString();
            user_level = spinner1.getSelectedItem().toString();
            String user_passwd = passwd.getText().toString();
            password = MD5.getMd5(user_passwd);
//            UUID uid = UUID.randomUUID();
            String user_re_passwd = repasswd.getText().toString();
//            Log.i("Passwd", user_passwd);
//            Log.i("Passwd_1",user_re_passwd);
//            System.out.println(password);
//            System.out.println(user_hrms);
//            Spinner sp1 = rootview.findViewById(R.id.spinner);
            user_circle = spinner.getSelectedItem().toString();
            user_ssaname = ssa_spinner.getSelectedItem().toString();
            if(user_name.trim().equalsIgnoreCase("") || user_name.trim().length()<5){
                name.setError("Enter the Name");
                return;
            } else if (user_hrms.length()<9){
                hrms.setError("HRMS No should be 9 digits");
                return;
            } else if (user_msisdn.length()<10){
                msisdn.setError("Msisdn should be 10 Digits");
                return;
            } else if (user_passwd.length()<8){
                passwd.setError("Password should be minimum 8 characters");
                return;
            } else if (user_re_passwd.length()<8){
                repasswd.setError("Password should be minimum 8 characters");
                return;
            } else if (!user_passwd.equals(user_re_passwd)) {
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Password and Re-enter Password mismatched");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                passwd.setError("Password/Re Password not matched");
                return;
            } else if(user_circle.equals("Select Circle")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No Circle is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            } else if(user_ssaname.equals("select ssa")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("No ssaname is selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            }
            else if(user_level.equals("Select-Level")){
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("User Level is not selected");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
                return;
            }
            else if(user_level.equals("Level-1") && user_lvl2.isEmpty()){
                lvl2.setError("Level-2 is compulsory");
                return;
            } else if(user_level.equals("Level-1") && user_lvl3.isEmpty()){
                lvl3.setError("Level-3 is compulsory");
                return;
            }
            MyTask myTask = new MyTask(UpdateUserProfile.this);
            myTask.execute(uri_builder.toString());
        });
        return rootview;
    }

    public static class MyTask extends AsyncTask<String , String, String> {

        private final WeakReference<UpdateUserProfile> activityReference;
        private MyTask(UpdateUserProfile context) {
            activityReference = new WeakReference<>(context);
        }
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get().getContext());
            pd.setTitle("User Profile");
            pd.setMessage("Updating the user profile....");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            UpdateUserProfile activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("name", activity.user_name);
                post_obj.put("desg", activity.user_desg);
                post_obj.put("hrms", activity.user_hrms);
                post_obj.put("email", activity.user_email);
                post_obj.put("username", activity.user_msisdn);
                post_obj.put("password", activity.password);
                post_obj.put("circle", activity.user_circle);
                post_obj.put("lvl", activity.user_level);
                post_obj.put("lvl2", activity.user_lvl2);
                post_obj.put("lvl3", activity.user_lvl3);
                post_obj.put("ssaname", activity.user_ssaname);

                return new MyHttpClient(activity.getContext()).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
//            System.out.println(s);
            UpdateUserProfile activity = activityReference.get();
            try {
                JSONObject obj = new JSONObject(s);
                String result = obj.getString("result");
                String remarks = obj.getString("remarks");
                if(result.equals("true")){
                    activity.alertDialog.setTitle("CNMC Alert ....");
                    activity.alertDialog.setMessage(remarks);
                    activity.alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                activity.requireActivity().finish();
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
