package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class IpSitesChangeMsisdn extends SessionActivity {

  private SharedPreferences sharedPreferences;
  private String ip_msisdn;
  private Retrofit retrofit;
  private List<IpBtsSmsModal> ipBtsSmsModalList;
  private Button btn_changemsisdn;
  private EditText ed_ipmsisdn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ip_sites_change_msisdn);

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

    ed_ipmsisdn = findViewById(R.id.search_msisdn);
    Button btn= findViewById(R.id.btn);
    btn_changemsisdn = findViewById(R.id.btn_changemsisdn);

    btn.setOnClickListener(v -> {
      ip_msisdn = ed_ipmsisdn.getText().toString();
      if(ip_msisdn.length() !=10){
        ed_ipmsisdn.setError("Mobile number should be 10 digits only");
        return;
      }

      JSONObject obj = new JSONObject();
      try {
        obj.put("msisdn", sharedPreferences.getString("msisdn",""));
        obj.put("ip_msisdn", ip_msisdn);
        getIpAssignedSites(obj);
      } catch (JSONException e) {
        e.printStackTrace();
      }


    });
  }

  public void getIpAssignedSites(JSONObject jsonObject){
    MyInterface myInterface = retrofit.create(MyInterface.class);
    Call<ApiResponse> call = myInterface.GetIpAssignedSites(jsonObject.toString(),sharedPreferences.getString("web_token",""));
    final ProgressDialog pd;
    pd = new ProgressDialog(this);
    pd.setTitle("IP-Add Sites");
    pd.setMessage("Fetching the sites assigned to technicians");
    pd.setCancelable(false);
    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pd.show();

    call.enqueue(new Callback<ApiResponse>() {
      @Override
      public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//        System.out.println(response.body());
        if (pd.isShowing()) {
          pd.dismiss();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        if (response.isSuccessful()) {
          try {
            JSONObject object = new JSONObject(new Gson().toJson(response.body()));
            JSONArray arr = new JSONArray(object.getString("rows"));
            if(arr.length()==0){
              AlertDialog.Builder nodata_builder = new AlertDialog.Builder(IpSitesChangeMsisdn.this)
                  .setTitle("IP-Sites View BTS")
                  .setMessage("No BTSs are configured against this numbers" )
                  .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
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
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(IpSitesChangeMsisdn.this,
                R.layout.my_bts_list_view, R.id.textview, ip_assigned);
            listView.setAdapter(arrayAdapter);

//            Click on button of change msisdn
            btn_changemsisdn.setOnClickListener(v -> {
              EditText ed_change_msisdn =findViewById(R.id.change_msisdn);
              String change_msisdn = ed_change_msisdn.getText().toString();
              if(change_msisdn.length() !=10){
                ed_change_msisdn.setError("Mobile number should be 10 digits");
                return;
              }
//                Toast.makeText(IpSitesChangeMsisdn.this, change_msisdn , Toast.LENGTH_SHORT).show();
              JSONObject chg_obj = new JSONObject();
              try {
                chg_obj.put("msisdn", sharedPreferences.getString("msisdn", ""));
                chg_obj.put("old_msisdn", ip_msisdn);
                chg_obj.put("new_msisdn", change_msisdn);
              } catch (JSONException e) {
                e.printStackTrace();
              }

              MyInterface myInterface1 = retrofit.create(MyInterface.class);
              Call<ApiResponse> call1 = myInterface1.changeIpTechnician(sharedPreferences.getString("web_token", ""), chg_obj.toString() );

              call1.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call1, Response<ApiResponse> response1) {
                  System.out.println(response1.body());
                  try {
                    JSONObject resp_obj= new JSONObject(new Gson().toJson(response1.body()));
//                    JSONArray arr1 = new JSONArray(resp_obj.getString("rows"));
                    String result = resp_obj.getString("result");
                    if(result.equals("success")){
                      AlertDialog.Builder builder = new AlertDialog.Builder(IpSitesChangeMsisdn.this)
                          .setTitle("Change IP Technician Mobile no")
                          .setMessage("Sucessfully changed Mobile no")
                          .setPositiveButton("ok", (dialog, which) -> {
                            dialog.dismiss();
                            listView.setAdapter(null);
                            ed_change_msisdn.getText().clear();
                            ed_ipmsisdn.getText().clear();
                          });
                      AlertDialog alertDialog = builder.create();
                      alertDialog.show();
                    } else{
                      Toast.makeText(IpSitesChangeMsisdn.this, resp_obj.getString("errors"), Toast.LENGTH_SHORT).show();
                    }
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }

                }

                @Override
                public void onFailure(Call<ApiResponse> call1, Throwable t) {

                }
              });



            });

            

          } catch (JSONException e) {
            e.printStackTrace();
          }

        }
      }

      @Override
      public void onFailure(Call<ApiResponse> call, Throwable t) {
        Toast.makeText(IpSitesChangeMsisdn.this, t.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }
}