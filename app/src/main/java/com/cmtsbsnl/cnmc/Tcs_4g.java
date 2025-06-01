package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.sql.SQLOutput;
import java.util.concurrent.ExecutionException;

public class Tcs_4g extends SessionActivity {
  private TableLayout tl;
  private String circle_id;
  private SharedPreferences sharedPreferences;
  private String faults;


  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tcs4g);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    MyTask myTask;
    ImageButton toXlsx, shareBy, tcs4g_sa ,legacy_nw;

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      Log.e("PreferencesError", "Failed to get encrypted shared preferences", e);
    }

    Intent intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");
    TextView hometextview = findViewById(R.id.textView1);
    hometextview.setText(getString(R.string.header_tcs4g,circle_id));

    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

//        Build the URL to consume API
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_ssa_tcs4g_down));

    tl = findViewById(R.id.tbl_lyt);
    myTask =new MyTask(this);
    myTask.execute(uri_builder.toString());

//    To Excel button
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener(v->{
      buttonCreateExcel();
    });


//    Pull to Refresh
    final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
    pullToRefresh.setOnRefreshListener(() -> {
      tl.removeAllViews();
      new MyTask(this).execute(uri_builder.toString());
      pullToRefresh.setRefreshing(false);
    });

    tcs4g_sa = findViewById(R.id.tcs4g_sa);
    tcs4g_sa.setOnClickListener( v -> {
      Intent intent_tcs4g_sa = new Intent(this, Tcs4g_sa.class);
      intent_tcs4g_sa.putExtra("circle_id",circle_id);
      startActivity(intent_tcs4g_sa);
    });

    legacy_nw = findViewById(R.id.legacy_nw);
    legacy_nw.setOnClickListener(v->{
      Intent intent_legacy = new Intent(this, LegacyNwSsa.class);
      intent_legacy.putExtra("circle_id", circle_id);
      startActivity(intent_legacy);
    });
  }
//  Verify the External storage writable
  private boolean isExternalStorageWritable(){
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

// Export to Excel

  private void buttonCreateExcel() {
    try {
//      String flts = new exportFaults(this).execute(url).get();
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("TechWise");
//      Log.d("Excel Faults", faults);
      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("SSAID");
      row.createCell(1).setCellValue("Tcs4G-B01");
      row.createCell(2).setCellValue("Tcs4G-B28");
      row.createCell(3).setCellValue("Tcs4G-B41");
      row.createCell(4).setCellValue("Total-down");
      row.createCell(5).setCellValue("Total-Sites");
      row.createCell(6).setCellValue("Perc");

      JSONObject url_obj = new JSONObject(faults);
      if(!url_obj.getString("result").equals("true")){
        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SesssionLogout.class));
      }
      JSONArray arr =new JSONArray(url_obj.getString("data"));
      for(int i=0; i<arr.length(); i++){
        HSSFRow drow = sheet.createRow(i+1);
        JSONObject obj = arr.getJSONObject(i);
        drow.createCell(0).setCellValue(obj.getString("ssaid"));
        drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("tcs_4g_b1")));
        drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("tcs_4g_b28")));
        drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("tcs_4g_b41")));
        drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("total_down")));
        drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("total")));
        drow.createCell(6).setCellValue(Double.parseDouble(obj.getString("perc_down")));
      }

      if(isExternalStorageWritable()) {
        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/Tcs4G_92_Faults.xls");
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


    } catch (JSONException e) {
      e.printStackTrace();
    }


  }

