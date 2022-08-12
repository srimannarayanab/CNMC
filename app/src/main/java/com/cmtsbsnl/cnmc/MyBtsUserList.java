package com.cmtsbsnl.cnmc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyBtsUserList extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private List<MyBtsUserModel> myBtsUserModelList ;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bts_user_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> startActivity(new Intent(MyBtsUserList.this, Navigational.class)));

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

//        TableLayout tl = findViewById(R.id.tbl_lyt);
        Intent intent = getIntent();
        String circle_id = intent.getStringExtra("circle_id");
        String ssa_id = intent.getStringExtra("ssa_id");

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_mybts_user_list));

        MyTask myTask = new MyTask(this);
        myTask.execute(uri_builder.toString(), circle_id, ssa_id);

//        Generate an Xlsx File
        ImageButton toXlsx = findViewById(R.id.toXlsx);
        toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    private void buttonCreateExcel(String url){
        try {
            String flts = new getMyBtsUserList(this).execute(url).get();
//            System.out.println(flts);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("DurationWise");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("msisdn");
            row.createCell(1).setCellValue("name");
            row.createCell(2).setCellValue("desg");
            row.createCell(3).setCellValue("hrms_no");
            row.createCell(4).setCellValue("circle_id");
            row.createCell(5).setCellValue("circle");
            row.createCell(6).setCellValue("bts_id");
            row.createCell(7).setCellValue("bts_name");
            row.createCell(8).setCellValue("ssa_id");


            JSONObject url_obj = new JSONObject(flts);
            if(!url_obj.getString("result").equals("true")){
                Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SesssionLogout.class));
            }
            JSONArray arr =new JSONArray(url_obj.getString("data"));
            for(int i=0; i<arr.length(); i++){
                HSSFRow drow = sheet.createRow(i+1);
                JSONObject obj = arr.getJSONObject(i);
                drow.createCell(0).setCellValue(obj.getString("msisdn"));
                drow.createCell(1).setCellValue(obj.getString("name"));
                drow.createCell(2).setCellValue(obj.getString("desg"));
                drow.createCell(3).setCellValue(obj.getString("hrms_no"));
                drow.createCell(4).setCellValue(obj.getString("circle_id"));
                drow.createCell(5).setCellValue(obj.getString("circle"));
                drow.createCell(6).setCellValue(obj.getString("bts_id"));
                drow.createCell(7).setCellValue(obj.getString("bts_name"));
                drow.createCell(8).setCellValue(obj.getString("ssa_id"));

            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/MyBtsUserConfiguredList.xls");
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

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<MyBtsUserList> activityReference;
        ProgressDialog pd;

        private MyTask(MyBtsUserList context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activityReference.get());
            pd.setTitle("Fetching user Counts...");
            pd.setMessage("Processing.... ");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            MyBtsUserList activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
                post_obj.put("circle_id", params[1]);
                post_obj.put("ssa_id",params[2]);
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0], "UTF-8"),
                        post_obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            MyBtsUserList activity = activityReference.get();
            System.out.println(s);
//            System.out.println("circle "+ circle);
//            super.onPostExecute(s);
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                }
                JSONArray arr =new JSONArray(url_obj.getString("data"));
                activity.myBtsUserModelList = new ArrayList<>();
                for(int i=0 ; i<arr.length(); i++){
                    JSONObject obj = arr.getJSONObject(i);
                    MyBtsUserModel myBtsUserModel = new MyBtsUserModel(obj.getString("name"),
                            obj.getString("circle"),
                            obj.getString("desg"),
                            obj.getString("email"),
                            obj.getString("cnt"),
                            obj.getString("msisdn"));
                    activity.myBtsUserModelList.add(myBtsUserModel);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Creating a recycler adapter
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            MyBtsUserAdapter myBtsUserAdapter = new MyBtsUserAdapter(activity, activity.myBtsUserModelList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(myBtsUserAdapter);
        }
    }

    public static class getMyBtsUserList extends AsyncTask<String, String, String> {
        private final WeakReference<MyBtsUserList> activityReference1;

        public getMyBtsUserList(MyBtsUserList context) {
            activityReference1 = new WeakReference<>(context);
        }


        @Override
        protected String doInBackground(String... strings) {
            MyBtsUserList activity = activityReference1.get();
            try {
//                String strUrl1 = "http://" + Constants.SERVER_IP + "/cnmc/getMyBtsConfiguredList.php";
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
