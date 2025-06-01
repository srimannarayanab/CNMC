package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CategoryWiseDetails extends SessionActivity {
  private TableLayout tl;
  private String circle, ssaname, criteria;
  private SharedPreferences sharedPreferences;
  private HashMap<String, String> operators;
  private final SimpleDateFormat sfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  public JSONArray res ;


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu to show the search icon
    getMenuInflater().inflate(R.menu.menu_toolbar, menu);

    // Find the SearchView
    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) searchItem.getActionView();

    // Optional: Set up the SearchView listener
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        // Handle query submission (e.g., start a search)
        Toast.makeText(CategoryWiseDetails.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        // Handle query text change (e.g., live search)

        JSONArray filteredArray = new JSONArray();
        if(!newText.isEmpty()){
          ScrollView scrollView = findViewById(R.id.scrollView);
          ViewGroup container = (ViewGroup) scrollView.getChildAt(0); // Assuming ScrollView has one child
          container.removeAllViews();
        }

        try {
          for (int i = 0; i < res.length(); i++) {
            JSONObject jsonObject = res.getJSONObject(i);
            // Check if "Bts_name" contains the search text
            if (jsonObject.optString("bts_name").toLowerCase().contains(newText.toLowerCase())
                || jsonObject.optString("bts_location").toLowerCase().contains(newText.toLowerCase())
                || jsonObject.optString("bts_site_id").toLowerCase().contains(newText.toLowerCase())
            ) {
              filteredArray.put(jsonObject);
            }
          }
//            assert filteredArray != null;
//            System.out.println(filteredArray.toString());
        } catch(JSONException e ){
          e.printStackTrace();
        }
//          if(filteredArray.length()>0){
        for(int i=0; i<filteredArray.length(); i++){
          final JSONObject obj;
          CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
          param.setMargins(10,10,10,10);
          try {
            obj = new JSONObject(filteredArray.getString(i));
            GenerateCardString generateCardString = new GenerateCardString(CategoryWiseDetails.this);
            String optr_id = obj.getString("operator_id");
            String opr_name = Config.getOperatorNames(CategoryWiseDetails.this.operators, optr_id);
            SpannableStringBuilder str = generateCardString.CardString(obj, opr_name);
            LinearLayout ll = new LinearLayout(CategoryWiseDetails.this);
            CardView card = new CardView(CategoryWiseDetails.this);
            card.setMaxCardElevation(5);
            card.setCardElevation(5);
            card.setLayoutParams(param);
            card.setPadding(10, 10, 10, 10);
            card.setRadius(30);
            card.setUseCompatPadding(true);
            String site_category = obj.getString("site_category");
            switch (site_category){
              case "SUPER_CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.super_critical));
                break;
              case "CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.critical));
                break;
              case "IMPORTANT":
                card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.important));
                break;
              default:
                card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.normal));
            }
            TextView tv = new TextView(CategoryWiseDetails.this);
            //                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(15, 15, 15, 15);
            tv.setText(str);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            tv.setTypeface(Typeface.MONOSPACE);

            card.addView(tv);
            card.setOnClickListener(v -> {
              Intent intent = new Intent(CategoryWiseDetails.this, ReasonUpdate.class);
              try {
                intent.putExtra("bts_id", obj.getString("bts_id"));
              } catch (JSONException e) {
                e.printStackTrace();
              }
              CategoryWiseDetails.this.startActivity(intent);
            });
            ll.addView(card);
            CategoryWiseDetails.this.tl.addView(ll);
          } catch (JSONException e) {
            e.printStackTrace();
          }

        }
        return true;
      }
    });

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_search:
        // Clear the text in the TextView
        TextView textViewToolbar = findViewById(R.id.textView1);
        textViewToolbar.setText("");  // Clears the text

        // Optionally, you can hide the TextView if needed
        // textViewToolbar.setVisibility(View.GONE);  // Hides the TextView

        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_category_wise_details);

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    Intent intent;
    TextView header;
    ImageButton toXlsx;

    String optrnames = null;

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    intent = getIntent();
    circle = intent.getStringExtra("circle");
    ssaname = intent.getStringExtra("ssaname");
    criteria = intent.getStringExtra("criteria");

    String h_txt = ssaname;
    if(ssaname.equals("%")){
      h_txt ="";
    }

    header = findViewById(R.id.textView1);
    header.setText(getString(R.string.header_downdetails, circle, h_txt));


    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener((View v)-> onBackPressed());
    }

    ImageButton homeBtn =  toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener((View v)->
        startActivity(new Intent(this, Navigational.class)));

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
      optrnames = sharedPreferences.getString("optrs","hello");
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }
//        System.out.println(optrnames);
    Gson gson = new Gson();
    Map<String, String> map = gson.fromJson(optrnames, Map.class);