//  On lcick process the export


  private static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<Tcs_4g> activityReference;
    private MyTask(Tcs_4g context) {
      activityReference = new WeakReference<>(context);
    }

    ProgressDialog pd ;

    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching Alarms...");
      pd.setMessage("Processing.... ");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected String doInBackground(String... params) {
      Tcs_4g activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle_id", activity.circle_id);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        Log.e("t","error while parse json object", e);
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
//      System.out.println(s.toString());
      Tcs_4g activity = activityReference.get();
      activity.faults = s;
      pd.dismiss();
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_ssa);
      tr1.addView(tv1);
      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_band_01);
      tr1.addView(tv2);
      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_band_28);
      tr1.addView(tv3);
      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_band_41);
      tr1.addView(tv4);
      TextView tv5 = new TextView(activity);
      tv5.setText(R.string.header_total);
      tr1.addView(tv5);
      TextView tv6 = new TextView(activity);
      tv6.setText(R.string.header_perc);
      tr1.addView(tv6);
      for (int k = 0; k < tr1.getChildCount(); k++) {
        View v = tr1.getChildAt(k);
        if (v instanceof TextView) {
          ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
          v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
          v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
          ((TextView) v).setGravity(Gravity.CENTER);
          ((TextView) v).setTextSize(15);
        }
      }
      activity.tl.addView(tr1);

      try {
        JSONObject url_obj = new JSONObject(s);
        Log.d("TCS 4G Down Details", s.toString());
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
          activity.finish();
        }
        JSONArray obj = new JSONArray(url_obj.getString("data"));
        for(int i=0; i<obj.length(); i++){
          TableRow tr = new TableRow(activity);
          Button btn = new Button(activity);
          JSONObject obj1 = new JSONObject(obj.getString(i));
          final String ssa_id = obj1.getString("ssaid");
//          final String circle_id = obj1.getString("circle_id");
          String tcs_4g_b1 = obj1.getString("tcs_4g_b1");
          String tcs_4g_b28 = obj1.getString("tcs_4g_b28");
          String tcs_4g_b41 = obj1.getString("tcs_4g_b41");
          String tcs_4g_b1_cnt = obj1.getString("tcs_4g_b1_cnt");
          String tcs_4g_b28_cnt = obj1.getString("tcs_4g_b28_cnt");
          String tcs_4g_b41_cnt = obj1.getString("tcs_4g_b41_cnt");
          String total_down = obj1.getString("total_down");
          String total = obj1.getString("total");
          String perc = obj1.getString("perc_down");
          boolean highlight = false;
          try{
            double perc_faults = obj1.getDouble("perc");
            if(perc_faults>10){
              highlight = true;
            }
          } catch (Exception e){
            Log.e("Percentage Error","Error while parsing percentage", e);
          }

          btn.setText(ssa_id);
          tr.addView(btn);
          btn.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id", activity.circle_id);
            intent.putExtra("ssa_id", ssa_id);
            intent.putExtra("bts_type","L");
            intent.putExtra("vendor_id","9");
            intent.putExtra("project","9.2");
            activity.startActivity(intent);
          });

          Button btn_tcs_4g_b1 = new Button(activity);
          btn_tcs_4g_b1.setText(activity.getString(R.string.down_wrt_total,tcs_4g_b1,tcs_4g_b1_cnt));
          btn_tcs_4g_b1.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",activity.circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","L");
            intent.putExtra("vendor_id","9");
            intent.putExtra("band","01");
            intent.putExtra("project","9.2");
            activity.startActivity(intent);
          });
          tr.addView(btn_tcs_4g_b1);


          Button btn_tcs_4g_b28 = new Button(activity);
          btn_tcs_4g_b28.setText(activity.getString(R.string.down_wrt_total, tcs_4g_b28, tcs_4g_b28_cnt));

          btn_tcs_4g_b28.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",activity.circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","L");
            intent.putExtra("vendor_id","9");
            intent.putExtra("band","28");
            intent.putExtra("project","9.2");
            activity.startActivity(intent);
          });
          tr.addView(btn_tcs_4g_b28);

          Button btn_tcs_4g_b41 = new Button(activity);
          btn_tcs_4g_b41.setText(activity.getString(R.string.down_wrt_total, tcs_4g_b41, tcs_4g_b41_cnt));
          btn_tcs_4g_b41.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",activity.circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","L");
            intent.putExtra("vendor_id","9");
            intent.putExtra("band","41");
            intent.putExtra("project","9.2");
            activity.startActivity(intent);
          });
          tr.addView(btn_tcs_4g_b41);

          Button btn_down = new Button(activity);
          btn_down.setText(activity.getString(R.string.down_wrt_total, total_down, total));
          btn_down.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",activity.circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","L");
            intent.putExtra("vendor_id","9");
            intent.putExtra("project","9.2");
            activity.startActivity(intent);
          });
          tr.addView(btn_down);

          TextView tv = new TextView(activity);
          tv.setText(perc);
          tr.addView(tv);

          if(ssa_id.equalsIgnoreCase("TOTAL")) {
            for (int k = 0; k < tr.getChildCount(); k++) {
              View v = tr.getChildAt(k);
              if (v instanceof TextView) {
                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.maroon));
                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                ((TextView) v).setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
                ((TextView) v).setTextSize(15);
              }
            }
          } else if(!highlight) {
            for (int k = 0; k < tr.getChildCount(); k++) {
              View v = tr.getChildAt(k);
              if (v instanceof TextView) {
                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                ((TextView) v).setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
                ((TextView) v).setTextSize(15);
              }
            }
          } else {
            for (int k = 0; k < tr.getChildCount(); k++) {
              View v = tr.getChildAt(k);
              if (v instanceof TextView) {
                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.red));
                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                ((TextView) v).setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
                ((TextView) v).setTextSize(15);
              }
            }
          }
          activity.tl.addView(tr);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      //        View Properties
      for(int j =0; j<activity.tl.getChildCount(); j++){
        ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
        int maxHeight = 0;

//        Calculate the row wise lines
        for (int i = 0; i < tabrows.getChildCount(); i++) {
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

          if(i ==1 || i==2 || i==3) {
            params.width = 150;
          } else if(i==4) {
            params.width = 200;
          } else {
            params.width=180;
          }

          if (v instanceof TextView) {
            TextView textView = (TextView) v;
            String text = textView.getText().toString();
            Paint paint = textView.getPaint();
            float textWidth = paint.measureText(text);
            // Calculate number of lines and required height
            int numLines = (int) Math.ceil(textWidth / params.width);
            System.out.println("no of lines = "+ numLines);
            int calculatedHeight = (int) (textView.getLineHeight() * numLines) + 20; // Adding padding

            maxHeight = Math.max(maxHeight, calculatedHeight); // Update max height
          } else if (v instanceof Button) {
            maxHeight = Math.max(maxHeight, 80); // Default height for buttons or other elements
          } else {
            maxHeight = Math.max(maxHeight, (j == activity.tl.getChildCount() - 1) ? 120 : 80);
          }
        }



        for(int i=0; i<tabrows.getChildCount(); i++) {
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
          params.rightMargin = 1;
          params.bottomMargin = 1;
          if (i == 1 || i == 2 || i == 3) {
            params.width = 150;
          } else if (i == 4) {
            params.width = 200;
          } else {
            params.width = 180;
          }
          if (j == activity.tl.getChildCount() - 1) {
            params.height = 120;
          } else {
            params.height = 80;
          }

          params.height = maxHeight;

          if (v instanceof Button) {
            v.setPadding(0, 0, 0, 0);
          }

          v.setLayoutParams(params);
        }
      }
    }
  }
}