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
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DurationWiseSsa extends SessionActivity {
    private TableLayout tl;
    private String circle_id;
    private SharedPreferences sharedPreferences;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration_wise_ssa);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView header;
        MyTask myTask;
        ImageButton toXlsx, shareBy;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        circle_id = intent.getStringExtra("circle_id");
        header = findViewById(R.id.textView1);
        header.setText(getString(R.string.header_durationwise,circle_id));

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
//        Uri Buider
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_ssawise_duration_down));

//        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());
//        Swipe to Refresh
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            tl.removeAllViews();
            new MyTask(this).execute(uri_builder.toString());
            swipeRefreshLayout.setRefreshing(false);
        });
//        Generate Xlsx file
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
            screeShare.saveBitmap(bitmap, "SSA wise duration wise btsdown count "+Constants.getCurrentTime());
        });
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    private void buttonCreateExcel(String url){
        try {
            String flts = new getDurationWiseSSAFaults(this).execute(url).get();
            System.out.println(flts);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("DurationWise");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("SSAID");
            row.createCell(1).setCellValue("<1D");
            row.createCell(2).setCellValue("1-2D");
            row.createCell(3).setCellValue("2-3D");
            row.createCell(4).setCellValue("3-7D");
            row.createCell(5).setCellValue(">7D");
            row.createCell(6).setCellValue("Total");

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
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("lt_24")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("d_1")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("d_2")));
                drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("d_3")));
                drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("d_7")));
                drow.createCell(6).setCellValue(Integer.parseInt(obj.getString("Total")));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/DurationWise_ssawise_Faults.xls");
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
        private final WeakReference<DurationWiseSsa> acivityReference;

        ProgressDialog pd ;

        private MyTask(DurationWiseSsa context) {
            acivityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(acivityReference.get());
            pd.setTitle("Fetching Alarms...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }
        @Override
        protected String doInBackground(String... params) {
            DurationWiseSsa activity= acivityReference.get();
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
            DurationWiseSsa activity = acivityReference.get();
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_lt_1day);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_1_2_day);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_2_3_day);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_3_7_day);
            tr1.addView(tv5);
            TextView tv6 = new TextView(activity);
            tv6.setText(R.string.header_gt_7_day);
            tr1.addView(tv6);
            TextView tv7 = new TextView(activity);
            tv7.setText(R.string.header_total_short);
            tr1.addView(tv7);
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
                }
                JSONArray obj = new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
                for(int i=0; i<obj.length(); i++){
                    TableRow tr = new TableRow(activity);
                    Button btn = new Button(activity);
                    JSONObject cat_data = new JSONObject(obj.getString(i));
                    final String ssa_id = cat_data.getString("ssaid");
                    String ls_24 = cat_data.getString("lt_24");
                    String d_1 = cat_data.getString("d_1");
                    String d_2 = cat_data.getString("d_2");
                    String d_3 = cat_data.getString("d_3");
                    String d_7 = cat_data.getString("d_7");
                    String total = cat_data.getString("Total");
                    double perc = cat_data.getDouble("perc");

                    btn.setText(ssa_id);
                    tr.addView(btn);

                    Button btn1  = new Button(activity);
                    btn1.setText(ls_24);
                    btn1.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","1");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn1);

                    Button btn2 =new Button(activity);
                    btn2.setText(d_1);
                    btn2.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","2");
                        activity.startActivity(intent);

                    });
                    tr.addView(btn2);

                    Button btn3 = new Button(activity);
                    btn3.setText(d_2);
                    btn3.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","3");
                        activity.startActivity(intent);

                    });
                    tr.addView(btn3);

                    Button btn4 = new Button(activity);
                    btn4.setText(d_3);
                    btn4.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","4");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn4);

                    Button btn5 = new Button(activity);
                    btn5.setText(d_7);
                    btn5.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","5");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn5);

                    Button btn6 = new Button(activity);
                    btn6.setText(total);
                    btn6.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DurationWise_details.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","6");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn6);
                    if(perc<10) {
                        for (int k = 0; k < tr.getChildCount(); k++) {
                            View v = tr.getChildAt(k);
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                ((TextView) v).setGravity(Gravity.CENTER);
                                ((TextView) v).setTextSize(15);
                            }
                        }
                    } else {
                        for (int k = 0; k < tr.getChildCount(); k++) {
                            View v = tr.getChildAt(k);
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.red));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                ((TextView) v).setGravity(Gravity.CENTER);
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
                for(int i=0; i<tabrows.getChildCount(); i++){
                    View v = tabrows.getChildAt(i);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.rightMargin = 1;
                    params.bottomMargin = 1;
                    params.width = 150;
                    params.height = 80;
                    if(v instanceof Button){
                        v.setPadding(0,0,0,0);
                    }
                }
                //                Last row Format
                if(j==activity.tl.getChildCount()-1) {
                    ViewGroup tabrow = (ViewGroup) activity.tl.getChildAt(j);
                    for (int k = 0; k < tabrow.getChildCount(); k++) {
                        View v = tabrow.getChildAt(k);
                        if (v instanceof TextView) {
                            ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                            v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.maroon));
                            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                            ((TextView) v).setGravity(Gravity.CENTER);
                            ((TextView) v).setTextSize(15);
                        }
                    }
                }
            }
        }
    }

    public static class getDurationWiseSSAFaults extends AsyncTask<String, String, String>{
        private final WeakReference<DurationWiseSsa> activityReference1;

        public getDurationWiseSSAFaults(DurationWiseSsa context) {
            activityReference1 = new WeakReference<>(context);
        }
        @Override
        protected String doInBackground(String... strings) {
            DurationWiseSsa activity = activityReference1.get();

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
