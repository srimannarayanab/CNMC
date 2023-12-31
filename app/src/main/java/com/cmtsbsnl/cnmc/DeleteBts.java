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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;

public class DeleteBts extends SessionActivity {
    private SharedPreferences sharedPreferences;
    private ArrayList<DataModel> dataModels;
    private CustomAdapter adapter;

    public DeleteBts() {
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_bts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(DeleteBts.this, Navigational.class)));

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(DeleteBts.this, Navigational.class)));

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

//        Uri budilder
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_view_bts));


//        msisdn = sharedPreferences.getString("msisdn","");
        MyTask mytask = new MyTask(this);
        mytask.execute(builder.toString());
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<DeleteBts> activityReference;

        ProgressDialog pd;

        private MyTask(DeleteBts context) {
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
            DeleteBts activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            DeleteBts activity = activityReference.get();
//            System.out.println(s);
            pd.dismiss();
//            https://www.journaldev.com/14171/android-checkbox

            ListView listView = activity.findViewById(R.id.listView);
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
//                JSONArray arr = new JSONArray(s);
                if(arr.length()>0) {
                    for (int idx = 0; idx < arr.length(); idx++) {
                        JSONObject obj2 = new JSONObject(arr.getString(idx));
                        String bts_name = obj2.getString("bts_name");
                        String bts_type = obj2.getString("bts_type");
                        String bts_id = obj2.getString("bts_id");
                        btsList.put(bts_name+'-'+bts_type , bts_id);
//                        bts.add(bts_name);
                        activity.dataModels.add(new DataModel(bts_name,  bts_type,false));
                    }

                    activity.adapter = new CustomAdapter(activity.getApplicationContext(), activity.dataModels );
                    listView.setAdapter(activity.adapter);
                    final List<String> checkedItems = new ArrayList<>();
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        DataModel dataModel = activity.dataModels.get(position);
                        dataModel.checked = !dataModel.checked;
                        activity.adapter.notifyDataSetChanged();
                        String bts_name = dataModel.bts_name;
                        String bts_type = dataModel.bts_type;
                        String bts = bts_name +'-'+ bts_type;

                        boolean checked = dataModel.checked;
                        if (checked) {
                            checkedItems.add(bts);
                        } else {
                            checkedItems.remove(bts);
                        }
//                        System.out.println(checked);
                    });

//                Get all the seleceted items of list view
                    Button btn = activity.findViewById(R.id.btn);
                    btn.setVisibility(View.VISIBLE);
                    btn.setOnClickListener(v -> {
                        ArrayList<String> btsids = new ArrayList<>();
//                        System.out.println(checkedItems.toString());
                        for (String bts1 : checkedItems) {
                            btsids.add(btsList.get(bts1));
                        }
//                        System.out.println(btsids);
                        Intent intent = new Intent(activity, DeleteBtsDatabase.class);
                        intent.putStringArrayListExtra("deletebts", btsids);
                        activity.startActivity(intent);
                        activity.finishActivityFromChild(activity, 0);
//                        finish();
//                      SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                    });
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("MyBts");
                    alertDialog.setMessage("No Sites are configured.\nGo to ADD BTS and add the sites.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", (dialog, which) -> {
                        alertDialog.dismiss();
                        activity.finish();
                    });
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
