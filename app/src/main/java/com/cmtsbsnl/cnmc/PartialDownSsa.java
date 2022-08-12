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

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PartialDownSsa extends SessionActivity {
    private TableLayout tl;
    private String circle_id;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partial_down_ssa);

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
        header.setText(getString(R.string.header_partialdown_circle,circle_id));

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

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_ssawise_partial));

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

//        Generate Xlx
        toXlsx = findViewById(R.id.toXlsx);
        toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString()));

        shareBy = findViewById(R.id.shareby);
        shareBy.setOnClickListener(v -> {
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = rootView.getDrawingCache();
            ScreeShare screeShare = new ScreeShare(getApplicationContext());
            screeShare.saveBitmap(bitmap, "ssa wise Partial btsdown count "+Constants.getCurrentTime());
        });
    }

    private Boolean isExternalStorageWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private void buttonCreateExcel(String url) {
        try {
            String flts = new getPartialDownSSAWiseFaults(this).execute(url).get();
            System.out.println(flts);
//            Writing to Data to XLS file
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("PartialDownSSA");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("SsaID");
            row.createCell(1).setCellValue("GSM");
            row.createCell(2).setCellValue("UMTS");
            row.createCell(3).setCellValue("LTE");
            row.createCell(4).setCellValue("Total");

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
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("pdown_g")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("pdown_u")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("pdown_l")));
                drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("Total")));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/PartialFaultsSSAWise.xls");
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


    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<PartialDownSsa> activityReference;
        ProgressDialog pd ;
        private MyTask(PartialDownSsa context) {
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
            PartialDownSsa activity = activityReference.get();
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
            PartialDownSsa activity = activityReference.get();
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_gsm);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_umts);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_lte);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_total);
            tr1.addView(tv5);
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
                    String gsm = cat_data.getString("pdown_g");
                    String umts = cat_data.getString("pdown_u");
                    String lte = cat_data.getString("pdown_l");
                    String total = cat_data.getString("Total");
                    btn.setText(ssa_id);
                    tr.addView(btn);
                    Button btn1 = new Button(activity);
                    btn1.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle_id", activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","G");
                        activity.startActivity(intent);
                    });
                    btn1.setText(gsm);
                    tr.addView(btn1);
                    Button btn2 = new Button(activity);
                    btn2.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle_id", activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","U");
                        activity.startActivity(intent);
                    });
                    btn2.setText(umts);
                    tr.addView(btn2);

                    Button btn3 = new Button(activity);
                    btn3.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle_id", activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","L");
                        activity.startActivity(intent);
                    });
                    btn3.setText(lte);
                    tr.addView(btn3);

                    Button btn4 = new Button(activity);
                    btn4.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle_id", activity.circle_id);
                        intent.putExtra("ssa_id", ssa_id);
                        intent.putExtra("criteria","%");
                        activity.startActivity(intent);
                    });
                    btn4.setText(total);
                    tr.addView(btn4);
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
                        params.width = 300;
                    } else if(i==6){
                        params.width =200;
                    } else {
                        params.width=200;
                    }
                    params.height = 70;
                    if(v instanceof TextView){
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
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

    public static class getPartialDownSSAWiseFaults extends AsyncTask<String, String, String>{
        private final WeakReference<PartialDownSsa> activityReference1;
        private getPartialDownSSAWiseFaults(PartialDownSsa context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            PartialDownSsa activity = activityReference1.get();
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
