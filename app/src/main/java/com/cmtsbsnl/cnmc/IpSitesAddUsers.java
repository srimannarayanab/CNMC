package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IpSitesAddUsers extends AppCompatActivity {

  private SharedPreferences sharedPreferences;
  private Spinner sp_ip_vendor;
  private Spinner sp_ip_ssa;
  private TextInputLayout sp_til_ssaname;
  private AutoCompleteTextView sp_act_ssaname;
  private TextInputLayout sp_til_ipvendor;
  private AutoCompleteTextView sp_act_ipvendor;
  private Button btn;
  private Retrofit retrofit;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ip_sites_add_users);

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

    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    List<IpVendorModel> ipvendors = databaseHelper.getIpvendors();
    List<SSAIdsModel> ssaids = databaseHelper.getSSAIds();

    EditText ed_ip_msisdn = findViewById(R.id.ip_msisdn);
    EditText ed_ip_technician_name = findViewById(R.id.ip_technician_name);


    sp_til_ssaname = (TextInputLayout) findViewById(R.id.sp_til_ssaname);
    sp_act_ssaname = (AutoCompleteTextView) findViewById(R.id.sp_atc_ssaname);

    ArrayAdapter<String> adapter_ip_ssa = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    adapter_ip_ssa.add("Select ssa");
    for(SSAIdsModel ssaid:ssaids){
      adapter_ip_ssa.add(ssaid.getSsa_id());
    }

    sp_act_ssaname.setAdapter(adapter_ip_ssa);
    sp_act_ssaname.setThreshold(1);

    sp_til_ipvendor = (TextInputLayout) findViewById(R.id.sp_til_ipvendor);
    sp_act_ipvendor = (AutoCompleteTextView) findViewById(R.id.sp_atc_ipvendor);

    ArrayAdapter<String> adapter_ip_vendor = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    adapter_ip_vendor.add("Select IP-Vendor");
    for(IpVendorModel ip: ipvendors){
      adapter_ip_vendor.add(ip.getIp_vendor_name());
    }

    sp_act_ipvendor.setAdapter(adapter_ip_vendor);
    sp_act_ipvendor.setThreshold(1);
    
    btn = findViewById(R.id.btn);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String ip_user_name = ed_ip_msisdn.getText().toString();
        String ip_technician_name = ed_ip_technician_name.getText().toString();
        String ip_ssaname = sp_act_ssaname.getText().toString();
        String ip_vendor = sp_act_ipvendor.getText().toString();
        if(ip_user_name.length() !=10){
          ed_ip_msisdn.setError("Mobile number should be 10 digit");
          ed_ip_msisdn.requestFocus();
          return;
        } else if(ip_technician_name.length() <5){
          ed_ip_technician_name.setError("Technician Name should be atleast 5 characters");
          ed_ip_technician_name.requestFocus();
          return;
        }else if(ip_ssaname.equals("Select ssa") || ip_ssaname.equals("")){
          sp_act_ssaname.setError("No SSA is choosen");
          sp_act_ssaname.requestFocus();
          return;
        } else if(ip_vendor.equals("") || ip_vendor.equals("Select IP-Vendor")){
          sp_act_ssaname.setError(null);
          sp_act_ipvendor.setError("No Vendor choosen");
          sp_act_ipvendor.requestFocus();
          return;
        }

        sp_act_ssaname.setError(null);

        JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("ip_msisdn", ip_user_name);
          jsonObject.put("ip_technician_name", ip_technician_name);
          jsonObject.put("ip_ssaname", ip_ssaname);
          jsonObject.put("ip_vendor", ip_vendor);
          jsonObject.put("password",MD5.getMd5("Bsnl@1234"));
          jsonObject.put("msisdn", sharedPreferences.getString("msisdn",""));
          jsonObject.put("circle", sharedPreferences.getString("circle",""));
          jsonObject.put("circle_id", sharedPreferences.getString("circle_id",""));
        } catch (JSONException e) {
          e.printStackTrace();
        }

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<ApiResponse> call = myInterface.addIpUser(sharedPreferences.getString("web_token",""), jsonObject.toString());

        final ProgressDialog pd;
        pd = new ProgressDialog(IpSitesAddUsers.this);
        pd.setTitle("IP-Add Users");
        pd.setMessage("Adding IP Technician User");
        pd.setCancelable(false);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();

        call.enqueue(new Callback<ApiResponse>() {
          @Override
          public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if(pd.isShowing()) {
              pd.dismiss();
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            if(response.isSuccessful()) {
              try {
                JSONObject jsonObject  = new JSONObject(new Gson().toJson(response.body()));
                if(jsonObject.getString("result").equals("success")){
                  AlertDialog.Builder builder = new AlertDialog.Builder(IpSitesAddUsers.this)
                      .setTitle("IP- Add user")
                      .setMessage("Successfull added user.")
                      .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                          ed_ip_msisdn.getText().clear();
                          ed_ip_technician_name.getText().clear();
                          sp_act_ipvendor.getText().clear();
                          sp_act_ssaname.getText().clear();
                        }
                      });
                  AlertDialog alertDialog = builder.create();
                  alertDialog.show();
                } else {
                  AlertDialog.Builder builder = new AlertDialog.Builder(IpSitesAddUsers.this)
                      .setTitle("IP- Add user")
                      .setMessage(jsonObject.getString("errors"))
                      .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                          ed_ip_msisdn.getText().clear();
                          ed_ip_technician_name.getText().clear();
                          sp_act_ipvendor.getText().clear();
                          sp_act_ssaname.getText().clear();
                        }
                      });
                  AlertDialog alertDialog = builder.create();
                  alertDialog.show();
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }

            }
          }

          @Override
          public void onFailure(Call<ApiResponse> call, Throwable t) {
            Toast.makeText(IpSitesAddUsers.this, t.getMessage(), Toast.LENGTH_SHORT).show();

          }
        });

        


      }
    });

  }
}