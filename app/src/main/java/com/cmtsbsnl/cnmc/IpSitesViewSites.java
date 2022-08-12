package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IpSitesViewSites extends SessionActivity {

  private SharedPreferences sharedPreferences;
  private EditText ed_search_msisdn;
  private List<IpBtsSmsModal> ipBtsSmsModalList;
  private Retrofit retrofit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ip_sites_view_sites);

    int SDK_INT = android.os.Build.VERSION.SDK_INT;
    if (SDK_INT > 8)
    {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
          .permitAll().build();
      StrictMode.setThreadPolicy(policy);

    }

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener((View v)->onBackPressed());
    }

    ImageButton homeBtn =  toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener((View v)->startActivity(new Intent(this, Navigational.class)));

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    OkHttpClient client = ApiAdapter.getUnsafeOkHttpClient();
    //    Using retrofit
    retrofit = new Retrofit.Builder()
        .baseUrl("https://61.0.234.2/cnmc/v1/")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    Button btn_get_ip_assigned_sites = findViewById(R.id.btn_get_ip_assigned_sites);
    ed_search_msisdn = findViewById(R.id.search_msisdn);
    btn_get_ip_assigned_sites.setOnClickListener(v -> {
      String ip_msisdn = ed_search_msisdn.getText().toString();
      if(ip_msisdn.length() !=10){
        ed_search_msisdn.setError("Msisdn should be 10 digits");
        return;
      }
      JSONObject obj = new JSONObject();
      try {
        obj.put("msisdn", sharedPreferences.getString("msisdn",""));
        obj.put("ip_msisdn", ip_msisdn);
        getSites(obj);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    });
  }

  public void getSites(JSONObject obj){
    MyInterface myInterface = retrofit.create(MyInterface.class);
    Call<ApiResponse> call = myInterface.GetIpAssignedSites(obj.toString(), sharedPreferences.getString("web_token",""));

    final ProgressDialog pd;
    pd = new ProgressDialog(this);
    pd.setTitle("IP-Add Sites");
    pd.setMessage("Adding sites to the IP Technician");
    pd.setCancelable(false);
    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pd.show();

    call.enqueue(new Callback<ApiResponse>() {
      @Override
      public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//        System.out.println(response.body());
          if(pd.isShowing()) {
            pd.dismiss();
          }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          if(response.isSuccessful()){
            try {
              JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
              JSONArray arr = new JSONArray(jsonObject.getString("rows"));
              if(arr.length()==0){
                AlertDialog.Builder nodata_builder = new AlertDialog.Builder(IpSitesViewSites.this)
                    .setTitle("IP-Sites View BTS")
                    .setMessage("No BTSs are configured against this numbers" )
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                      }
                    });
                AlertDialog nodata_dialog = nodata_builder.create();
                nodata_dialog.show();
                return;
              }
              List<String> ip_assigned = new ArrayList<>();
              ipBtsSmsModalList = new ArrayList<>();
              for(int i=0; i<arr.length(); i++){
                JSONObject inr_obj = arr.getJSONObject(i);
                IpBtsSmsModal ipBtsSmsModal = new IpBtsSmsModal(inr_obj.getString("bts_id"),
                    inr_obj.getString("bts_name"),
                    inr_obj.getString("bts_type"),
                    inr_obj.getString("ssa_id"),
                    inr_obj.getString("operator_name"),
                    inr_obj.getString("add_by"),
                    inr_obj.getString("add_date"));
                ipBtsSmsModalList.add(ipBtsSmsModal);
                ip_assigned.add(ipBtsSmsModal.toString());
              }
            ListView listView = findViewById(R.id.listView);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(IpSitesViewSites.this,
                R.layout.my_bts_list_view, R.id.textview, ip_assigned);
            listView.setAdapter(arrayAdapter);
            listView.setClickable(true);

            listView.setOnItemClickListener((parent, view, position, id) -> {
              IpBtsSmsModal ipBtsSmsModal =  ipBtsSmsModalList.get(position);

              AlertDialog.Builder builder = new AlertDialog.Builder(IpSitesViewSites.this)
                  .setTitle("Unlink-IP Assigned BTS")
                  .setMessage("Your are about to unlink the Bts from the IP-Technician")
                  .setPositiveButton("ok", (dialog, which) -> {
//                        unlink_ip_assigned_bts(ipBtsSmsModal.getBts_id());
                    String username = sharedPreferences.getString("msisdn","");
                    String msisdn = ed_search_msisdn.getText().toString();
                    Call<ApiResponse> call_btsid = myInterface.UnlinkIPAssignedBts( sharedPreferences.getString("web_token",""), username, msisdn, ipBtsSmsModal.getBts_id());
                    call_btsid.enqueue(new Callback<ApiResponse>() {
                      @Override
                      public void onResponse(Call<ApiResponse> call1, Response<ApiResponse> response1) {
                        Toast.makeText(IpSitesViewSites.this, "Successfully unliked "+ipBtsSmsModal.getBts_name(), Toast.LENGTH_SHORT).show();
                        ipBtsSmsModalList.remove(position);
                        ip_assigned.clear();
                        for(IpBtsSmsModal ip: ipBtsSmsModalList){
                          ip_assigned.add(ip.toString());
                        }
                        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(IpSitesViewSites.this,
                            R.layout.my_bts_list_view, R.id.textview, ip_assigned);
                        listView.setAdapter(arrayAdapter1);
                        listView.setClickable(true);

                      }

                      @Override
                      public void onFailure(Call<ApiResponse> call1, Throwable t) {
                        Toast.makeText(IpSitesViewSites.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                      }
                    });
                  })
                  .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
              AlertDialog alertDialog = builder.create();
              alertDialog.show();
            });
          } catch (JSONException e) {
              e.printStackTrace();
            }

          }
//        }
      }

      @Override
      public void onFailure(Call<ApiResponse> call, Throwable t) {

      }
    });
  }


}