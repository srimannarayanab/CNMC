package com.cmtsbsnl.cnmc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

import androidx.appcompat.widget.Toolbar;

public class ChangeMyBtsMsisdn extends SessionActivity {

  private SharedPreferences sharedPreferences;
  private String curr_msisdn;
  private String change_msisdn;
  private List<String> myBtsModalList;
  private ListView listView;
  private static EditText ed_change_msisdn;
  private static Button btn_change_msisdn;
  private Uri.Builder uri_builder_change_msisdn;
  private EditText ed_msisdn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_change_my_bts_msisdn);
    
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener((View v)->onBackPressed());
    }

    ImageButton homeBtn =  toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener((View v)->startActivity(new Intent(ChangeMyBtsMsisdn.this, Navigational.class)));

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    ed_msisdn = findViewById(R.id.search_msisdn);
    Button btn = findViewById(R.id.btn);

    ed_change_msisdn = findViewById(R.id.change_msisdn);
    btn_change_msisdn = findViewById(R.id.btn_changemsisdn);

    ed_change_msisdn.setVisibility(View.GONE);
    btn_change_msisdn.setVisibility(View.GONE);
    listView = findViewById(R.id.listView);
// Uri for getting the Configured sites list
    Uri.Builder uri_builder = new Uri.Builder()
            .scheme("https")
            .authority(Constants.getSecureBaseUrl())
            .appendPath(getString(R.string.ulr_get_mybts_list));
//  Uri Builder for change of msisdn
    uri_builder_change_msisdn = new Uri.Builder()
            .scheme("https")
            .authority(Constants.getSecureBaseUrl())
            .appendPath(getString(R.string.ulr_change_mybts_msisdn));

    btn.setOnClickListener(view -> {
      curr_msisdn = ed_msisdn.getText().toString();
//        System.out.println(curr_msisdn);
      if(curr_msisdn.length() !=10){
        ed_msisdn.setError("msisdn length should be 10");
        return;
      }
      try {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
      } catch (Exception e) {
        // TODO: handle exception
      }

      MyTask myTask = new MyTask(ChangeMyBtsMsisdn.this);
      myTask.execute(uri_builder.toString() , curr_msisdn);
    });
  }

  public static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<ChangeMyBtsMsisdn> activityReference;
    ProgressDialog pd;
    private MyTask(ChangeMyBtsMsisdn context){
      activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching my Bts configured list");
      pd.setMessage("Processing.... ");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected void onPostExecute(String s) {
//      System.out.println(s);
      ChangeMyBtsMsisdn activity = activityReference.get();
      pd.dismiss();
      activity.listView.setAdapter(null);
      try {
        JSONObject url_obj = new JSONObject(s);
        if (!url_obj.getString("result").equals("true")) {
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray arr = new JSONArray(url_obj.getString("data"));
//        System.out.println(arr);
        if (arr.length() == 0) {
          ed_change_msisdn.setVisibility(View.GONE);
          btn_change_msisdn.setVisibility(View.GONE);
          AlertDialog.Builder builder = new AlertDialog.Builder(activity);
          builder.setMessage("No Bts are configured")
                  .setTitle("MyBTS Configuration")
                  .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
          builder.show();
        } else {
          activity.myBtsModalList = new ArrayList<>();
          for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String bts_name = obj.getString("bts_name");
            String bts_type = obj.getString("bts_type");
            String ssa_id = obj.getString("ssa_id");
            int mybts_id = obj.getInt("mybts_id");

            MyBtsModal myBtsModal = new MyBtsModal(bts_name, bts_type, ssa_id, mybts_id);
            activity.myBtsModalList.add(myBtsModal.toString());
          }
          ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.my_bts_list_view,
              R.id.textview, activity.myBtsModalList);
          activity.listView.setAdapter(adapter);
          activity.listView.setTextFilterEnabled(true);
          activity.listView.setClickable(true);
          ed_change_msisdn.setVisibility(View.VISIBLE);
          btn_change_msisdn.setVisibility(View.VISIBLE);
          btn_change_msisdn.setOnClickListener(view -> {
            activity.change_msisdn = ed_change_msisdn.getText().toString();
            if(activity.change_msisdn.length() !=10){
              ed_change_msisdn.setError("Msisdn length should be 10");
              return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle("Change of Msisdn")
                    .setMessage("Do you want to continue to change from one user to another user?")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
//                          Api call for change of msisdn
                      ChangeMsisdn changeMsisdn = new ChangeMsisdn(activity);
                      changeMsisdn.execute(activity.uri_builder_change_msisdn.toString(), activity.curr_msisdn, activity.change_msisdn);



                    }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
          });

        }
//        Set the List view of all the sites

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected String doInBackground(String... strings) {
      ChangeMyBtsMsisdn activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("mybts_msisdn", strings[1] );
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
                post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  }

//  Api to call the data
  public static class ChangeMsisdn extends AsyncTask<String, String, String>{
    private final WeakReference<ChangeMyBtsMsisdn> activityReference1;
    ProgressDialog pd;
    private ChangeMsisdn(ChangeMyBtsMsisdn context){
      activityReference1 = new WeakReference<>(context);
    }


    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference1.get());
      pd.setTitle("Updation of new msisdn");
      pd.setMessage("Processing.... ");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

  @Override
  protected void onPostExecute(String s) {
    ChangeMyBtsMsisdn activity = activityReference1.get();
    pd.dismiss();
    try {
      JSONObject url_obj1 = new JSONObject(s);
      System.out.println(s);
      String message;
      if(url_obj1.getString("result").equals("true")){
        message="Sucessfully change the msisdn";
      } else {
        message=url_obj1.getString("error");

      }
      AlertDialog.Builder builder = new AlertDialog.Builder(activity)
              .setTitle("Msisdn Change")
              .setMessage(message)
              .setPositiveButton("ok", (dialogInterface, i) -> dialogInterface.dismiss());
      builder.show();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
    protected String doInBackground(String... strings) {
    ChangeMyBtsMsisdn activity = activityReference1.get();
    try {
      JSONObject post_obj = new JSONObject();
      post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
      post_obj.put("old_msisdn", strings[1] );
      post_obj.put("new_msisdn", strings[2]);
      return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
              post_obj.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
      return null;
    }
  }
}