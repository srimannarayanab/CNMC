package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Mybts_Leftout extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String circle_id;
    private TableLayout tl;
    private HashMap<String, String> operators;

    public Mybts_Leftout() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mybts_leftout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        String optrnames = null;
        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
            optrnames = sharedPreferences.getString("optrs","hello");
            circle_id = sharedPreferences.getString("circle_id","");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

//        pref = getApplicationContext().getSharedPreferences("CnmcPref",MODE_PRIVATE);
//        circle_id = pref.getString("circle_id","");
//        strUrl = "http://"+Constants.SERVER_IP+"/cnmc/getLeftOutMyBts.php";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)-> onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(Mybts_Leftout.this, Navigational.class)));

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(optrnames, Map.class);
        operators = new HashMap<>();
        for(String k: map.keySet()){
            operators.put(k, map.get(k));
        }

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_get_leftout_bts));


        tl = findViewById(R.id.tbl_lyt);
        MyTask mytask = new MyTask(this);
        mytask.execute(uri_builder.toString());

//        Generate Xlsx
        //    private SimpleDateFormat sfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ImageButton toXlsx = findViewById(R.id.toXlsx);
        toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    private void buttonCreateExcel(String url){
        try {
            String flts = new getMyBtsLeftOut(this).execute(url).get();
            JSONObject flts_obj = new JSONObject(flts);
//            System.out.println(flts);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("DurationWise");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("bts_id");
            row.createCell(1).setCellValue("ssa_name");
            row.createCell(2).setCellValue("bts_name");
            row.createCell(3).setCellValue("site_category");
            row.createCell(4).setCellValue("operator_id");
            row.createCell(5).setCellValue("vendor_name");
            row.createCell(6).setCellValue("circle_name");
            row.createCell(7).setCellValue("bts_type");
            row.createCell(6).setCellValue("site_type");

            JSONArray arr = new JSONArray(flts_obj.getString("data"));
            for(int i=0; i<arr.length(); i++){
                HSSFRow drow = sheet.createRow(i+1);
                JSONObject obj = arr.getJSONObject(i);
                drow.createCell(0).setCellValue(obj.getString("bts_id"));
                drow.createCell(1).setCellValue(obj.getString("ssa_name"));
                drow.createCell(2).setCellValue(obj.getString("bts_name"));
                drow.createCell(3).setCellValue(obj.getString("site_category"));
                drow.createCell(4).setCellValue(obj.getString("operator_id"));
                drow.createCell(5).setCellValue(obj.getString("vendor_name"));
                drow.createCell(6).setCellValue(obj.getString("circle_name"));
                drow.createCell(7).setCellValue(obj.getString("bts_type"));
                drow.createCell(8).setCellValue(obj.getString("site_type"));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/MyBtsleftout.xls");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(filepath);
                    workbook.write(fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    Toast.makeText(getApplicationContext(), "Excel file generated sucessfully in downloads folder", Toast.LENGTH_SHORT).show();
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

    private static class MyTask extends AsyncTask<String, Void, String>{
        private final WeakReference<Mybts_Leftout> activityReference;

        public MyTask(Mybts_Leftout context) {
            activityReference = new WeakReference<>(context);
        }
        ProgressDialog pd ;
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("LeftOut myBts");
            pd.setMessage("Getting the list of left out myBts list...");
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            Mybts_Leftout activity = activityReference.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Authorization",activity.sharedPreferences.getString("web_token",""));
                conn.setRequestProperty("Content-Type","application/json; utf-8");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("circle_id", activity.circle_id );
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

        @SuppressLint("RtlHardcoded")
        @Override
        protected void onPostExecute(String s) {
            Mybts_Leftout activity = activityReference.get();
            pd.dismiss();
            CardView.LayoutParams param = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            param.setMargins(10,10,10,10);
            try {
                JSONObject object = new JSONObject(s);
                JSONArray arr = new JSONArray(object.getString("data"));
//                System.out.println(s);
                if(arr.length()>0){
                    for(int i=0; i<arr.length(); i++){
                        final JSONObject obj = new JSONObject(arr.getString(i));
                        String optr_id = obj.getString("operator_id");
                        String opr_name = Config.getOperatorNames(activity.operators, optr_id);
//                        System.out.println(optr_id +"->"+ opr_name);

                        StringBuilder str = new StringBuilder();
                        str.append(obj.getString("bts_name"));
                        str.append("\n");
                        str.append(obj.getString("circle_name")+"/"+obj.getString("ssa_name"));
                        str.append("\n");
                        str.append(obj.getString("bts_type")+"/"+obj.getString("site_type")+"/"+obj.getString("vendor_name")+"/"+obj.getString("site_category"));
                        if(!obj.getString("outsrc_name").equals("NOT APPLICABLE")){
                            str.append("\n");
                            str.append(obj.getString("outsrc_name"));
                        }
                        LinearLayout ll = new LinearLayout(activity);
                        CardView card = new CardView(activity);
                        card.setMaxCardElevation(5);
                        card.setCardElevation(5);
                        card.setLayoutParams(param);
                        card.setPadding(10, 10, 10, 10);
                        card.setRadius(30);
                        card.setUseCompatPadding(true);
//                        String site_category = obj.getString("site_category");
                        card.setCardBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.normal));
                        TextView tv = new TextView(activity);
//                    tv.setBackgroundColor(Color.rgb(32, 9, 237));
                        tv.setTextColor(Color.BLACK);
                        tv.setGravity(Gravity.CENTER);
                        tv.setPadding(15, 15, 15, 15);
                        tv.setText(str);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                        tv.setTypeface(Typeface.MONOSPACE);

                        card.addView(tv);
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(activity, AddBtsAdministrator.class);
                            try {
                                intent.putExtra("bts_id", obj.getString("bts_id"));
                                intent.putExtra("bts_name", obj.getString("bts_name"));
                                intent.putExtra("circle_name", obj.getString("circle_name"));
                                intent.putExtra("ssa_name", obj.getString("ssa_name"));
                                intent.putExtra("bts_type", obj.getString("bts_type"));
                                intent.putExtra("site_type", obj.getString("site_type"));
                                intent.putExtra("vendor_name", obj.getString("vendor_name"));
                                intent.putExtra("site_category", obj.getString("site_category"));
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
                    alertDialog.setMessage("No sites are left for configuration");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> activity.finish());
                    alertDialog.show();
                }
            } catch (JSONException  e) {
                e.printStackTrace();
            }
        }
    }

    private static class getMyBtsLeftOut extends AsyncTask<String, String, String>{
        private final WeakReference<Mybts_Leftout> activityReference1;

        private getMyBtsLeftOut(Mybts_Leftout context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            Mybts_Leftout activity = activityReference1.get();
            try {
                HttpsURLConnection conn = new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"));
                conn.setRequestProperty("Content-Type","application/json; utf-8");
                conn.setRequestProperty("Authorization",activity.sharedPreferences.getString("web_token",""));
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("circle_id", activity.circle_id);
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}