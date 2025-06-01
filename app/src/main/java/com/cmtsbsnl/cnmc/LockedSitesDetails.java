package com.cmtsbsnl.cnmc;

import static com.cmtsbsnl.cnmc.Constants.addZeroWidthSpaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

public class LockedSitesDetails extends AppCompatActivity {
  String circle_id, ssa_id, vendor_id;
  private String bts_id ,faults;
  TableLayout tl;
  private SharedPreferences sharedPreferences;
  private String faultReason;
  private Spinner spinner;
  private List<String> fault_types;
  private HashMap<String, String> faultMap;

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_locked_sites_details);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
      faults  = sharedPreferences.getString("faults","");
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    ImageButton toXlsx;

    Intent intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");
    ssa_id = intent.getStringExtra("ssa_id");
    vendor_id = intent.getStringExtra("vendor_id");
    Gson gson = new Gson();
    Map map = gson.fromJson(faults, Map.class);
    fault_types = new ArrayList<>();
    Iterator itr = map.keySet().iterator();
    faultMap = new HashMap<>();
    while(itr.hasNext()){
      String flt = (String) itr.next();
      fault_types.add(flt);
      faultMap.put(flt, map.get(flt).toString());
//            System.out.println(map.get(itr.next()));
    }
//    System.out.println(faultMap.toString());
//    Log.d("Faults Map", faultMap.toString());

    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_locked_sites_details));

    TextView header = findViewById(R.id.textView1);
    String htxt = vendor_id;
    if(vendor_id.equals("%")){
      htxt = "All Vendors";
    }
    header.setText(getString(R.string.header_locked_details,circle_id,htxt));


    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener(v -> {
        onBackPressed();
//                finish();
      });
    }

    ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(LockedSitesDetails.this, Navigational.class)));

//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    MyTask mytask = new MyTask(this);
    mytask.execute(uri_builder.toString());

//    Excel Generation
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));
  }

