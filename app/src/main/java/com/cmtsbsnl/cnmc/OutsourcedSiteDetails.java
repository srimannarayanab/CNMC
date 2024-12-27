package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class OutsourcedSiteDetails extends AppCompatActivity {
    private String circle_id;
    private String ssa_id;
    private TableLayout tl;
    private SharedPreferences sharedPreferences;
    private HashMap<String, String> operators;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outsourced_site_details);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Intent intent;
        TextView header;
        MyTask myTask;
        ImageButton toXlsx;

        intent = getIntent();
        circle_id = intent.getStringExtra("circle_id");
        ssa_id = intent.getStringExtra("ssa_id");
        String h_txt = ssa_id;
        if(ssa_id.equals("%")){
            h_txt ="";
        }

//        UriBuilder
        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_outsourced_down_details));

        header = findViewById(R.id.textView1);
        header.setText(getString(R.string.outsource_details, circle_id, h_txt));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));


        String optrnames = sharedPreferences.getString("optrs","hello");
//        System.out.println(optrnames);
        Gson gson = new Gson();
        Map map = gson.fromJson(optrnames, Map.class);
//        System.out.println(map);
        operators = new HashMap<>();
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

    }

    private void buttonCreateExcel(String url) {
        try {
            String flts = new getFaultsDetails(this).execute(url).get();
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

//            if(isExternalStorageWritable()) {
//                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/Faults.xls");
//                try {
//                    FileOutputStream fileOutputStream = new FileOutputStream(filepath);
//                    workbook.write(fileOutputStream);
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                    Toast.makeText(getApplicationContext(), "Excel file generated sucessfully in downloads folder", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else{
//                Toast.makeText(getApplicationContext(), "Sorry not permitted",Toast.LENGTH_SHORT).show();
//            }

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


        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean isExternalStorageWritable() {
        //            Log.i("ExternalStorage","Yes it is writeable");
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<OutsourcedSiteDetails> activityReference;

        ProgressDialog pd ;

        private MyTask(OutsourcedSiteDetails context) {
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
            OutsourcedSiteDetails activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                post_obj.put("circle_id", activity.circle_id);
                post_obj.put("ssa_id", activity.ssa_id);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            OutsourcedSiteDetails activity = activityReference.get();
            pd.dismiss();
//            Config utils = new Config();
//            System.out.println(s);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
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
//                System.out.println(s);
                if(arr.length()>0){
                    for(int i=0; i<arr.length(); i++){
                        final JSONObject obj = new JSONObject(arr.getString(i));
                        String str = Constants.getBtsDownInfo(obj, activity.operators);
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
        private final WeakReference<OutsourcedSiteDetails> activityReference1;

        private getFaultsDetails(OutsourcedSiteDetails context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            OutsourcedSiteDetails activity = activityReference1.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                post_obj.put("circle_id", activity.circle_id);
                post_obj.put("ssa_id", activity.ssa_id);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}