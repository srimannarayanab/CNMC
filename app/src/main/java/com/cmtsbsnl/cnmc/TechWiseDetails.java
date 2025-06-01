package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TechWiseDetails extends SessionActivity {
  private String circle_id , ssa_id, bts_type, vendor_id, band, project, legacy;
  private TableLayout tl;
  private SharedPreferences sharedPreferences;
  private HashMap<String, String> operators;
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
//        Toast.makeText(TechWiseDetails.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
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
                GenerateCardString generateCardString = new GenerateCardString(TechWiseDetails.this);
                String optr_id = obj.getString("operator_id");
                String opr_name = Config.getOperatorNames(TechWiseDetails.this.operators, optr_id);
                SpannableStringBuilder str = generateCardString.CardString(obj, opr_name);
                LinearLayout ll = new LinearLayout(TechWiseDetails.this);
                CardView card = new CardView(TechWiseDetails.this);
                card.setMaxCardElevation(5);
                card.setCardElevation(5);
                card.setLayoutParams(param);
                card.setPadding(10, 10, 10, 10);
                card.setRadius(30);
                card.setUseCompatPadding(true);
                String site_category = obj.getString("site_category");
                switch (site_category){
                  case "SUPER_CRITICAL":
                    card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.super_critical));
                    break;
                  case "CRITICAL":
                    card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.critical));
                    break;
                  case "IMPORTANT":
                    card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.important));
                    break;
                  default:
                    card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.normal));
                }
                TextView tv = new TextView(TechWiseDetails.this);
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
                  Intent intent = new Intent(TechWiseDetails.this, ReasonUpdate.class);
                  try {
                    intent.putExtra("bts_id", obj.getString("bts_id"));
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }
                  TechWiseDetails.this.startActivity(intent);
                });
                ll.addView(card);
                TechWiseDetails.this.tl.addView(ll);
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



  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tech_wise_details);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    Intent intent;
    TextView header;
    MyTask myTask;
    ImageButton toXlsx;

    intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");
    ssa_id = intent.getStringExtra("ssa_id");
    bts_type = intent.getStringExtra("bts_type");
    if (intent.hasExtra("vendor_id")) {
      vendor_id = intent.getStringExtra("vendor_id");
    }
    if(intent.hasExtra("band")){
      band = intent.getStringExtra("band");
    }

    if(intent.hasExtra("project")){
      project = intent.getStringExtra("project");
    }

    if(intent.hasExtra("legacy")){
      legacy = intent.getStringExtra("legacy");
    }
    String h_txt = ssa_id;
    if(ssa_id.equals("%")){
      h_txt ="";
    }

    Log.d("Extras Print", vendor_id+" "+band);

    header = findViewById(R.id.textView1);
    header.setText(getString(R.string.header_tech_circle_ssa, circle_id, h_txt));
    header.setTextSize(18);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    toolbar.setFocusable(true);
    toolbar.setFocusableInTouchMode(true);
    toolbar.requestFocus();
    toolbar.clearFocus();

    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener(v -> {
//        Toast.makeText(this, "Pressed",Toast.LENGTH_SHORT).show();
        header.setVisibility(View.VISIBLE);
        header.setText(getString(R.string.header_tech_circle_ssa, circle_id, ssa_id));
        header.setTextSize(18);
        super.onBackPressed();
      });
    }

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_techwise_down_details));

    String optrnames = sharedPreferences.getString("optrs","hello");
//        System.out.println(optrnames);
    Gson gson = new Gson();
    Map map = gson.fromJson(optrnames, Map.class);
//        System.out.println(map);
    operators = new HashMap<>();
//        Iterator<String> itr = map.keySet().iterator();
    for(Object opr :map.keySet()){
      String optr_id = opr.toString();
      String optr_name = map.get(optr_id).toString();
      operators.put( optr_id, optr_name);
    }
//        System.out.println(operators);
//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    myTask = new MyTask(this);
    myTask.execute(uri_builder.toString());

//        TO Excel Generation
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));

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
          // Perform actions with the selected item
//          Log.d("Filtered Data Before", res.toString());
//          Toast.makeText(getApplicationContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
          GenerateFilteredData(res, selectedItem);
