package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class TechWiseSsa extends SessionActivity {
  private TableLayout tl;
  private String circle_id;
  private SharedPreferences sharedPreferences;


  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tech_wise_ssa);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener(v -> {
        onBackPressed();
      });
    }

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    MyTask myTask;
    ImageButton toXlsx, shareBy, tcs_4g, tcs4g_sa, legacy_nw;

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      Log.e("PreferencesError", "Failed to get encrypted shared preferences", e);
    }

    Intent intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");
    TextView hometextview = findViewById(R.id.textView1);
    hometextview.setText(getString(R.string.header_tech,circle_id));

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

//        Build the URL to consume API
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_ssawise_tech_down));

//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    myTask =new MyTask(this);
    myTask.execute(uri_builder.toString());

//        Swipe to refresh

    final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
    pullToRefresh.setOnRefreshListener(() -> {
      tl.removeAllViews();
      new MyTask(this).execute(uri_builder.toString());
      pullToRefresh.setRefreshing(false);
    });

    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));

//    shareBy = findViewById(R.id.shareby);
//    shareBy.setOnClickListener(v -> {
//      View rootView = getWindow().getDecorView().getRootView();
//      rootView.setDrawingCacheEnabled(true);
//      Bitmap bitmap = rootView.getDrawingCache();
//      ScreeShare screeShare = new ScreeShare(getApplicationContext());
//      screeShare.saveBitmap(bitmap, "SSA wise Tech wise btsdown count "+Constants.getCurrentTime());
//    });
    shareBy = findViewById(R.id.shareby);
    shareBy.setOnClickListener(v -> {
      View rootView = getWindow().getDecorView().getRootView();

      // Create a bitmap with the same dimensions as the view
      Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);

      // Use Canvas to draw the view into the bitmap
      Canvas canvas = new Canvas(bitmap);
      rootView.draw(canvas);

      // Save the bitmap using ScreeShare
      ScreeShare screeShare = new ScreeShare(getApplicationContext());
      requestStoragePermission();
      screeShare.saveBitmap(bitmap, "SSA wise Tech wise BTS Down Count " + Constants.getCurrentTime());

      // Optionally, recycle bitmap if no longer needed
      bitmap.recycle();
    });

//    TCS 4G Activity
    tcs_4g = findViewById(R.id.tcs4g);
    tcs_4g.setOnClickListener( v -> {
      Intent intent_tcs_4g = new Intent(this, Tcs_4g.class);
      intent_tcs_4g.putExtra("circle_id",circle_id);
      startActivity(intent_tcs_4g);
    });

//    Saturation Project Activity
    tcs4g_sa = findViewById(R.id.tcs4g_sa);
    tcs4g_sa.setOnClickListener(v->{
      Intent intent_tcs4g_sa = new Intent(this, Tcs4g_sa.class);
      intent_tcs4g_sa.putExtra("circle_id",circle_id);
      startActivity(intent_tcs4g_sa);
    });

//    Legacy N/W
    legacy_nw = findViewById(R.id.legacy_nw);
    legacy_nw.setOnClickListener(View ->{
      Intent intent_lgnw = new Intent(this, LegacyNwSsa.class);
      intent_lgnw.putExtra("circle_id", circle_id);
      startActivity(intent_lgnw);
    });


  }

  private boolean isExternalStorageWritable(){
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  private void buttonCreateExcel(String url) {
    try {
      String flts = new getTechWiseSSAFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("TechWise");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("SSAID");
      row.createCell(1).setCellValue("2G");
      row.createCell(2).setCellValue("3G");
      row.createCell(3).setCellValue("4G");
      row.createCell(4).setCellValue("Total");
      row.createCell(5).setCellValue("Perc");

      JSONObject url_obj = new JSONObject(flts);
      if(!url_obj.getString("result").equals("true")){
        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SesssionLogout.class));
      }
      JSONArray arr =new JSONArray(url_obj.getString("data"));
      for(int i=0; i<arr.length(); i++){
        HSSFRow drow = sheet.createRow(i+1);
        JSONObject obj = arr.getJSONObject(i);
        drow.createCell(0).setCellValue(obj.getString("ssa_id"));
        drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("bts_2g_down_cnt")));
        drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("bts_3g_down_cnt")));
        drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("bts_4g_down_cnt")));
        drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("down_cnt")));
        drow.createCell(5).setCellValue(Double.parseDouble(obj.getString("perc")));
      }

      if(isExternalStorageWritable()) {
        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/TechwiseSSAFaults.xls");
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


    } catch (JSONException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }


  }

  private static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<TechWiseSsa> activityReference;
    private MyTask(TechWiseSsa context) {
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
      TechWiseSsa activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle_id", activity.circle_id);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      TechWiseSsa activity = activityReference.get();
      pd.dismiss();
//            System.out.println(s);
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_ssa);
      tr1.addView(tv1);
      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_2g);
      tr1.addView(tv2);
      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_3g);
      tr1.addView(tv3);
      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_4g);
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
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
          activity.finish();
        }
        JSONArray obj = new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
        for(int i=0; i<obj.length(); i++){
          TableRow tr = new TableRow(activity);
          Button btn = new Button(activity);
          JSONObject obj1 = new JSONObject(obj.getString(i));
          final String ssa_id = obj1.getString("ssa_id");
          final String circle_id = obj1.getString("circle_id");
          String bts_2g_cnt = obj1.getString("bts_2g_cnt");
          String bts_3g_cnt = obj1.getString("bts_3g_cnt");
          String bts_4g_cnt = obj1.getString("bts_4g_cnt");
          String bts_2g_down_cnt = obj1.getString("bts_2g_down_cnt");
          String bts_3g_down_cnt = obj1.getString("bts_3g_down_cnt");
          String bts_4g_down_cnt = obj1.getString("bts_4g_down_cnt");
          String down_cnt = obj1.getString("down_cnt");
          String total = obj1.getString("total_cnt");
          String perc = obj1.getString("perc");
          boolean highlight = false;
          try{
            double perc_faults = obj1.getDouble("perc");
            if(perc_faults>10){
              highlight = true;
            }
          } catch (Exception e){
            e.printStackTrace();
          }

          btn.setText(ssa_id);
          tr.addView(btn);
          btn.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id", circle_id);
            intent.putExtra("ssa_id", ssa_id);
            intent.putExtra("bts_type","%");
            activity.startActivity(intent);
          });

          Button btn_2g = new Button(activity);
          if(ssa_id.equals("Total")){
            btn_2g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_2g_down_cnt,  bts_2g_cnt));
          } else {
            btn_2g.setText(activity.getString(R.string.down_wrt_total, bts_2g_down_cnt, bts_2g_cnt));
          }
