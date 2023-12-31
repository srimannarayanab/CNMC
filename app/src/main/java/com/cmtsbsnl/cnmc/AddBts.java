package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.widget.Toolbar;

public class AddBts extends SessionActivity {
    private SharedPreferences sharedPreferences;
    private String ssa_id;
    private MyTask mytask;
    private ArrayList<DataModel> dataModels;
    private ArrayList<DataModel> filteredList = new ArrayList<>();
    private CustomAdapter customAdapter;
    private ListView listView;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)->onBackPressed());
        }

        ImageButton homeBtn =  toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener((View v)->startActivity(new Intent(AddBts.this, Navigational.class)));

        try {
             sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException |IOException e) {
            e.printStackTrace();
        }

        EditText inputsearch;

//        Get SSA Ids
        String ssaids = sharedPreferences.getString("ssa_ids", "");
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        HashMap<String, String> ssanames = gson.fromJson(ssaids, type);
        //        Iterator<String> itr =  ssanames.keySet().iterator();
        List<String> ssa_ids = new ArrayList<>(ssanames.keySet());
        Collections.sort(ssa_ids);
//        while(itr.hasNext()){
//            ssa_ids.add(itr.next());
//        }

//        Uri Builder
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_bts_ssa));

        Spinner spinner =  findViewById(R.id.spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(AddBts.this, R.layout.support_simple_spinner_dropdown_item, ssa_ids);
        spinner.setAdapter(adapter);
//        ssa_id = spinner.getSelectedItem().toString();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ssa_id = parent.getItemAtPosition(position).toString();
                mytask = new MyTask(AddBts.this);
                mytask.execute(builder.toString(), ssa_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        inputsearch = findViewById(R.id.search_bts);
        inputsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filteredList = new ArrayList<>();
                for(DataModel item:dataModels){
                    if(item.getBts_name().toLowerCase().contains(s)){
                        filteredList.add(item);
                    }
                }
//                dataModels = filteredList;
                customAdapter = new CustomAdapter(AddBts.this, filteredList);
                listView.setAdapter(customAdapter);
            }
        });
    }

//    @Override
//    public Filter getFilter() {
//        return null;
//    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<AddBts> activityReference;
        ProgressDialog pd;

        private MyTask(AddBts context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching Bts...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            AddBts activity = activityReference.get();
            try {
                String ssaid = params[1];
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("ssa_id", ssaid);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            AddBts activity = activityReference.get();
//            System.out.println(s);
            pd.dismiss();
//            https://www.journaldev.com/14171/android-checkbox
//            https://www.mysamplecode.com/2012/07/android-listview-custom-layout-filter.html

            activity.listView =  activity.findViewById(R.id.listView);
            activity.dataModels = new ArrayList<>();
            final HashMap<String, String> btsList = new HashMap<>();
//            final List<String> bts = new ArrayList<>();
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray arr = new JSONArray(url_obj.getString("data"));
                for(int idx = 0; idx<arr.length(); idx++){
                    JSONObject obj2 = new JSONObject(arr.getString(idx));
                    String bts_name = obj2.getString("bts_name");
                    String bts_type = obj2.getString("bts_type");
                    String bts_id = obj2.getString("bts_id");
                    btsList.put(bts_name+'-'+bts_type, bts_id);
//                    bts.add(bts_name);
                    activity.dataModels.add(new DataModel(bts_name,  bts_type,false));
                }

                activity.customAdapter = new CustomAdapter(activity.getApplicationContext(), activity.dataModels );
                activity.listView.setAdapter(activity.customAdapter);
                activity.listView.setTextFilterEnabled(true);
//                registerForContextMenu(listView);
//                Text View filter

//                activity.filteredList = activity.dataModels;
                final List<String> checkedItems = new ArrayList<>();
                activity.listView.setOnItemClickListener((parent, view, position, id) -> {
                    if(activity.filteredList.isEmpty()){
                        activity.filteredList=activity.dataModels;
                    }
                    DataModel dataModel = activity.filteredList.get(position);
                    dataModel.checked = !dataModel.checked;
                    activity.customAdapter.notifyDataSetChanged();
                    String bts_name = dataModel.bts_name;
                    String bts_type = dataModel.bts_type;
                    String bts = bts_name +'-'+ bts_type;

                    boolean checked = dataModel.checked;
                    if(checked){
                        checkedItems.add(bts);
                    } else{
                        checkedItems.remove(bts);
                    }
//                        System.out.println(checked);
                });

//                Get all the seleceted items of list view
                Button btn =  activity.findViewById(R.id.btn);
                btn.setOnClickListener((View v)-> {
                        ArrayList<String> btsids = new ArrayList<>();
//                        System.out.println(checkedItems.toString());
                        for(String bts_names: checkedItems){
                            btsids.add(btsList.get(bts_names));
                        }
//                        System.out.println(btsids);
                        Intent intent = new Intent(activity, AddBtsDatabase.class);
                        intent.putStringArrayListExtra("addbts", btsids);
                        activity.startActivity(intent);
                        activity.finishActivityFromChild(activity, 0);
//                      SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();

                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
