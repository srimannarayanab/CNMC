package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
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

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CategoryWiseSsa extends SessionActivity {
  private TableLayout tl;
  public String circle_id;
  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_category_wise_ssa);

    TextView header;
    MyTask myTask;
    ImageButton toXlsx, shareBy;

    Intent intent = getIntent();
    circle_id = intent.getStringExtra("circle_id");
    header = findViewById(R.id.textView1);
    header.setText(getString(R.string.header_categorywise,circle_id));

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener((View v)->onBackPressed());
    }

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener((View v)->
        startActivity(new Intent(this, Navigational.class))
    );
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
//        Building URI
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_ssawise_category_down));


//        Presenting the data
    tl = findViewById(R.id.tbl_lyt);
    myTask = new MyTask(this);
    myTask.execute(uri_builder.toString());

    //      Swipe to refresh
    final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
    pullToRefresh.setOnRefreshListener(()-> {
      tl.removeAllViews();
      new MyTask(this).execute(uri_builder.toString());
      pullToRefresh.setRefreshing(false);
    });

    //        Generate Excel
    toXlsx = findViewById(R.id.toXlsx);
    toXlsx.setOnClickListener((View v)-> buttonCreateExcel(uri_builder.toString()));

    shareBy = findViewById(R.id.shareby);
    shareBy.setOnClickListener((View v)-> {
      View rootView = getWindow().getDecorView().getRootView();
      rootView.setDrawingCacheEnabled(true);
      Bitmap bitmap = rootView.getDrawingCache();
      ScreeShare screeShare = new ScreeShare(getApplicationContext());
      screeShare.saveBitmap(bitmap, "SSA Category wise Sites down "+Constants.getCurrentTime());
    });
  }

  private void buttonCreateExcel(String url) {
    try {
      String flts = new getCategoryWiseSSAFaults(this).execute(url).get();
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("Categorywise");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("SSAID");
      row.createCell(1).setCellValue("SC");
      row.createCell(2).setCellValue("Cri");
      row.createCell(3).setCellValue("Imp");
      row.createCell(4).setCellValue("Nor");
      row.createCell(5).setCellValue("Total");

      JSONObject url_obj = new JSONObject(flts);
      if(!url_obj.getString("result").equals("true")){
        Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SesssionLogout.class));
      }
      JSONArray arr =new JSONArray(url_obj.getString("data"));
      for(int i=0; i<arr.length(); i++){
        HSSFRow drow = sheet.createRow(i+1);
        JSONObject obj = arr.getJSONObject(i);
        drow.createCell(0).setCellValue(obj.getString("ssaid"));
        drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("sc")));
        drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("cri")));
        drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("imp")));
        drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("nor")));
        drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("Total")));
      }

      if(isExternalStorageWritable()) {
        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/CategorySsawiseFaults.xls");
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


    } catch (ExecutionException | InterruptedException |JSONException e) {
      e.printStackTrace();
    }
  }

  private boolean isExternalStorageWritable() {
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  private static class MyTask extends AsyncTask<String, String, String> {

    private final WeakReference<CategoryWiseSsa> activityReference;
    MyTask(CategoryWiseSsa context){
      activityReference = new WeakReference<>(context);
    }

    ProgressDialog pd;
    @Override
    protected void onPreExecute() {
      pd = new ProgressDialog(activityReference.get());
      pd.setTitle("Fetching Alarms ...");
      pd.setMessage("Processing...");
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setCancelable(false);
      pd.show();
    }

    @Override
    protected String doInBackground(String... params) {
      CategoryWiseSsa activity = activityReference.get();
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
      CategoryWiseSsa activity = activityReference.get();
      pd.dismiss();
      if (activity == null || activity.isFinishing()) return;
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_ssa);
      tr1.addView(tv1);
      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_supercritical);
      tr1.addView(tv2);
      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_critical);
      tr1.addView(tv3);
      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_important);
      tr1.addView(tv4);
      TextView tv5 = new TextView(activity);
      tv5.setText(R.string.header_normal);
      tr1.addView(tv5);
      TextView tv6 = new TextView(activity);
      tv6.setText(R.string.header_total_short);
      tr1.addView(tv6);
      activity.tl.addView(tr1);
      try {
        JSONObject url_obj = new JSONObject(s);
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray obj = new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
        for(int i=0; i<obj.length(); i++){
          TableRow tr = new TableRow(activity);
          Button btn = new Button(activity);
          JSONObject cat_data = new JSONObject(obj.getString(i));
          final String ssaid = cat_data.getString("ssaid");
          String sc = cat_data.getString("sc");
          String c = cat_data.getString("cri");
          String imp = cat_data.getString("imp");
          String nor = cat_data.getString("nor");
          String total = cat_data.getString("Total");
          String sc_cnt = cat_data.getString("sc_cnt");
          String c_cnt = cat_data.getString("c_cnt");
          btn.setText(ssaid);
          tr.addView(btn);
          Button sc_txt = new Button(activity);
          sc_txt.setText(activity.getString(R.string.down_wrt_total, sc, sc_cnt));
          sc_txt.setOnClickListener((View v)-> {
            Intent intent = new Intent(activity, CategoryWiseDetails.class);
            intent.putExtra("circle", activity.circle_id);
            intent.putExtra("ssaname", ssaid);
            intent.putExtra("criteria","SUPER_CRITICAL");
            activity.startActivity(intent);
          });
          tr.addView(sc_txt);
          Button c_txt =new Button(activity);
//                    c_txt.setText(c+"/"+c_cnt);
          c_txt.setText(activity.getString(R.string.down_wrt_total, c, c_cnt));
          c_txt.setOnClickListener((View v)-> {
            Intent intent = new Intent(activity, CategoryWiseDetails.class);
            intent.putExtra("circle", activity.circle_id);
            intent.putExtra("ssaname", ssaid);
            intent.putExtra("criteria","CRITICAL");
            activity.startActivity(intent);
          });
          tr.addView(c_txt);
          Button imp_txt = new Button(activity);
          imp_txt.setText(imp);
          imp_txt.setOnClickListener((View v)-> {
            Intent intent = new Intent(activity, CategoryWiseDetails.class);
            intent.putExtra("circle", activity.circle_id);
            intent.putExtra("ssaname", ssaid);
            intent.putExtra("criteria","IMPORTANT");
            activity.startActivity(intent);
          });
          tr.addView(imp_txt);
          Button nor_txt = new Button(activity);
          nor_txt.setText(nor);
          nor_txt.setOnClickListener((View v)-> {
            Intent intent = new Intent(activity, CategoryWiseDetails.class);
            intent.putExtra("circle", activity.circle_id);
            intent.putExtra("ssaname", ssaid);
            intent.putExtra("criteria","NORMAL");
            activity.startActivity(intent);
          });
          tr.addView(nor_txt);
          Button tot_txt = new Button(activity);
          tot_txt.setText(total);
          tot_txt.setOnClickListener((View v)-> {
            Intent intent = new Intent(activity, CategoryWiseDetails.class);
            intent.putExtra("circle", activity.circle_id);
            intent.putExtra("ssaname", ssaid);
            intent.putExtra("criteria","%");
            activity.startActivity(intent);
          });
          tr.addView(tot_txt);
          activity.tl.addView(tr);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      //        View Properties
      for(int j =0; j<activity.tl.getChildCount(); j++){
        ViewGroup tabrows = (ViewGroup) activity.tl.getChildAt(j);
        int maxHeight = 0;
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int width = screenWidth/6;

//        Measure the height of the row
        for (int i = 0; i < tabrows.getChildCount(); i++) {
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

          TextView textView = (TextView) v;
          String text = textView.getText().toString();
          Paint paint = textView.getPaint();
          float textWidth = paint.measureText(text);

          // Calculate number of lines and required height
          int numLines = (int) Math.ceil((textWidth + 20) / width);
          int calculatedHeight = (int) (textView.getLineHeight() * numLines) + 20; // Adding padding
          maxHeight = Math.max(maxHeight, calculatedHeight); // Update max height
          System.out.println("String ="+ text + "Screen Width="+ width+" Width="+ textWidth+" no of line ="+numLines+" height "+ maxHeight);
        }

        for(int i=0; i<tabrows.getChildCount(); i++){
          View v = tabrows.getChildAt(i);
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
          params.rightMargin = 1;
          params.bottomMargin = 1;
          params.width= width;
          params.height = maxHeight;
          if(v instanceof TextView){
            ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
            v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            ((TextView) v).setGravity(Gravity.CENTER_HORIZONTAL );
            ((TextView) v).setTextSize(15);
          }
          if(v instanceof Button){
            v.setPadding(0,0,0,0);
          }
          v.setLayoutParams(params);
        }
      }
    }
  }

  public static class getCategoryWiseSSAFaults extends AsyncTask<String, String, String> {
    private final WeakReference<CategoryWiseSsa> activityReference1;

    public getCategoryWiseSSAFaults(CategoryWiseSsa context) {
      activityReference1 = new WeakReference<>(context);
    }


    @Override
    protected String doInBackground(String... strings) {
      CategoryWiseSsa activity = activityReference1.get();
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
