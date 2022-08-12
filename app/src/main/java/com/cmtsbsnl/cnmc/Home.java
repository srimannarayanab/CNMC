package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import static android.widget.Toast.LENGTH_SHORT;

public class Home extends Fragment {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Spinner designation;

    private final ConstraintSet constraintSet =new ConstraintSet();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.activity_home, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(getContext()).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        String username = sharedPreferences.getString("name","");
        String last_login = sharedPreferences.getString("last_login","");

        final String user_privs = sharedPreferences.getString("user_privs","");
        final String circle_id = sharedPreferences.getString("circle_id","");
        final String lvl = sharedPreferences.getString("lvl","");
        final String admin = sharedPreferences.getString("admin","");
        final String ssaname = sharedPreferences.getString("ssaname","");
        final String user_desg = sharedPreferences.getString("desg","");

        String message = "Welcome " + username + " \n " + "Last Login: " + last_login +" \n "+"SSA Name:"+ ssaname;
        Toast.makeText(rootview.getContext(), message, LENGTH_SHORT).show();

//        Toast.makeText(rootview.getContext(), user_privs+","+lvl, LENGTH_SHORT).show();

        ConstraintLayout constraintLayout;
        constraintLayout = rootview.findViewById(R.id.layout);

        Button btnh = rootview.findViewById(R.id.buttonh);
        btnh.setOnClickListener(v -> {
            if(user_privs.equals("circle")){
                Intent intent;
                if(circle_id.equals("HR") || circle_id.equals("UW")) {
                    intent = new Intent(getActivity(), CircleWiseAvailability.class);
                } else {
                    intent = new Intent(getActivity(), SsaWiseAvailability.class);
                    intent.putExtra("circle_id", circle_id);
                }
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), CircleWiseAvailability.class);
                startActivity(intent);
            }
        });

        Button btn1 = rootview.findViewById(R.id.button1);
        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BtsDown.class);
            startActivity(intent);
        });

        Button btn2 = rootview.findViewById(R.id.button2);
        btn2.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Availability.class);
            startActivity(intent);
        });

        Button btn3 = rootview.findViewById(R.id.button3);
        btn3.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Traffic.class);
            startActivity(intent);
        });

        Button btn4 = rootview.findViewById(R.id.button4);
        btn4.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyBts.class);
            startActivity(intent);
        });

        Button btn_search_bts = rootview.findViewById(R.id.btn_bts_search);
        btn_search_bts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchByBts.class);
                startActivity(intent);
            }
        });

        if(user_privs.equals("circle") && !admin.equals("Y")){
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.button4,ConstraintSet.TOP, R.id.button3, ConstraintSet.BOTTOM,20);
            constraintSet.applyTo(constraintLayout);

            View vadmin = rootview.findViewById(R.id.button5);
            ViewGroup viewGroup = (ViewGroup) vadmin.getParent();
            if(viewGroup != null){
                viewGroup.removeView(vadmin);
            }


        } else {
            Button btn5 = rootview.findViewById(R.id.button5);
            btn5.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Admin.class);
                startActivity(intent);
            });
        }

