package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class IpSitesAddSites extends SessionActivity {

  private SharedPreferences sharedPreferences;
  private SSLSocketFactory sslSocketFactory;
  private static Retrofit retrofit;
  private ArrayList<DataModel> dataModelArrayList = new ArrayList<>();
  private ArrayList<DataModel> filteredList = new ArrayList<>();
  private ListView listView;
  private List<String[]> checkedItems;
  private CustomAdapter customAdapter;
  private JSONObject obj;
  private Spinner sp_ip_vendor;
  private Spinner sp_ip_ssa;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ip_sites_add_sites);

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

    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    List<IpVendorModel> ipvendors = databaseHelper.getIpvendors();
    List<SSAIdsModel> ssaids = databaseHelper.getSSAIds();
//    Toast.makeText(this, ipvendors.get(0).getIp_vendor_name(), Toast.LENGTH_SHORT).show();

    sp_ip_vendor = findViewById(R.id.sp_ip_vendor);
    ArrayAdapter<String> adapter_ip_vendor = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    adapter_ip_vendor.add("Select IP-Vendor");
    for(IpVendorModel ip: ipvendors){
      adapter_ip_vendor.add(ip.getIp_vendor_name());
    }
    sp_ip_vendor.setAdapter(adapter_ip_vendor);

    sp_ip_ssa = findViewById(R.id.sp_ip_ssa);
    ArrayAdapter<String> adapter_ip_ssa = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    adapter_ip_ssa.add("Select ssa");
    for(SSAIdsModel ssaid:ssaids){
      adapter_ip_ssa.add(ssaid.getSsa_id());
    }
    sp_ip_ssa.setAdapter(adapter_ip_ssa);
    listView = findViewById(R.id.ipsites);

//    Get the list of bts from the combination  of ip vendor and ssa id
    Button btn_get_btslist = findViewById(R.id.btn_get_btslist);
    btn_get_btslist.setOnClickListener(view -> {
      dataModelArrayList.clear();
      String operator_name = sp_ip_vendor.getSelectedItem().toString();
      String ssa_id = sp_ip_ssa.getSelectedItem().toString();

      List<BtsMasterModal> btsMasterModalList = databaseHelper.getBtsList(operator_name, ssa_id);
      for(BtsMasterModal bm : btsMasterModalList){
        String btsname = bm.getBts_name();
        String btstype = bm.getBts_type();
        DataModel dataModel = new DataModel(btsname, btstype, false);
        dataModelArrayList.add(dataModel);
      }
      customAdapter = new CustomAdapter(IpSitesAddSites.this, dataModelArrayList);
      listView.setAdapter(customAdapter);
      listView.setTextFilterEnabled(true);

      EditText filter_bts = findViewById(R.id.ed_search_bts);
      filter_bts.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
          filteredList = new ArrayList<>();
//          customAdapter.clear();
          for(DataModel dm:dataModelArrayList){
            if(dm.getBts_name().toLowerCase().contains(s)){
              filteredList.add(dm);
//              customAdapter.add(dm);
            }
          }
          customAdapter = new CustomAdapter(IpSitesAddSites.this, filteredList);
          listView.setAdapter(customAdapter);


        }
      });

//        Get the checked items
      checkedItems = new ArrayList<>();
      listView.setOnItemClickListener((adapterView, view1, i, l) -> {
        if(filteredList.isEmpty()){
          filteredList=dataModelArrayList;
        }
        DataModel dataModel = filteredList.get(i);
        dataModel.checked =!dataModel.checked;
        customAdapter.notifyDataSetChanged();
        String bts_name = dataModel.bts_name;
        String bts_type = dataModel.bts_type;
        boolean checked = dataModel.checked;
        if(checked){
          checkedItems.add(new String[]{bts_name, bts_type});
        } else{
          checkedItems.remove(new String[]{bts_name, bts_type});
        }
      });
    });

//    Add sites
    Button btn_add_sites = findViewById(R.id.btn_add_bts);
    btn_add_sites.setOnClickListener(view -> {
      final EditText ip_vendor_msisdn = new EditText(IpSitesAddSites.this);
      ip_vendor_msisdn.setHint("IP-Vendor Mobile No");
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.WRAP_CONTENT);
      lp.setMargins(30,0,30,0);
      ip_vendor_msisdn.setLayoutParams(lp);
      ip_vendor_msisdn.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
      ip_vendor_msisdn.setInputType(InputType.TYPE_CLASS_NUMBER);
      ip_vendor_msisdn.setBackground(getResources().getDrawable(R.drawable.edittext_style)); //call reequires api 16 and above
      final View titleView = getLayoutInflater().inflate(R.layout.dialog_ip_vendor, null);
      AlertDialog alertDialog= new AlertDialog.Builder(IpSitesAddSites.this)
          .setCustomTitle(titleView)
          .setView(ip_vendor_msisdn)
          .setPositiveButton(android.R.string.ok, null)
          .setNegativeButton("Cancel",null)
          .setCancelable(false)
          .create();

      alertDialog.setOnShowListener(dialog -> {
        Button btn  = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btn_cancel = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        btn.setOnClickListener(v -> {
          String ip_msisdn = ip_vendor_msisdn.getText().toString();
          if(ip_msisdn.length() !=10){
            ip_vendor_msisdn.setError("Msisdn should be 10 digits");
          } else{
            JSONArray btss = new JSONArray();
            for(String[] x: checkedItems){
              JSONObject bts_obj = new JSONObject();
              try {
                bts_obj.put("bts_name", x[0]);
                bts_obj.put("bts_type", x[1]);
                btss.put(bts_obj);
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
            try {
              String vendor_name = sp_ip_vendor.getSelectedItem().toString();
              String ssaname = sp_ip_ssa.getSelectedItem().toString();

              obj = new JSONObject();
              obj.put("msisdn", sharedPreferences.getString("msisdn", null));
              obj.put("add_by", sharedPreferences.getString("msisdn", null));
              obj.put("vendor_name", vendor_name);
              obj.put("ssaname", ssaname);
              obj.put("ip_vendor_msisdn", ip_vendor_msisdn.getText().toString());
              obj.put("btss", btss);
              System.out.println(obj.toString());
            } catch (JSONException e) {
              e.printStackTrace();
            }
            addSites(obj);

            dialog.dismiss();
          }
        });

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
      });
      alertDialog.show();
    });



  }

  public void addSites(JSONObject obj) {
    final OkHttpClient client = ApiAdapter.getUnsafeOkHttpClient();
    //    Using retrofit
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://61.0.234.2/cnmc/v1/")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    MyInterface myInterface = retrofit.create(MyInterface.class);
    Call<ApiResponse> call = myInterface.IpAddSites(obj.toString(), sharedPreferences.getString("web_token",""));

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
        if(pd.isShowing()){
          pd.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(IpSitesAddSites.this)
            .setTitle("IP-Site Add")
            .setMessage("Sucessfully added the sites")
            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                listView.setAdapter(null);
                sp_ip_vendor.setSelection(0);
                sp_ip_ssa.setSelection(0);
                dialog.dismiss();
              }
            });
        AlertDialog alertDialog =builder.create();
        alertDialog.show();
      }

      @Override
      public void onFailure(Call<ApiResponse> call, Throwable t) {
        System.out.println(t.getMessage());
        Toast.makeText(IpSitesAddSites.this, t.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }
}