//          Log.d("Filtered Data", filtered_data.toString());

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
      String flts = new getFaultsDetails(this).execute(url).get();
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
//        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        Log.e("JSON Error", url_obj.getString("error"));
        startActivity(new Intent(this, SesssionLogout.class));
        finish();
      }
      JSONArray arr = new JSONArray(url_obj.getString("data"));
      for(int i=0; i<arr.length(); i++) {
        HSSFRow drow = sheet.createRow(i + 1);
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
    ProgressDialog pd;
    private final WeakReference<TechWiseDetails> activityReference;

    private MyTask(TechWiseDetails context) {
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
      TechWiseDetails activity = activityReference.get();
      try {
        HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"));
        conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        JSONObject obj = new JSONObject();
        obj.put("circle_id", activity.circle_id);
        obj.put("ssa_id", activity.ssa_id);
        obj.put("bts_type",activity.bts_type);
        if(activity.vendor_id !=null ){
          obj.put("vendor_id", activity.vendor_id);
        }
        if(activity.band !=null){
          obj.put("band", activity.band);
        }
        if(activity.project !=null){
          obj.put("project", activity.project);
        }

        obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        String input = obj.toString();
//        Log.d("Extras Print", input);
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        os.close();
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
      TechWiseDetails activity = activityReference.get();
      pd.dismiss();
      CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
      param.setMargins(10,10,10,10);
      try {
        JSONObject url_obj = new JSONObject(s);
        if (!url_obj.getString("result").equals("true")) {
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
          activity.finish();
        }
        JSONArray arr = new JSONArray(url_obj.getString("data"));
        JSONArray filteredArray = arr; // Default to arr

        if (activity != null) {
          activity.res = arr;
          if(activity.getIntent() !=null && activity.getIntent().hasExtra("legacy")) {
            if (activity.legacy.equals("Y")) {
              Log.d("LegacyNW", "Legacy network");

              try {
                // ✅ Correct way: Handle JSONException inside the Stream
                filteredArray = new JSONArray(
                    IntStream.range(0, arr.length())   // Iterate over indices
                        .mapToObj(i -> {
                          try {
                            return arr.getJSONObject(i); // Extract JSONObject safely
                          } catch (Exception e) {
                            Log.d("JSONException", "Error at index " + i + ": " + e.toString());
                            return null; // Skip invalid entries
                          }
                        })
                        .filter(obj -> obj != null && obj.optString("vendor_name", "").equals("TEJAS") == false)
                        .collect(Collectors.toList()) // Collect into List<JSONObject>
                );

                // ✅ Log the filtered JSON array
                Log.d("Filtered JSON", filteredArray.toString(2)); // Pretty-print JSON in logs

              } catch (Exception e) {
                Log.d("JSONException", "Unexpected error: " + e.toString());
              }
            }
          }
        }

        if (filteredArray.length() > 0) {
          assert activity != null;
          TextView cnttextview = activity.findViewById(R.id.countTextView);
          cnttextview.setText(String.valueOf(filteredArray.length()));
          for (int i = 0; i < filteredArray.length(); i++) {
            GenerateCardString generateCardString = new GenerateCardString(activity);
            final JSONObject obj = new JSONObject(filteredArray.getString(i));
            String optr_id = obj.getString("operator_id");
            assert activity != null;
            String opr_name = Config.getOperatorNames(activity.operators, optr_id);
//            String str = Constants.getBtsDownInfo(obj, activity.operators);
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
            switch (site_category) {
              case "SUPER_CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.super_critical));
                break;
              case "CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.critical));
                break;
              case "IMPORTANT":
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.important));
                break;
              default:
                card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.normal));
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
            card.setOnClickListener(v -> {
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
          alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> activity.finish());
          alertDialog.show();
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

    }
  }

  public static class getFaultsDetails extends AsyncTask<String, String, String> {
    private final WeakReference<TechWiseDetails> activityReference1;

    public getFaultsDetails(TechWiseDetails context) {
      activityReference1 = new WeakReference<>(context);
    }
    @Override
    protected String doInBackground(String... strings) {
      TechWiseDetails activity = activityReference1.get();
      try {
        HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"));
        conn.setRequestProperty("Authorization", activity.sharedPreferences.getString("web_token",""));
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        JSONObject obj = new JSONObject();
        obj.put("circle_id", activity.circle_id);
        obj.put("ssa_id", activity.ssa_id);
        obj.put("bts_type",activity.bts_type);
        if(activity.vendor_id != null){
          obj.put("vendor_id", activity.vendor_id);
        }
        if(activity.band !=null ){
          obj.put("band", activity.band);
        }
        if(activity.project !=null){
          obj.put("project", activity.project);
        }

        obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        String input = obj.toString();
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        os.close();
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
            GenerateCardString generateCardString = new GenerateCardString(TechWiseDetails.this);
            String optr_id = obj.getString("operator_id");
            String opr_name = Config.getOperatorNames(TechWiseDetails.this.operators, optr_id);
            SpannableStringBuilder str = generateCardString.CardString(obj, opr_name);
            LinearLayout ll = new LinearLayout(TechWiseDetails.this);
            CardView card = new CardView(TechWiseDetails.this);
            card.setMaxCardElevation(5);
            card.setCardElevation(5);
            card.setLayoutParams(param);
            card.setPadding(10, 10, 10, 10);
            card.setRadius(30);
            card.setUseCompatPadding(true);
            String site_category = obj.getString("site_category");
            switch (site_category){
              case "SUPER_CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.super_critical));
                break;
              case "CRITICAL":
                card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.critical));
                break;
              case "IMPORTANT":
                card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.important));
                break;
              default:
                card.setCardBackgroundColor(ContextCompat.getColor(TechWiseDetails.this.getApplicationContext(),R.color.normal));
            }
            TextView tv = new TextView(TechWiseDetails.this);
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
              Intent intent = new Intent(TechWiseDetails.this, ReasonUpdate.class);
              try {
                intent.putExtra("bts_id", obj.getString("bts_id"));
              } catch (JSONException e) {
                e.printStackTrace();
              }
              TechWiseDetails.this.startActivity(intent);
            });
            ll.addView(card);
            TechWiseDetails.this.tl.addView(ll);
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