//        Removing MyBts from the co user_privs
        if(user_privs.equals("co")){
            View tv1 = rootview.findViewById(R.id.button4);
            ViewGroup cl = (ViewGroup) tv1.getParent();
            if(cl !=null){
                cl.removeView(tv1);
            }

          View tv2 = rootview.findViewById(R.id.btn_bts_search);
          ViewGroup c2 = (ViewGroup) tv2.getParent();
          if(c2 !=null){
            c2.removeView(tv2);
          }
            /*for(int v =0; v<cl.getChildCount(); v++){
                if(cl.getChildAt(v) instanceof TextView ){
                    if(((TextView) cl.getChildAt(v)).equals(tv1)) {
                        cl.removeView(cl.getChildAt(v));
                    }
                }
            }*/
        }

        if(user_privs.equals("circle") && lvl.equals("N")){
            try {
                // Designation
                designation = new Spinner(getContext());
                GetUserDesignations userdesg = new GetUserDesignations(getContext());
                String desg = userdesg.execute().get();
                System.out.println(desg);
                JSONObject desg_obj = new JSONObject(desg);
                final List<String> desgs = new ArrayList<>();
                desgs.add("Select Designation");
                JSONArray arr = new JSONArray(desg_obj.getString("data"));
                for(int a=0; a<arr.length(); a++){
                    desgs.add((String) arr.get(a));
                }
                ArrayAdapter<String> desg_adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, desgs);
                designation.setAdapter(desg_adapter);
                if(user_desg !=null){
                    int user_position = desg_adapter.getPosition(user_desg);
                    designation.setSelection(user_position);
                }

                final Spinner level = new Spinner(getContext());
                List<String> levels = new ArrayList<>(Arrays.asList("Select Level", "Level-0", "Level-1", "Level-2", "Level-3", "Co/Circle"));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, levels);
                level.setAdapter(adapter);
                final EditText msisdn_l2 = new EditText(getContext());
                msisdn_l2.setHint("L2 MSISDN");
                msisdn_l2.setInputType(InputType.TYPE_CLASS_NUMBER);
                final EditText msisdn_l3 = new EditText(getContext());
                msisdn_l3.setHint("L3 MSISDN");
                msisdn_l2.setInputType(InputType.TYPE_CLASS_NUMBER);
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(designation);
                layout.addView(level);
                layout.addView(msisdn_l2);
                layout.addView(msisdn_l3);
                final View titleView = getLayoutInflater().inflate(R.layout.dialog_title, null);
                final AlertDialog d = new AlertDialog.Builder(requireContext())
                        .setView(layout)
                        .setCustomTitle(titleView)
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
//                        .setNegativeButton(R.string.skip, null)
                        .setCancelable(false)
                        .create();
                d.setOnShowListener(dialog -> {
                    Button btn  = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    btn.setOnClickListener(v -> {
                        boolean isError = false;
                        String str_level = level.getSelectedItem().toString();
                        String usr_desg =  designation.getSelectedItem().toString();
                        if(usr_desg.equals("Select Designation")){
                            isError=true;
                            ((TextView) designation.getSelectedView()).setError("Designation is must");
                        } else if(str_level.equals("Select Level")){
                            isError =true;
                            ((TextView) level.getSelectedView()).setError("level is must");
                        } else if(msisdn_l2.getText().toString().length()<10 &&
                                str_level.equals("Level-1")){
                            isError=true;
                            msisdn_l2.setError("Mobile Number should be min 10 digits");
                            msisdn_l2.setFocusable(true);
                        } else if(msisdn_l3.getText().toString().length()<10 &&
                                str_level.equals("Level-1")){
                            isError=true;
                            msisdn_l3.setError("Mobile Number should be min 10 digits");
                            msisdn_l3.setFocusable(true);
                        }
                    if(!isError){
                        Intent intent = new Intent(getContext(), update_user_level.class);
                        intent.putExtra("desg", usr_desg.trim());
                        intent.putExtra("level", str_level.trim());
                        intent.putExtra("level2", msisdn_l2.getText().toString().trim());
                        intent.putExtra("level3", msisdn_l3.getText().toString().trim());
                        startActivity(intent);
//                                getActivity().finish();
                        }
                    });

                    Button btn_skip = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                    btn_skip.setOnClickListener(v -> d.dismiss());
                });
                d.show();


            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }

        } // for Level complete

        if(user_privs.equals("circle") && (ssaname.equals("") || ssaname.length()==0)){
            Map<String, String> ssaid = new Gson().fromJson(sharedPreferences.getString("ssa_ids",""),
                    new TypeToken<HashMap<String,String>>(){}.getType());
            Iterator<String> itr = ssaid.keySet().iterator();
            final List<String> ssanames = new ArrayList<>();
            while(itr.hasNext()){
                ssanames.add(ssaid.get(itr.next()));
            }

            Collections.sort(ssanames);
            ssanames.add("ALLSSAS");
//            System.out.println(ssanames);
            final Spinner sp_ssanames = new Spinner(getContext());
            final ArrayAdapter<String> ssa_adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item,ssanames);
            ssa_adapter.insert("Select SSA",0);
            sp_ssanames.setAdapter(ssa_adapter);

            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(sp_ssanames);
            final View titleView = getLayoutInflater().inflate(R.layout.dialog_title_ssaname_updt, null);
            final AlertDialog d_1 = new AlertDialog.Builder(requireContext())
                    .setView(layout)
                    .setCustomTitle(titleView)
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
//                        .setNegativeButton(R.string.skip, null)
                    .setCancelable(false)
                    .create();

            d_1.setCanceledOnTouchOutside(false);
            d_1.setOnShowListener(dialog -> {
                Button btn_ok = d_1.getButton(AlertDialog.BUTTON_POSITIVE);
                btn_ok.setOnClickListener(v -> {
//                    boolean error = true;
                    String user_ssaname= sp_ssanames.getSelectedItem().toString();
                    if(user_ssaname.equals("Select SSA")){
                        ((TextView)sp_ssanames.getSelectedView()).setError("Invalid SSA Name Choosed");
//                                error=false;
                        return;
                    }
//                    Toast.makeText(getContext(), "SSA Name choosen "+user_ssaname, LENGTH_SHORT).show();
                    try {
                        String ssa_updt = new UpdateSSAName(getContext()).execute(user_ssaname, sharedPreferences.getString("msisdn","")).get();
                        JSONObject res_obj = new JSONObject(ssa_updt);
                        String ssaupdt_result = res_obj.getString("result");
                        String ssaupdt_error = res_obj.getString("error");
                        if(ssaupdt_result.equals("ok")){
                            editor = sharedPreferences.edit();
                            editor.putString("ssaname",user_ssaname);
                            editor.apply();
                        } else {
                            Toast.makeText(getContext(),"Error: "+ssaupdt_error, LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                    d_1.dismiss();

                });
            });
            d_1.show();
        }

        /*if(user_privs.equals("circle")) {
//            Remove the Co users text views
            View tv1 = rootview.findViewById(R.id.button5);
            ViewGroup cl = (ViewGroup) tv1.getParent();
            for(int v =0; v<cl.getChildCount(); v++){
                if(cl.getChildAt(v) instanceof TextView ){
                    if(((TextView) cl.getChildAt(v)).equals(tv1)) {
                        cl.removeView(cl.getChildAt(v));
                    }
                }
            }

            Button btn4 = (Button) rootview.findViewById(R.id.button4);
            btn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MyBts.class);
                    startActivity(intent);
                }
            });
        } else {
            View tv1 = rootview.findViewById(R.id.button4);
            ViewGroup cl = (ViewGroup) tv1.getParent();
            for(int v =0; v<cl.getChildCount(); v++){
                if(cl.getChildAt(v) instanceof TextView ){
                    if(((TextView) cl.getChildAt(v)).equals(tv1)) {
                        cl.removeView(cl.getChildAt(v));
                    }
                }
            }

            Button btn5 = (Button) rootview.findViewById(R.id.button5);
            btn5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), Admin.class);
                    startActivity(intent);
                }
            });
        }*/
        return rootview;
    }
}