//        System.out.println(map);
    operators = new HashMap<>();
//        Iterator<String> itr = map.keySet().iterator();
//        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
//            String optr_id = (String) it.next();
//            String optr_name = map.get(optr_id).toString();
//            operators.put( optr_id, optr_name);
//        }
    for(String k: map.keySet()){
      operators.put(k, map.get(k));
    }

//        System.out.println(operators);
//        Uri Builder
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_categorywise_down_details));

//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    MyTask mytask = new MyTask(this);
    mytask.execute(uri_builder.toString(),circle, ssaname, criteria);

//        TO Excel Generation
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener((View v)->buttonCreateExcel(uri_builder.toString()));

    //    Spinner add Options
    Spinner spinner = findViewById(R.id.spinner_below_actionbar);
    String[] options = getResources().getStringArray(R.array.spinner_fault_details);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options){
      @Override
      public boolean isEnabled(int position) {
        // Disable the first item
        return position != 0;
      }

      @Override
      public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        // Make the first item appear grayed out
        TextView textView = (TextView) view;
        if (position == 0) {
          textView.setTextColor(Color.GRAY);
        } else {
          textView.setTextColor(Color.BLACK);
        }
        return view;
      }
    };
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

// Spinner on selected
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
          String selectedItem = (String) parent.getItemAtPosition(position);
          GenerateFilteredData(res, selectedItem);

        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        // Handle case where nothing is selected
      }
    });
    //  Spinner Drop down menu

  }



  private void buttonCreateExcel(String url) {
    try {
      String flts = new getFaultsDetails(this).execute(url, circle, ssaname, criteria).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("Faults");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("Btsname");
      row.createCell(1).setCellValue("ssaname");
      row.createCell(2).setCellValue("vendor_name");
      row.createCell(3).setCellValue("bts_type");
      row.createCell(4).setCellValue("site_type");
      row.createCell(5).setCellValue("outsrc_name");
      row.createCell(6).setCellValue("down_time");
      row.createCell(7).setCellValue("cumm_downtime");
      row.createCell(8).setCellValue("fault_type");
      row.createCell(9).setCellValue("fault_update_by");
      row.createCell(10).setCellValue("fault_update_date");

      JSONObject url_obj = new JSONObject(flts);
      if(!url_obj.getString("result").equals("true")){
        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SesssionLogout.class));
        finish();
      }


      JSONArray arr = new JSONArray(url_obj.getString("data"));
      for(int i=0; i<arr.length(); i++){
        HSSFRow drow = sheet.createRow(i+1);
        JSONObject obj = arr.getJSONObject(i);
        drow.createCell(0).setCellValue(obj.getString("bts_name"));
        drow.createCell(1).setCellValue(obj.getString("ssa_name"));
        drow.createCell(2).setCellValue(obj.getString("vendor_name"));
        drow.createCell(3).setCellValue(obj.getString("bts_type"));
        drow.createCell(4).setCellValue(obj.getString("sitetype"));
        drow.createCell(5).setCellValue(obj.getString("outsrc_name"));
        drow.createCell(6).setCellValue(obj.getString("bts_status_dt"));
        drow.createCell(7).setCellValue(Config.calculateTime(obj.getInt("cumm_down_time")));
        drow.createCell(8).setCellValue(obj.getString("fault_type"));
        drow.createCell(9).setCellValue(obj.getString("fault_updated_by"));
        drow.createCell(10).setCellValue(obj.getString("fault_update_date"));


      }

