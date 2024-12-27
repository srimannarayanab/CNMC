package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CommerciallyLocked extends SessionActivity {
  private TableLayout tl;
  private SharedPreferences sharedPreferences;

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_commercially_locked);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }


    ImageButton toXlsx, shareBy;
    MyTask  myTask;

    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_circle_comm_locked));

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    tl = findViewById(R.id.tbl_lyt);
    myTask = new MyTask(this);
    myTask.execute(uri_builder.toString());

//        Swipe refresh
    final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh);
    swipeRefreshLayout.setOnRefreshListener(() -> {
      tl.removeAllViews();
      new MyTask(this).execute(uri_builder.toString());
      swipeRefreshLayout.setRefreshing(false);
    });

//        Create Xlsx
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));

    shareBy = findViewById(R.id.shareby);
    shareBy.setOnClickListener(v -> {
//                Toast.makeText(SsaWiseAvailability.this, "Share it is clicked",Toast.LENGTH_SHORT).show();
//                Bitmap bitmap = takeScreenshot();
      View rootView = getWindow().getDecorView().getRootView();
      rootView.setDrawingCacheEnabled(true);
      Bitmap bitmap = rootView.getDrawingCache();
      ScreeShare screeShare = new ScreeShare(getApplicationContext());
      screeShare.saveBitmap(bitmap, "Circle wise Commercially locked site "+Constants.getCurrentTime());
    });
  }

  private void buttonCreateExcel(String url) {
    try {
      String flts = new getLockedSitesFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("Locked");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("CircleID");
      row.createCell(1).setCellValue("Eric");
      row.createCell(2).setCellValue("Alca");
      row.createCell(3).setCellValue("Moto");
      row.createCell(4).setCellValue("Huaw");
      row.createCell(5).setCellValue("Nor");
      row.createCell(6).setCellValue("Nok");
      row.createCell(7).setCellValue("Zte");
      row.createCell(8).setCellValue("Total");

      JSONObject url_obj = new JSONObject(flts);
      if(!url_obj.getString("result").equals("true")){
        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SesssionLogout.class));
      }
      JSONArray arr =new JSONArray(url_obj.getString("data"));
      int eric=0, alc=0, moto=0, hua =0, nor=0, nok=0, zte=0, cnt=0;
      for(int i=0; i<arr.length(); i++){
        HSSFRow drow = sheet.createRow(i+1);
        JSONObject obj = arr.getJSONObject(i);
        drow.createCell(0).setCellValue(obj.getString("circle_id"));
        drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("eric_cnt")));
        drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("alc_cnt")));
        drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("moto_cnt")));
        drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("hua_cnt")));
        drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("nor_cnt")));
        drow.createCell(6).setCellValue(Integer.parseInt(obj.getString("nok_cnt")));
        drow.createCell(7).setCellValue(Integer.parseInt(obj.getString("zte_cnt")));
        drow.createCell(8).setCellValue(Integer.parseInt(obj.getString("cnt")));
        eric += Integer.parseInt(obj.getString("eric_cnt"));
        alc += Integer.parseInt(obj.getString("alc_cnt"));
        moto += Integer.parseInt(obj.getString("moto_cnt"));
        hua += Integer.parseInt(obj.getString("hua_cnt"));
        nor += Integer.parseInt(obj.getString("nor_cnt"));
        nok += Integer.parseInt(obj.getString("nok_cnt"));
        zte += Integer.parseInt(obj.getString("zte_cnt"));
        cnt += Integer.parseInt(obj.getString("cnt"));
      }
