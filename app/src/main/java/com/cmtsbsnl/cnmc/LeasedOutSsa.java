package com.cmtsbsnl.cnmc;

import android.Manifest;
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

public class LeasedOutSsa extends SessionActivity {
    private TableLayout tl;
    private String circle_id;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leased_out_ssa);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        TextView header;
        MyTask myTask;
        ImageButton toXlsx, shareBy;

        Intent intent = getIntent();
        circle_id = intent.getStringExtra("circle_id");
        header = findViewById(R.id.textView1);
        header.setText(getString(R.string.header_leasedout,circle_id));

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
//        Uri Builder
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_ssawise_leased));

//        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());

//          Swipe to refresh
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            tl.removeAllViews();
            new MyTask(this).execute(uri_builder.toString());
            swipeRefreshLayout.setRefreshing(false);
        });

//        To Xlsx
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
            screeShare.saveBitmap(bitmap, "ssawise leased out btsdown count "+Constants.getCurrentTime());
        });
    }

    private void buttonCreateExcel(String url) {
        try {
            String flts = new getLeasedOutSsaWise(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("LeasedOutSSA");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("SsaID");
            row.createCell(1).setCellValue("BSNL");
            row.createCell(2).setCellValue("Non-BSNL");
            row.createCell(3).setCellValue("USO");
            row.createCell(4).setCellValue("Unidentified");
            row.createCell(5).setCellValue("Total");
            row.createCell(6).setCellValue("Count");
            row.createCell(7).setCellValue("Perc");


            JSONObject url_obj = new JSONObject(flts);
            if(!url_obj.getString("result").equals("true")){
                Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SesssionLogout.class));
            }
            JSONArray arr =new JSONArray(url_obj.getString("data"));
            for(int i=0; i<arr.length(); i++){
                HSSFRow drow = sheet.createRow(i+1);
                JSONObject obj = arr.getJSONObject(i);
                System.out.println(obj.toString());
                drow.createCell(0).setCellValue(obj.getString("ssaid"));
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("BS")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("NB")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("US")));
                drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("UI")));
                drow.createCell(5).setCellValue(Integer.parseInt(obj.getString("TOT")));
                drow.createCell(6).setCellValue(Integer.parseInt(obj.getString("CNT")));
                drow.createCell(7).setCellValue(Double.parseDouble(obj.getString("perc")));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/LeasedOutSSA.xls");
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
        private final WeakReference<LeasedOutSsa> activityReference;
        ProgressDialog pd;
        String per;

        private MyTask(LeasedOutSsa context) {
            activityReference = new WeakReference<>(context);
        }

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
            LeasedOutSsa activity = activityReference.get();
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
            LeasedOutSsa activity = activityReference.get();
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_bsnl);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_nonbsnl);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_uso);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_unidentified);
            tr1.addView(tv5);
            TextView tv6 = new TextView(activity);
            tv6.setText(R.string.header_total_short);
            tr1.addView(tv6);
            TextView tv7 = new TextView(activity);
            tv7.setText(R.string.header_total_short);
            tr1.addView(tv7);
            TextView tv8 = new TextView(activity);
            tv8.setText(R.string.header_perc);
            tr1.addView(tv8);
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
                JSONArray obj =new JSONArray(url_obj.getString("data"));
//                Log.i("Array", obj.toString());
                for(int i=0; i<obj.length(); i++){
                    TableRow tr = new TableRow(activity);
                    Button btn = new Button(activity);
                    JSONObject cat_data = new JSONObject(obj.getString(i));
                    final String ssa_id = cat_data.getString("ssaid");
                    String bs = cat_data.getString("BS");
                    String nb = cat_data.getString("NB");
                    String us = cat_data.getString("US");
                    String ui = cat_data.getString("UI");
                    String total = cat_data.getString("TOT");
                    String cnt = cat_data.getString("CNT");
                    double perc = cat_data.getDouble("perc");
                    float percentage;
                    try {
                        percentage = ((float) Integer.parseInt(total) * 100 / Integer.parseInt(cnt));
                        if(Double.valueOf(percentage).isNaN()){
                            per = "NA";
                        } else {
                            per = String.format(Locale.getDefault(),"%2.02f", percentage);
                        }
                    } catch (ArithmeticException | NumberFormatException ae){
                        percentage = 0.0f;
                        per = String.format(Locale.getDefault(),"%2.02f", percentage);
                    }
                    btn.setText(ssa_id);
                    tr.addView(btn);

                    Button  bs_txt = new Button(activity);
                    bs_txt.setText(bs);
                    bs_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, LeasedOutDetails.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","BS");
                        activity.startActivity(intent);
                    });
                    tr.addView(bs_txt);
                    Button nb_txt =new Button(activity);
                    nb_txt.setText(nb);
                    nb_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, LeasedOutDetails.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","NB");
                        activity.startActivity(intent);
                    });
                    tr.addView(nb_txt);
                    Button us_txt = new Button(activity);
                    us_txt.setText(us);
                    us_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, LeasedOutDetails.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","US");
                        activity.startActivity(intent);
                    });
                    tr.addView(us_txt);
                    Button ui_txt = new Button(activity);
                    ui_txt.setText(ui);
                    ui_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, LeasedOutDetails.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","UI");
                        activity.startActivity(intent);
                    });
                    tr.addView(ui_txt);
                    Button total_txt = new Button(activity);
                    total_txt.setText(total);
                    total_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, LeasedOutDetails.class);
                        intent.putExtra("circle_id",activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","%");
                        activity.startActivity(intent);
                    });
                    tr.addView(total_txt);
                    TextView cnt_txt = new TextView(activity);
                    cnt_txt.setText(cnt);
                    tr.addView(cnt_txt);
                    TextView perc_txt = new TextView(activity);
                    perc_txt.setText(per);
                    tr.addView(perc_txt);
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
                    if(i ==0) {
                        params.width = 150;
                    } else {
                        params.width=120;
                    }
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

    public static class getLeasedOutSsaWise extends AsyncTask<String, String, String>{
        private final WeakReference<LeasedOutSsa> activityReference1;
        private getLeasedOutSsaWise(LeasedOutSsa context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            LeasedOutSsa activity = activityReference1.get();
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