//      if(isExternalStorageWritable()) {
//        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/Faults.xls");
//        try {
//          FileOutputStream fileOutputStream = new FileOutputStream(filepath);
//          workbook.write(fileOutputStream);
//          fileOutputStream.flush();
//          fileOutputStream.close();
//          Toast.makeText(getApplicationContext(), "Excel file generated sucessfully in downloads folder", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//      } else{
//        Toast.makeText(getApplicationContext(), "Sorry not permitted",Toast.LENGTH_SHORT).show();
//      }
      if (isExternalStorageWritable()) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Faults.xls");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        try {
          OutputStream outputStream = getContentResolver().openOutputStream(uri);
          workbook.write(outputStream);
          outputStream.flush();
          outputStream.close();
          Toast.makeText(getApplicationContext(), "Excel file generated successfully in Downloads folder", Toast.LENGTH_SHORT).show();

          // Open the file
          Intent openIntent = new Intent(Intent.ACTION_VIEW);
          openIntent.setDataAndType(uri, "application/vnd.ms-excel");
          openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          startActivity(Intent.createChooser(openIntent, "Open with"));
        } catch (Exception e) {
//                    e.printStackTrace();
          Toast.makeText(getApplicationContext(), "Failed to generate Excel file", Toast.LENGTH_SHORT).show();
        }
      } else {
        Toast.makeText(getApplicationContext(), "External storage is not writable", Toast.LENGTH_SHORT).show();
      }

    } catch (JSONException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  private boolean isExternalStorageWritable() {
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  private static class MyTask extends AsyncTask<String, String, String> {
    WeakReference<CategoryWiseDetails> activityReference;
    MyTask(CategoryWiseDetails context){
      activityReference = new WeakReference<>(context);
    }
    ProgressDialog pd;

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
      CategoryWiseDetails activity = activityReference.get();
      try {
        HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
        conn.setRequestProperty("Authorization",activity.sharedPreferences.getString("web_token",""));
        conn.setRequestProperty("Content-Type","application/json; utf-8");
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle", strings[1]);
        post_obj.put("ssaname", strings[2]);
        post_obj.put("criteria",strings[3]);
        String input = post_obj.toString();
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        os.close();
        conn.connect();

        InputStream inputStream = conn.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
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
      CategoryWiseDetails activity = activityReference.get();
      pd.dismiss();
      CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
      param.setMargins(10,10,10,10);
      try {
        JSONObject url_obj = new JSONObject(s);

        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
          activity.finish();
        }
        JSONArray arr = new JSONArray(url_obj.getString("data"));
        if (activity != null) {
          activity.res = arr;
        }
        if(arr.length()>0){
          assert activity != null;
          TextView cnttextview = activity.findViewById(R.id.countTextView);
          cnttextview.setText(String.valueOf(arr.length()));

          for(int i=0; i<arr.length(); i++){
            GenerateCardString generateCardString = new GenerateCardString(activity);
            final JSONObject obj = new JSONObject(arr.getString(i));
            String optr_id = obj.getString("operator_id");
            String opr_name = Config.getOperatorNames(activity.operators, optr_id);
//                    System.out.println(optr_id +"->"+ opr_name);
            if(opr_name.equals("Not Applicable")){
              opr_name="";
            } else {
              opr_name=" - "+opr_name;
            }
            String reason = obj.getString("fault_type").isEmpty() ? obj.getString("fault_type") :"";
            SpannableStringBuilder str = generateCardString.CardString(obj, opr_name);
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
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.super_critical));
                break;
              case "CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.critical));
                break;
              case "IMPORTANT":
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.important));
                break;
              default:
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.normal));
            }
            TextView tv = new TextView(activity);