//                    btn_2g.setText(activity.getString(R.string.down_wrt_total,bts_2g_down_cnt,bts_2g_cnt));
          btn_2g.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","G");
            activity.startActivity(intent);
          });
          tr.addView(btn_2g);


          Button btn_3g = new Button(activity);
          if(ssa_id.equals("Total")){
            btn_3g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_3g_down_cnt,  bts_3g_cnt));
          } else {
            btn_3g.setText(activity.getString(R.string.down_wrt_total, bts_3g_down_cnt, bts_3g_cnt));
          }
//                    btn_3g.setText(activity.getString(R.string.down_wrt_total,bts_3g_down_cnt,bts_3g_cnt));
          btn_3g.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","U");
            activity.startActivity(intent);
          });
          tr.addView(btn_3g);

          Button btn_4g = new Button(activity);
          if(ssa_id.equals("Total")){
            btn_4g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_4g_down_cnt,  bts_4g_cnt));
          } else {
            btn_4g.setText(activity.getString(R.string.down_wrt_total, bts_4g_down_cnt, bts_4g_cnt));
          }
//                    btn_4g.setText(activity.getString(R.string.down_wrt_total,bts_4g_down_cnt,bts_4g_cnt));
          btn_4g.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","L");
            activity.startActivity(intent);
          });
          tr.addView(btn_4g);

          Button btn_down = new Button(activity);
          if(ssa_id.equals("Total")){
            btn_down.setText(activity.getString(R.string.down_wrt_total_for_total, down_cnt,  total));
          } else {
            btn_down.setText(activity.getString(R.string.down_wrt_total, down_cnt, total));
          }
//                    btn_down.setText(activity.getString(R.string.down_wrt_total,down_cnt,total));
          btn_down.setOnClickListener(v -> {
            Intent intent = new Intent(activity, TechWiseDetails.class);
            intent.putExtra("circle_id",circle_id);
            intent.putExtra("ssa_id",ssa_id);
            intent.putExtra("bts_type","%");
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
//            System.out.println("no of lines = "+ numLines);
            int calculatedHeight = (int) (textView.getLineHeight() * numLines) + 20; // Adding padding

            maxHeight = Math.max(maxHeight, calculatedHeight); // Update max height
          } else if (v instanceof Button) {
            maxHeight = Math.max(maxHeight, 80); // Default height for buttons or other elements
          } else {
            maxHeight = Math.max(maxHeight, (j == activity.tl.getChildCount() - 1) ? 120 : 80);
          }
        }



        for(int i=0; i<tabrows.getChildCount(); i++){
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
          params.rightMargin = 1;
          params.bottomMargin = 1;
          if(i ==1 || i==2 || i==3) {
            params.width = 150;
          } else if(i==4) {
            params.width = 200;
          } else {
            params.width=180;
          }
          if(j==activity.tl.getChildCount()-1) {
            params.height = 120;
          } else {
            params.height =80;
          }

//          System.out.println("Max height" + maxHeight);
          params.height= maxHeight;

          if(v instanceof Button){
            v.setPadding(0,0,0,0);
          }

          v.setLayoutParams(params);
        }

//                Last row Format
                /*if(j==tl.getChildCount()-1) {
                    ViewGroup tabrow = (ViewGroup) tl.getChildAt(j);
                    for (int k = 0; k < tabrow.getChildCount(); k++) {
                        View v = tabrow.getChildAt(k);
                        if (v instanceof TextView) {
                            ((TextView) v).setTextColor(getResources().getColor(R.color.white));
                            ((TextView) v).setBackgroundColor(getResources().getColor(R.color.maroon));
                            ((TextView) v).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                            ((TextView) v).setGravity(Gravity.CENTER);
                            ((TextView) v).setTextSize(14);
                        }
                    }
                }*/
      }
    }
  }

  private static class getTechWiseSSAFaults extends AsyncTask<String, String, String>{
    private final WeakReference<TechWiseSsa> activityReference1;

    private getTechWiseSSAFaults(TechWiseSsa context) {
      activityReference1 = new WeakReference<>(context);
    }


    @Override
    protected String doInBackground(String... strings) {
      TechWiseSsa activity = activityReference1.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
        post_obj.put("circle_id", activity.circle_id);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString() );
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  }

//  Permission
private void requestStoragePermission() {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Only needed for Android 9 and below
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }
  }
}

  // Handle permission result
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Storage Permission Granted!", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(this, "Storage Permission Denied!", Toast.LENGTH_SHORT).show();
      }
    }
  }

}