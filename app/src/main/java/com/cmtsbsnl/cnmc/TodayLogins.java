package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TodayLogins extends SessionActivity {

  private SharedPreferences sharedPreferences;
  private List<TodayLoginsModel> todayLoginsModelList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_today_logins);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    TextView homeTextView = findViewById(R.id.textView1);

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    Intent intent = getIntent();
    String circle_id = intent.getStringExtra("circle_id");
    String ssa_id = intent.getStringExtra("ssa_id");

    homeTextView.setText(homeTextView.getText()+"-"+ssa_id);

    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_get_today_logins));

    MyTask myTask = new MyTask(this);
    myTask.execute(uri_builder.toString(), circle_id, ssa_id);
  }

  private static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<TodayLogins> activityReference;
    ProgressDialog pd;

    private MyTask(TodayLogins context) {
      activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching user Login details...");
      pd.setMessage("Processing.... ");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected String doInBackground(String... params) {
      TodayLogins activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle_id", params[1]);
        post_obj.put("ssa_id",params[2]);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      pd.dismiss();
      TodayLogins activity = activityReference.get();
      System.out.println(s);
//            System.out.println("circle "+ circle);
//            super.onPostExecute(s);
      try {
        JSONObject url_obj = new JSONObject(s);
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray arr =new JSONArray(url_obj.getString("data"));
        activity.todayLoginsModelList = new ArrayList<>();
        for(int i=0 ; i<arr.length(); i++){
          JSONObject obj = arr.getJSONObject(i);
          TodayLoginsModel todayLoginsModel = new TodayLoginsModel(obj.getString("name"),
              obj.getString("circle"),
              obj.getString("desg"),
              obj.getString("email"),
              obj.getString("msisdn"),
              obj.getString("last_login"));
          activity.todayLoginsModelList.add(todayLoginsModel);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
//            Creating a recycler adapter
      RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
      TodayLoginsAdapter todayLoginsAdapter = new TodayLoginsAdapter(activity, activity.todayLoginsModelList);
      LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getApplicationContext());
      recyclerView.setLayoutManager(layoutManager);
      recyclerView.setAdapter(todayLoginsAdapter);
    }
  }
}