//                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(15, 15, 15, 15);
            tv.setText(str);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            tv.setTypeface(Typeface.MONOSPACE);

            card.addView(tv);
            card.setOnClickListener((View v)-> {
              Intent intent = new Intent(activity, ReasonUpdate.class);
              try {
                intent.putExtra("bts_id", obj.getString("bts_id"));
              } catch (JSONException e) {
                e.printStackTrace();
              }
              activity.startActivity(intent);
            });
            ll.addView(card);
            activity.tl.addView(ll);
          }
        } else {
          AlertDialog alertDialog;
          alertDialog = new AlertDialog.Builder(activity).create();
          alertDialog.setTitle("Info...");
          alertDialog.setMessage("No Bts are down");
          alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Ok",
              ((DialogInterface dialogInterface, int which) -> activity.finish()));
          alertDialog.show();
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

    }
  }

  public static class getFaultsDetails extends AsyncTask<String, String, String> {
    private final WeakReference<CategoryWiseDetails> activityReference1;

    public getFaultsDetails(CategoryWiseDetails context) {
      activityReference1 = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... strings) {
      CategoryWiseDetails activity = activityReference1.get();
      try {
        HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
        conn.setRequestProperty("Content-Type","application/json; utf-8");
        conn.setRequestProperty("Authorization",activity.sharedPreferences.getString("web_token",""));
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle", strings[1]);
        post_obj.put("ssaname", strings[2]);
        post_obj.put("criteria",strings[3]);
        String input = post_obj.toString();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(input.getBytes());
        outputStream.flush();
        outputStream.close();
        conn.connect();

        InputStream inputStream = conn.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
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

  //  Filtered Item Data to be organized
  public void GenerateFilteredData(JSONArray arr, String selectedString) {
//    Log.d("Selected String",selectedString);
    JSONArray filteredArray = new JSONArray();
    try{
      if(selectedString.equals("All")){
        filteredArray=arr;
      } else {
        for (int i = 0; i < arr.length(); i++) {
          JSONObject obj = new JSONObject(arr.getString(i));
          String bts_site_id = obj.getString("bts_site_id");
//        Tejas 9.2 Band -1 Sites
          if (selectedString.startsWith("Tejas-Band")) {
            if (bts_site_id.length() == 20 && bts_site_id.startsWith("T4")) {
              String band = bts_site_id.substring(4, 6);
//            Log.d("Filtered Data", "9.2"+" "+band );
              if (selectedString.equals("Tejas-Band-01") && band.equals("01")) {
                filteredArray.put(obj);
              } else if (selectedString.equals("Tejas-Band-28") && band.equals("28")) {
                filteredArray.put(obj);
              } else if (selectedString.equals("Tejas-Band-41") && band.equals("41")) {
                filteredArray.put(obj);
              }
            }
          } else if (selectedString.startsWith("Saturation")) { // Saturation Project
            if (bts_site_id.length() > 20 && bts_site_id.startsWith("T4")) {
              String band = bts_site_id.substring(4, 6);
              if (selectedString.equals("Saturation-Band-01") && band.equals("01")) {
                filteredArray.put(obj);
              } else if (selectedString.equals("Saturation-Band-28") && band.equals("28")) {
                filteredArray.put(obj);
              } else if (selectedString.equals("Saturation-Band-41") && band.equals("41")) {
                filteredArray.put(obj);
              }
            }
          } else if (selectedString.equals("Reason-NotUpdated")) {
            String fault_updated_by = obj.getString("fault_updated_by");
            if (obj.isNull("fault_updated_by")) {
              filteredArray.put(obj);
            }
          } else if (selectedString.equals("LTE-Legacy N/W")) {
            String bts_type = obj.getString("bts_type");
            if (bts_type.equals("LTE") && !bts_site_id.startsWith("T4")) {
              filteredArray.put(obj);
            }
          } else if (selectedString.equals("Legacy N/W 2G/3G")) {
            String bts_type = obj.getString("bts_type");
            if (bts_type.equals("GSM") || bts_type.equals("UMTS")) {
              filteredArray.put(obj);
            }
          }
        }
      }

      TextView cnttextview = findViewById(R.id.countTextView);
      cnttextview.setText(String.valueOf(filteredArray.length()));

//        Add the card Views for the selected Data
      ScrollView scrollView = findViewById(R.id.scrollView);
      ViewGroup container = (ViewGroup) scrollView.getChildAt(0); // Assuming ScrollView has one child
      container.removeAllViews();

      for(int i=0; i<filteredArray.length(); i++){
        final JSONObject obj;
        CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        param.setMargins(10,10,10,10);
        try {
          obj = new JSONObject(filteredArray.getString(i));
          GenerateCardString generateCardString = new GenerateCardString(CategoryWiseDetails.this);
          String optr_id = obj.getString("operator_id");
          String opr_name = Config.getOperatorNames(CategoryWiseDetails.this.operators, optr_id);
          SpannableStringBuilder str = generateCardString.CardString(obj, opr_name);
          LinearLayout ll = new LinearLayout(CategoryWiseDetails.this);
          CardView card = new CardView(CategoryWiseDetails.this);
          card.setMaxCardElevation(5);
          card.setCardElevation(5);
          card.setLayoutParams(param);
          card.setPadding(10, 10, 10, 10);
          card.setRadius(30);
          card.setUseCompatPadding(true);
          String site_category = obj.getString("site_category");
          switch (site_category){
            case "SUPER_CRITICAL":
              card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.super_critical));
              break;
            case "CRITICAL":
              card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.critical));
              break;
            case "IMPORTANT":
              card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.important));
              break;
            default:
              card.setCardBackgroundColor(ContextCompat.getColor(CategoryWiseDetails.this.getApplicationContext(),R.color.normal));
          }
          TextView tv = new TextView(CategoryWiseDetails.this);
          //                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
          tv.setTextColor(Color.BLACK);
          tv.setGravity(Gravity.CENTER);
          tv.setPadding(15, 15, 15, 15);
          tv.setText(str);
          tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
          tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
          tv.setTypeface(Typeface.MONOSPACE);

          card.addView(tv);
          card.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryWiseDetails.this, ReasonUpdate.class);
            try {
              intent.putExtra("bts_id", obj.getString("bts_id"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
            CategoryWiseDetails.this.startActivity(intent);
          });
          ll.addView(card);
          CategoryWiseDetails.this.tl.addView(ll);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

    } catch (Exception e) {
      Log.e("JSON Exception Filtered array", e.toString());
    }
//    return true;;
  }

}