//  Excel data
private void buttonCreateExcel(String url) {
  try {
    String details = new getLockedSitesDetails(this).execute(url).get();
    System.out.println(details.toString());
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet sheet = workbook.createSheet("Locked");

    HSSFRow row = sheet.createRow(0);
    row.createCell(0).setCellValue("BTSName");
    row.createCell(1).setCellValue("SSAName");
    row.createCell(2).setCellValue("Make");
    row.createCell(3).setCellValue("LockedDate");
    row.createCell(4).setCellValue("Reason");
//    row.createCell(5).setCellValue("Nor");
//    row.createCell(6).setCellValue("Nok");
//    row.createCell(7).setCellValue("Zte");
//    row.createCell(8).setCellValue("Total");

    JSONObject url_obj = new JSONObject(details);
    if(!url_obj.getString("result").equals("true")){
      Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
      startActivity(new Intent(this, SesssionLogout.class));
    }
    JSONArray arr =new JSONArray(url_obj.getString("data"));
    int eric=0, alc=0, moto=0, hua =0, nor=0, nok=0, zte=0, cnt=0;
    for(int i=0; i<arr.length(); i++){
      HSSFRow drow = sheet.createRow(i+1);
      JSONObject obj = arr.getJSONObject(i);
      String reason = !obj.getString("fault_type").equals("null") ? obj.getString("fault_type") : "";
      drow.createCell(0).setCellValue(obj.getString("bts_name"));
      drow.createCell(1).setCellValue(obj.getString("ssa_name"));
      drow.createCell(2).setCellValue(obj.getString("make"));
      drow.createCell(3).setCellValue(obj.getString("bts_location"));
      drow.createCell(4).setCellValue(obj.getString("bts_site_id"));
      drow.createCell(5).setCellValue(obj.getString("bts_type"));
      drow.createCell(6).setCellValue(obj.getString("site_type"));
      drow.createCell(7).setCellValue(obj.getString("lock_date"));
      drow.createCell(8).setCellValue(reason);
//      drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("nor_cnt")));
//      drow.createCell(6).setCellValue(Integer.parseInt(obj.getString("nok_cnt")));
//      drow.createCell(7).setCellValue(Integer.parseInt(obj.getString("zte_cnt")));
//      drow.createCell(8).setCellValue(Integer.parseInt(obj.getString("cnt")));
//      eric += Integer.parseInt(obj.getString("eric_cnt"));
//      alc += Integer.parseInt(obj.getString("alc_cnt"));
//      moto += Integer.parseInt(obj.getString("moto_cnt"));
//      hua += Integer.parseInt(obj.getString("hua_cnt"));
//      nor += Integer.parseInt(obj.getString("nor_cnt"));
//      nok += Integer.parseInt(obj.getString("nok_cnt"));
//      zte += Integer.parseInt(obj.getString("zte_cnt"));
//      cnt += Integer.parseInt(obj.getString("cnt"));
    }
//            Writing the Total data
//    HSSFRow trow = sheet.createRow(arr.length()+1);
//    trow.createCell(0).setCellValue("Total");
//    trow.createCell(1).setCellValue(eric);
//    trow.createCell(2).setCellValue(alc);
//    trow.createCell(3).setCellValue(moto);
//    trow.createCell(4).setCellValue(hua);
//    trow.createCell(5).setCellValue(nor);
//    trow.createCell(6).setCellValue(nok);
//    trow.createCell(7).setCellValue(zte);
//    trow.createCell(8).setCellValue(cnt);


    if(isExternalStorageWritable()) {
      File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/CommLocked.xls");
      try {
        FileOutputStream fileOutputStream = new FileOutputStream(filepath);
        workbook.write(fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(filepath.getName());
        String type = map.getMimeTypeFromExtension(ext);
//                    System.out.println(type);
        if (type == null)
          type = "*/*";
        Intent excel_open_intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(filepath);
        excel_open_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        excel_open_intent.setDataAndType(data, type);
        startActivity(excel_open_intent);
        Toast toast = Toast.makeText(getApplicationContext(), "Excel file generated sucessfully in downloads folder", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else{
      Toast.makeText(getApplicationContext(), "Sorry not permitted",Toast.LENGTH_SHORT).show();
    }
  } catch (ExecutionException | InterruptedException | JSONException e) {
    e.printStackTrace();
  }
}

  private Boolean isExternalStorageWritable(){
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }


  private class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<LockedSitesDetails> activityReference;
    ProgressDialog pd;

    private MyTask(LockedSitesDetails context) {
      activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching Alarms");
      pd.setMessage("Processing...");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected String doInBackground(String... strings) {
      LockedSitesDetails activity = activityReference.get();
      try {
        HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
        conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
        conn.setRequestProperty("Context-Type","application/json; utf-8");
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        JSONObject post_obj = new JSONObject();
        post_obj.put("circle_id", activity.circle_id);
        post_obj.put("ssa_id",activity.ssa_id);
        post_obj.put("vendor_id",activity.vendor_id);
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));

        OutputStream os = conn.getOutputStream();
        os.write(post_obj.toString().getBytes());
        os.flush();
        os.close();
        conn.connect();

        InputStream inputStream = conn.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        StringBuilder res = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
          res.append(line);
        }
        bufferedReader.close();
        inputStream.close();
        conn.disconnect();
        return res.toString();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      LockedSitesDetails activity = activityReference.get();
      pd.dismiss();

      CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
      param.setMargins(10,10,10,10);
      try {
        JSONObject flts_obj = new JSONObject(s);
        JSONArray arr = new JSONArray(flts_obj.getString("data"));
//        System.out.println(arr.toString());
        if(arr.length()>0) {
          for (int i = 0; i < arr.length(); i++) {
            final JSONObject obj = new JSONObject(arr.getString(i));
            String reason = !obj.getString("fault_type").equals("null") ? obj.getString("fault_type") : "";
            String bts_type = obj.getString("bts_type");
            String site_type = obj.getString("site_type");
            StringBuilder str = new StringBuilder();
            String b_name = obj.getString("bts_name");
            String rpid = "";
            str.append("<b>Bts Name :</b>");

            if(obj.getString("circle_id").equals("AP")) {
              try {
                String bts_site_id = obj.getString("bts_site_id");
                if (bts_site_id.startsWith("T4") && bts_site_id.length() == 20) {
                  rpid = bts_site_id.substring(14, 20);
                } else if (bts_site_id.startsWith("T4") && bts_site_id.length() > 20) {
                  rpid = obj.getString("bts_ip_id");
                }
              } catch (Exception e) {
                Log.e("TCS 4G ID Missing", "BTS_SITE is missing");
              }

              if (b_name.toUpperCase().contains(rpid.toUpperCase())) {
                str.append(addZeroWidthSpaces(b_name,25));
              } else {
                str.append(addZeroWidthSpaces(b_name + "_" + rpid, 25));
              }
            } else {
              str.append(addZeroWidthSpaces(b_name,25));
            }

            str.append("<br>");
            str.append("<b>BTS Site ID :</b>").append(addZeroWidthSpaces(obj.getString("bts_site_id"),25));
            str.append("<br>");
            str.append("<b>Bts Location :</b>").append(addZeroWidthSpaces(obj.getString("bts_location"),25));
            str.append("<br>");
            str.append("<b>Bts Type :</b>").append(getBtsType(bts_type));
            str.append("/").append(getSiteType(site_type));
            str.append("/").append(obj.getString("ssa_name"));
            str.append("/").append(obj.getString("make"));
            str.append("<br>");
            str.append("<b>Site Category: </b>").append(obj.getString("site_category"));
            str.append("<br>");
            str.append("<b>Locked Date :</b>").append(obj.getString("lock_date"));
            str.append("<br>");
            str.append("<b>Reason :</b>").append(reason);
            str.append("<br>");


            LinearLayout ll = new LinearLayout(activity);
            CardView card = new CardView(activity);
            card.setMaxCardElevation(5);
            card.setCardElevation(5);
            card.setLayoutParams(param);
            card.setPadding(10, 10, 10, 10);
            card.setRadius(30);
            card.setUseCompatPadding(true);
            String site_category = obj.getString("site_category");
            switch (site_category){
              case "SUPER_CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(LockedSitesDetails.this.getApplicationContext(),R.color.super_critical));
                break;
              case "CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(LockedSitesDetails.this.getApplicationContext(),R.color.critical));
                break;
              case "IMPORTANT":
                card.setCardBackgroundColor(ContextCompat.getColor(LockedSitesDetails.this.getApplicationContext(),R.color.important));
                break;
              default:
                card.setCardBackgroundColor(ContextCompat.getColor(LockedSitesDetails.this.getApplicationContext(),R.color.normal));
            }
            TextView tv = new TextView(activity);
//                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(15, 15, 15, 15);
            tv.setText(Html.fromHtml(str.toString()));
            tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            tv.setTypeface(Typeface.MONOSPACE);
            card.addView(tv);
//                        on click listner
            card.setOnClickListener((View v)-> {
              AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

              LayoutInflater inflater = LayoutInflater.from(v.getContext());
              View dialogView = inflater.inflate(R.layout.dialog_layout_locked, null);
              TextView tv_sitename = dialogView.findViewById(R.id.textbox_locked_site_name);
              TextView tv_ssaname = dialogView.findViewById(R.id.textbox_locked_ssaname);
              TextView tv_make = dialogView.findViewById(R.id.textbox_locked_make);
              TextView tv_lock_date = dialogView.findViewById(R.id.textbox_locked_date);
              Spinner spinner = dialogView.findViewById(R.id.spinner_reasons);
              try {
                tv_sitename.setText(Html.fromHtml("<b><font size='20'>Bts Name: </font></b>" + obj.getString("bts_name")));
                tv_ssaname.setText(Html.fromHtml("<b><font size='20'>SSA Name: </font></b>" + obj.getString("ssa_name")));
                tv_make.setText(Html.fromHtml("<b><font size='20'>Make: </font></b>" + obj.getString("make")));
                tv_lock_date.setText(Html.fromHtml("<b><font size='20'>Lock Date: </font></b>" + obj.getString("lock_date")));
                if (!fault_types.isEmpty() && !fault_types.get(0).equals("Select reason")) {
                  // Add "Select reason" as the first item if it's not already present
                  fault_types.add(0, "Select reason");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LockedSitesDetails.this, R.layout.support_simple_spinner_dropdown_item, fault_types);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                String fault_type = reason;
                int position = adapter.getPosition(reason);
                if(position>=0){
                  spinner.setSelection(position);
                } else {
                  spinner.setSelection(0);
                }
              } catch (JSONException e) {
                throw new RuntimeException(e);
              }

              builder.setView(dialogView);

              builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
              builder.setPositiveButton("Update",(dialog,which)-> {
                String selectedReason = spinner.getSelectedItem().toString();
                if ("Select reason".equals(selectedReason)) {
                  // Prompt the user to select a valid reason
                  Toast.makeText(LockedSitesDetails.this, "Please select a valid reason", Toast.LENGTH_SHORT).show();
                } else {
                  String msisdn = activity.sharedPreferences.getString("msisdn","");
                  String fault_reason = spinner.getSelectedItem().toString();
                  updateAction(obj, msisdn, faultMap.get(fault_reason) );
                  // Proceed with the selected reason
                }
              });

              AlertDialog alertDialog = builder.create();
              alertDialog.show();


//              AlertDialog alertDialog;
//              alertDialog = new AlertDialog.Builder(activity).create();
//              alertDialog.setTitle("Locked Site Update info");
//              alertDialog.setMessage("Developing....");
//              alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE,"Ok",(dialog,which)-> dialog.dismiss());
//              alertDialog.show();


//              Intent intent = new Intent(activity, LockedSiteReasonUpdate.class);
//              try {
//                intent.putExtra("bts_id", obj.getString("bts_id"));
//              } catch (JSONException e) {
//                e.printStackTrace();
//              }
//              activity.startActivity(intent);
            });
            ll.addView(card);
            activity.tl.addView(ll);
          }
        } else{
          AlertDialog alertDialog ;
          alertDialog = new AlertDialog.Builder(activity).create();
          alertDialog.setTitle("Info...");
          alertDialog.setMessage("No Bts are down");
          alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> activity.finish());
          alertDialog.show();
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

    }
  }

  public void updateAction(JSONObject obj, String username, String fault_reason){
    System.out.println(fault_reason);
    new UpdateReasonTask(this, obj, username, fault_reason).execute();
//    System.out.println(obj.toString());
//    // Refresh the activity
//    try {
//      System.out.println(obj.toString());
//      System.out.println(username);
//      System.out.println(fault_reason);
//
//      // You can refresh the activity if necessary
//      Intent intent = getIntent();
//      finish();
//      startActivity(intent);
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

  }

//  Update Reason Task
private class UpdateReasonTask extends AsyncTask<Void, Void, String> {
  private WeakReference<LockedSitesDetails> activityReference;
  private JSONObject obj;
  private String username;
  private String fault_reason;
  private ProgressDialog pd;

  public UpdateReasonTask(LockedSitesDetails context, JSONObject obj, String username, String fault_reason) {
    activityReference = new WeakReference<>(context);
    this.obj = obj;
    this.username = username;
    this.fault_reason = fault_reason;
  }

  @Override
  protected void onPreExecute() {
    LockedSitesDetails activity = activityReference.get();
    if (activity == null || activity.isFinishing()) return;

    pd = new ProgressDialog(activity);
    pd.setTitle("Updating Reason");
    pd.setMessage("Please wait...");
    pd.setCancelable(false);
    pd.show();
  }

  @Override
  protected String doInBackground(Void... voids) {
    LockedSitesDetails activity = activityReference.get();
    if (activity == null || activity.isFinishing()) return null;

    try {
      // Your API URL
//      String apiUrl = "https://yourapi.url/updateReason";
      Uri.Builder uri_builder_reason = new Uri.Builder()
          .scheme("https")
          .authority(Constants.getSecureBaseUrl())
          .appendPath(getString(R.string.url_locked_sites_reason_update));
      String url = uri_builder_reason.build().toString();

//      System.out.println(url);

      // Prepare the HTTP connection
      HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(url,"UTF-8"));
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token", ""));
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setDoOutput(true);
      JSONObject post_obj = new JSONObject();
      post_obj.put("bts_site_id", obj.getString("bts_site_id"));
      post_obj.put("bts_down_cause",fault_reason);
      post_obj.put("lock_date",obj.getString("lock_date"));
      post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
      System.out.println(post_obj.toString());

      // Send the payload
      OutputStream os = conn.getOutputStream();
      os.write(post_obj.toString().getBytes());
      os.flush();
      os.close();
      conn.connect();

      InputStream inputStream = conn.getInputStream();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      String line;
      StringBuilder res = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) {
        res.append(line);
      }
      bufferedReader.close();
      inputStream.close();
      conn.disconnect();
      System.out.println(res.toString());

      return res.toString();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(String result) {
    LockedSitesDetails activity = activityReference.get();
    if (activity == null || activity.isFinishing()) return;

    pd.dismiss();
    System.out.println(result);

    if (result != null) {
      // Handle the API response
      Toast.makeText(activity, "Reason updated successfully!", Toast.LENGTH_SHORT).show();
//      activity.updateAction(); // Refresh the activity
      Intent intent = getIntent();
      finish();
      startActivity(intent);
    } else {
      System.out.println(result);
      Toast.makeText(activity, "Failed to update reason.", Toast.LENGTH_SHORT).show();
    }
  }
}


//Details
public static class getLockedSitesDetails extends AsyncTask<String, String, String>{
  private final WeakReference<LockedSitesDetails> activityReference;

  public getLockedSitesDetails(LockedSitesDetails context) {
    activityReference = new WeakReference<>(context);
  }

  @Override
  protected String doInBackground(String... strings) {
    LockedSitesDetails activity = activityReference.get();
    try {
      HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
      conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
      conn.setRequestProperty("Context-Type","application/json; utf-8");
      conn.setRequestMethod("POST");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      JSONObject post_obj = new JSONObject();
      post_obj.put("circle_id", activity.circle_id);
      post_obj.put("ssa_id",activity.ssa_id);
      post_obj.put("vendor_id",activity.vendor_id);
      post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));

      OutputStream os = conn.getOutputStream();
      os.write(post_obj.toString().getBytes());
      os.flush();
      os.close();
      conn.connect();

      InputStream inputStream = conn.getInputStream();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      String line;
      StringBuilder res = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) {
        res.append(line);
      }
      bufferedReader.close();
      inputStream.close();
      conn.disconnect();
      return res.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
// Get BTS Type
  private static String getBtsType(String bts_type){
    String btstype;
    switch (bts_type){
      case "U":
        btstype = "UMTS";
        break;
      case "G":
        btstype = "GSM";
        break;
      case "L":
        btstype = "LTE";
        break;
      default:
        btstype ="";
    }
    return btstype;
  }

//  Get Site Type

  public static String getSiteType(String site_type){
    String tmp_site_type;
    switch(site_type){
      case "BS":
        tmp_site_type="BSNL";
        break;
      case "NB":
        tmp_site_type="NBSNL";
        break;
      case "IP":
        tmp_site_type="IP";
        break;
      case "US":
        tmp_site_type="USO";
        break;
      default:
        tmp_site_type="UN-IDENTIFIED";
    }
    return tmp_site_type;
  }


}
