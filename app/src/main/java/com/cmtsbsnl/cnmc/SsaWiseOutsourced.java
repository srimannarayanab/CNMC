package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SsaWiseOutsourced extends AppCompatActivity {
  private TableLayout tl;
  private SharedPreferences sharedPreferences;
  private String circle_id;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ssa_wise_outsourced);

    MyTask myTask;
    ImageButton toXlsx;
    ImageButton shareBy;

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    Intent intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    TextView title_txt = findViewById(R.id.textView1);
    title_txt.setText(getString(R.string.header_circle_outsourced, circle_id));

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    //        Uri Builder
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_ssawise_outsourced_down));

//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    myTask = new MyTask(this);
    myTask.execute(uri_builder.toString());

//      Swipe to refresh
    final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
    pullToRefresh.setOnRefreshListener(() -> {
      tl.removeAllViews();
      new MyTask(this).execute(uri_builder.toString());
      pullToRefresh.setRefreshing(false);
    });

//        Generate Excel
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener((View v)->buttonCreateExcel(uri_builder.toString()));

//        Share By
    shareBy = findViewById(R.id.shareby);
    shareBy.setOnClickListener(v -> {
//                Toast.makeText(SsaWiseAvailability.this, "Share it is clicked",Toast.LENGTH_SHORT).show();
//                Bitmap bitmap = takeScreenshot();
      View rootView = getWindow().getDecorView().getRootView();
      rootView.setDrawingCacheEnabled(true);
      Bitmap bitmap = rootView.getDrawingCache();
      ScreeShare screeShare = new ScreeShare(getApplicationContext());
      screeShare.saveBitmap(bitmap, "Circle Category wise Sites down "+Constants.getCurrentTime());
    });
  }

  private void buttonCreateExcel(String url) {
    try {
      String flts = new getSSAOutsourcedWiseFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("SSAOutSourcedWise");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("SSAId");
      row.createCell(1).setCellValue("OutSrc-Down");
      row.createCell(2).setCellValue("OutSrc-Total");
      row.createCell(3).setCellValue("Bsnl-Down");
      row.createCell(4).setCellValue("Bsnl-Up");
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
        drow.createCell(0).setCellValue(obj.getString("ssaname"));
        drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("outsource_down")));
        drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("outsourced_sites")));
        drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("bsnl_down")));
        drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("bsnl_sites")));
        drow.createCell(5).setCellValue(Float.parseFloat(obj.getString("perc")));
      }

      if(isExternalStorageWritable()) {
        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/OutsourcedFaults.xls");
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

  private boolean isExternalStorageWritable(){
    //            Log.i("ExternalStorage","Yes it is writeable");
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  private static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<SsaWiseOutsourced> activityReference;
    private MyTask(SsaWiseOutsourced context) {
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
      SsaWiseOutsourced activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
        post_obj.put("circle_id", activity.circle_id);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0],"UTF-8"), post_obj.toString() );
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      SsaWiseOutsourced activity = activityReference.get();
      pd.dismiss();
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_ssa);
      tr1.addView(tv1);
      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_outsourced);
      tr1.addView(tv2);
      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_bsnl_nonip);
      tr1.addView(tv3);
      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_perc);
      tr1.addView(tv4);
      activity.tl.addView(tr1);
      try {
        JSONObject url_obj = new JSONObject(s);
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray obj =new JSONArray(url_obj.getString("data"));
        for(int i=0; i<obj.length(); i++){
          TableRow tr = new TableRow(activity);
          Button btn = new Button(activity);
          JSONObject cat_data = new JSONObject(obj.getString(i));
          final String ssa_name = cat_data.getString("ssaname");
          String outsource_down = cat_data.getString("outsource_down");
          String outsourced_sites = cat_data.getString("outsourced_sites");
          String bsnl_down = cat_data.getString("bsnl_down");
          String bsnl_sites = cat_data.getString("bsnl_sites");
          String perc = cat_data.getString("perc");
          btn.setText(ssa_name);
          tr.addView(btn);
          btn.setOnClickListener(v -> {
            Intent intent = new Intent(activity, OutsourcedSiteDetails.class);
            intent.putExtra("ssa_id", ssa_name);
            intent.putExtra("circle_id", activity.circle_id);
            activity.startActivity(intent);
          });
          Button outsrc = new Button(activity);
          outsrc.setText(activity.getString(R.string.down_wrt_total,outsource_down,outsourced_sites));
          outsrc.setOnClickListener(v -> {
            Intent intent = new Intent(activity, OutsourcedSiteDetails.class);
            intent.putExtra("ssa_id", ssa_name);
            intent.putExtra("circle_id",activity.circle_id);
            activity.startActivity(intent);
          });
          tr.addView(outsrc);
          Button bsnl =new Button(activity);
          bsnl.setText(activity.getString(R.string.down_wrt_total,bsnl_down,bsnl_sites));
          tr.addView(bsnl);
          TextView perc_txt = new TextView(activity);
          perc_txt.setText(perc);
          tr.addView(perc_txt);
          activity.tl.addView(tr);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      //        View Properties
      for(int j =0; j<activity.tl.getChildCount(); j++){
        ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
        for(int i=0; i<tabrows.getChildCount(); i++){
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
          params.rightMargin = 1;
          params.bottomMargin = 1;
          if(i ==1 || i==2 || i==3 ||i==4) {
            params.width = 200;
          } else {
            params.width=180;
          }
          params.height = 80;
          if(v instanceof TextView){
            ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
            v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.blue));
            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            ((TextView) v).setGravity(Gravity.CENTER);
            ((TextView) v).setTextSize(13);
          }
          if(v instanceof Button){
            v.setPadding(0,0,0,0);
          }
        }
      }
    }
  }

  public static class getSSAOutsourcedWiseFaults extends AsyncTask<String, String, String>{
    private final WeakReference<SsaWiseOutsourced> activityReference1;

    public getSSAOutsourcedWiseFaults(SsaWiseOutsourced context) {
      activityReference1 = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... strings) {
      SsaWiseOutsourced activity = activityReference1.get();
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
}