//            Writing the Total data
      HSSFRow trow = sheet.createRow(arr.length()+1);
      trow.createCell(0).setCellValue("Total");
      trow.createCell(1).setCellValue(eric);
      trow.createCell(2).setCellValue(alc);
      trow.createCell(3).setCellValue(moto);
      trow.createCell(4).setCellValue(hua);
      trow.createCell(5).setCellValue(nor);
      trow.createCell(6).setCellValue(nok);
      trow.createCell(7).setCellValue(zte);
      trow.createCell(8).setCellValue(cnt);


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


  private static class MyTask extends AsyncTask<String, String, String> {
    private final WeakReference<CommerciallyLocked> activityReference;
    ProgressDialog pd;

    private MyTask(CommerciallyLocked context) {
      activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching Report...");
      pd.setMessage("Processing.... ");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected String doInBackground(String... params) {
      CommerciallyLocked activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
//            System.out.println(s);
      CommerciallyLocked activity = activityReference.get();
      pd.dismiss();
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_circle);
      tr1.addView(tv1);
      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_ericsson);
      tr1.addView(tv2);
      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_alcatel);
      tr1.addView(tv3);
      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_motorola);
      tr1.addView(tv4);
      TextView tv5 = new TextView(activity);
      tv5.setText(R.string.header_huawei);
      tr1.addView(tv5);
      TextView tv6 = new TextView(activity);
      tv6.setText(R.string.header_nortel);
      tr1.addView(tv6);
      TextView tv7 = new TextView(activity);
      tv7.setText(R.string.header_nokia);
      tr1.addView(tv7);
      TextView tv8 = new TextView(activity);
      tv8.setText(R.string.header_zte);
      tr1.addView(tv8);
      TextView tv9 = new TextView(activity);
      tv9.setText(R.string.header_total_short);
      tr1.addView(tv9);

      activity.tl.addView(tr1);

      try {
        JSONObject url_obj = new JSONObject(s);
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray obj =new JSONArray(url_obj.getString("data"));
//                JSONArray obj = new JSONArray();
//                JSONArray obj = new JSONArray(s);
//                Log.i("Array", obj.toString());
        int tot_eric =0;
        int tot_alca =0;
        int tot_moto =0;
        int tot_hua =0;
        int tot_nor =0;
        int tot_nok =0;
        int tot_zte =0;
        int tot_tot =0;
        for(int i=0; i<obj.length(); i++){
          TableRow tr = new TableRow(activity);
          Button btn = new Button(activity);
          JSONObject c_obj = new JSONObject(obj.getString(i));
          final String circle = c_obj.getString("circle_id");
          String eric = c_obj.getString("eric_cnt");
          String alca = c_obj.getString("alc_cnt");
          String moto = c_obj.getString("moto_cnt");
          String hua = c_obj.getString("hua_cnt");
          String nor = c_obj.getString("nor_cnt");
          String nok = c_obj.getString("nok_cnt");
          String zte = c_obj.getString("zte_cnt");
          String tot = c_obj.getString("cnt");
          tot_eric +=Integer.parseInt(eric);
          tot_alca +=Integer.parseInt(alca);
          tot_moto +=Integer.parseInt(moto);
          tot_hua +=Integer.parseInt(hua);
          tot_nor +=Integer.parseInt(nor);
          tot_nok +=Integer.parseInt(nok);
          tot_zte +=Integer.parseInt(zte);
          tot_tot +=Integer.parseInt(tot);

          btn.setText(circle);
          tr.addView(btn);
          btn.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SsaLockedSites.class);
            intent.putExtra("circle_id", circle);
            activity.startActivity(intent);
          });

          Button btn_eric = new Button(activity);
          btn_eric.setText(eric);
          btn_eric.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","8");
            activity.startActivity(intent1);
          });
          tr.addView(btn_eric);

          Button btn_alca = new Button(activity);
          btn_alca.setText(alca);
          btn_alca.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","2");
            activity.startActivity(intent1);
          });
          tr.addView(btn_alca);

          Button btn_moto = new Button(activity);
          btn_moto.setText(moto);
          btn_moto.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","4");
            activity.startActivity(intent1);
          });
          tr.addView(btn_moto);

          Button btn_hua = new Button(activity);
          btn_hua.setText(hua);
          btn_hua.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","6");
            activity.startActivity(intent1);
          });
          tr.addView(btn_hua);

          Button btn_nor = new Button(activity);
          btn_nor.setText(nor);
          btn_nor.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","5");
            activity.startActivity(intent1);
          });
          tr.addView(btn_nor);

          Button btn_nok = new Button(activity);
          btn_nok.setText(nok);
          btn_nok.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","3");
            activity.startActivity(intent1);
          });
          tr.addView(btn_nok);

          Button btn_zte = new Button(activity);
          btn_zte.setText(zte);
          btn_zte.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","1");
            activity.startActivity(intent1);
          });
          tr.addView(btn_zte);

          Button btn_total = new Button(activity);
          btn_total.setText(tot);
          btn_total.setOnClickListener(v -> {
            Intent intent1 = new Intent(activity, LockedSitesDetails.class);
            intent1.putExtra("circle_id", circle);
            intent1.putExtra("ssa_id", "%");
            intent1.putExtra("vendor_id","%");
            activity.startActivity(intent1);
          });
          tr.addView(btn_total);
          activity.tl.addView(tr);
        }
        TableRow tr_f = new TableRow(activity);
        TextView tv_f_1 = new TextView(activity);
        tv_f_1.setText(R.string.header_total_short);
        tr_f.addView(tv_f_1);
        Button btn_f_2= new Button(activity);
        btn_f_2.setText(String.format(Locale.getDefault(), "%d", tot_eric));
        btn_f_2.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","8");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_2);

        Button btn_f_3 = new Button(activity);
        btn_f_3.setText(String.format(Locale.getDefault(), "%d",tot_alca));
        btn_f_3.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","2");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_3);

        Button btn_f_4 = new Button(activity);
        btn_f_4.setText(String.format(Locale.getDefault(), "%d",tot_moto));
        btn_f_4.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","4");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_4);

        Button btn_f_5 = new Button(activity);
        btn_f_5.setText(String.format(Locale.getDefault(), "%d",tot_hua));
        btn_f_5.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","6");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_5);

        Button btn_f_6 = new Button(activity);
        btn_f_6.setText(String.format(Locale.getDefault(), "%d", tot_nor));
        btn_f_6.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","5");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_6);

        Button btn_f_7 = new Button(activity);
        btn_f_7.setText(String.format(Locale.getDefault(), "%d", tot_nok));
        btn_f_7.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","3");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_7);

        Button btn_f_8 = new Button(activity);
        btn_f_8.setText(String.format(Locale.getDefault(), "%d", tot_zte));
        btn_f_8.setOnClickListener(v -> {
          Intent intent1 = new Intent(activity, LockedSitesDetails.class);
          intent1.putExtra("circle_id", "%");
          intent1.putExtra("ssa_id", "%");
          intent1.putExtra("vendor_id","1");
          activity.startActivity(intent1);
        });
        tr_f.addView(btn_f_8);

        TextView tv_f_9 = new TextView(activity);
        tv_f_9.setText(String.format(Locale.getDefault(), "%d" ,tot_tot));
        tr_f.addView(tv_f_9);

        activity.tl.addView(tr_f);

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
          params.height = 80;
          params.width = 100;
          if(v instanceof TextView){
            if(j>18 && j<activity.tl.getChildCount()-1){
              v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.red));
            } else if(j<=18){
              v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
            } else{
              v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.maroon));
            }
//                        ((TextView) v).setBackgroundColor(getResources().getColor(R.color.blue));
            ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            ((TextView) v).setGravity(Gravity.CENTER);
            ((TextView) v).setTextSize(15);
          }
          if(v instanceof Button){
            v.setPadding(0,0,0,0);
          }
        }
      }
    }
  }

  public static class getLockedSitesFaults extends AsyncTask<String, String, String>{
    private final WeakReference<CommerciallyLocked> activityReference;

    public getLockedSitesFaults(CommerciallyLocked context) {
      activityReference = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... strings) {
      CommerciallyLocked activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}
