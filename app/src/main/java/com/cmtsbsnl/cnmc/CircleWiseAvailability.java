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
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CircleWiseAvailability extends SessionActivity {
    private TableLayout tl;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private SharedPreferences sharedPreferences;
    private String circle_id;
    private TextView tv_header;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wise_availability);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageButton toXlsx, shareBy;
        MyTask myTask;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException |IOException e) {
            e.printStackTrace();
        }
        circle_id = sharedPreferences.getString("circle_id",null);
        tv_header = findViewById(R.id.textView1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)->onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener((View v)-> startActivity(new Intent(getApplicationContext(), Navigational.class)));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

//        Uri builder for building the url
//        String msisdn = sharedPreferences.getString("msisdn","");
        Uri.Builder uri_builder =new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_circlewise_availability));

//        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());

//        Swipe to Refresh
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(()->{
            tl.removeAllViews();
            new MyTask(this).execute(uri_builder.toString());
            swipeRefreshLayout.setRefreshing(false);
        });
       /* swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tl.removeAllViews();
                new MyTask().execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/

//        Create an uri builder to build the url

//        To Xlsx
        toXlsx = findViewById(R.id.toXlsx);
        toXlsx.setOnClickListener((View v)->buttonCreateExcel(uri_builder.toString()));

        //        Share it
        shareBy = findViewById(R.id.shareby);
        shareBy.setOnClickListener((View v)->{
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = rootView.getDrawingCache();
            ScreeShare screeShare = new ScreeShare(getApplicationContext());
            screeShare.saveBitmap(bitmap, "CircleWise At a Glance "+Constants.getCurrentTime());

        });
//        shareBy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(SsaWiseAvailability.this, "Share it is clicked",Toast.LENGTH_SHORT).show();
////                Bitmap bitmap = takeScreenshot();
//                View rootView = getWindow().getDecorView().getRootView();
//                rootView.setDrawingCacheEnabled(true);
//                Bitmap bitmap = rootView.getDrawingCache();
//                ScreeShare screeShare = new ScreeShare(getApplicationContext());
//                screeShare.saveBitmap(bitmap, "CircleWise At a Glance");
//            }
//        });

    }

    private Boolean isExternalStorageWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private void buttonCreateExcel(String url) {
        try {
            String flts = new getCircleWiseFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("PartialDown");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("CircleID");
            row.createCell(1).setCellValue("Down");
            row.createCell(2).setCellValue("Partial");
            row.createCell(3).setCellValue("Count");
            row.createCell(4).setCellValue("Perc");
            row.createCell(5).setCellValue("Rank");

            int down =0, partial=0, total=0;

            JSONObject url_obj = new JSONObject(flts);
            if(!url_obj.getString("result").equals("true")){
                Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SesssionLogout.class));
            }
            JSONArray arr =new JSONArray(url_obj.getString("data"));
            for(int i=0; i<arr.length(); i++){
                HSSFRow drow = sheet.createRow(i+1);
                JSONObject obj = arr.getJSONObject(i);
                drow.createCell(0).setCellValue(obj.getString("circle"));
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("down")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("partial_down")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("total")));
                drow.createCell(4).setCellValue(Double.parseDouble(obj.getString("perc_availability")));
                drow.createCell(5).setCellValue(i+1);
                down += Integer.parseInt(obj.getString("down"));
                partial += Integer.parseInt(obj.getString("partial_down"));
                total +=Integer.parseInt(obj.getString("total"));
            }

            double perc = 100.00-(down+partial)*100.00/total;

            HSSFRow trow = sheet.createRow(arr.length()+1);
            trow.createCell(0).setCellValue("Total");
            trow.createCell(1).setCellValue(down);
            trow.createCell(2).setCellValue(partial);
            trow.createCell(3).setCellValue(total);
            trow.createCell(4).setCellValue(Double.parseDouble(decimalFormat.format(perc)));

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/CircleWiseFaults.xls");
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


        } catch (ExecutionException |InterruptedException|JSONException e) {
            e.printStackTrace();
        }
    }

    private static class MyTask extends AsyncTask<String, String, String> {

        private final WeakReference<CircleWiseAvailability> activityReference;

        // only retain a weak reference to the activity
        MyTask(CircleWiseAvailability context) {
            activityReference = new WeakReference<>(context);
        }

        ProgressDialog pd;
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
        protected String doInBackground(String... strings) {
            CircleWiseAvailability activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
//            System.out.println(s);
            CircleWiseAvailability activity = activityReference.get();
            pd.dismiss();
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_down);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_partial);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_count);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_perc);
            tr1.addView(tv5);
            TextView tv6 = new TextView(activity);
            tv6.setText(R.string.header_rank);
            tr1.addView(tv6);
            activity.tl.addView(tr1);
            int total_down =0;
            int total_partial = 0;
            int total_cnt = 0;
            try {
                JSONObject url_obj = new JSONObject(s);
                if(!url_obj.getString("result").equals("true")){
                    Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, SesssionLogout.class));
                    activity.finish();
                }
                JSONArray u_obj =new JSONArray(url_obj.getString("data"));
                JSONArray obj = new JSONArray();
                if(activity.circle_id.equals("HR") || activity.circle_id.equals("UW")){
                    for(int i=0; i<u_obj.length(); i++){
                        JSONObject n_obj = new JSONObject(u_obj.getString(i));
                        String circle = n_obj.getString("circle");
                        if(circle.equals(activity.circle_id) || circle.equals("DL")){
                            obj.put(n_obj);
                        }
                    }
                    activity.tv_header.setText(activity.getString(R.string.header_ssaavailability,activity.circle_id));
                    View v_toxlsx = activity.findViewById(R.id.toXlsx);
                    if(v_toxlsx !=null) {
                        ViewGroup viewGroup = (ViewGroup) v_toxlsx.getParent();
                        if (viewGroup != null) {
                            viewGroup.removeView(v_toxlsx);
                        }
                    }
                } else {
                    obj = u_obj;
                }
//                Log.i("Array", obj.toString());
                for(int i=0; i<obj.length(); i++){
                    TableRow tr = new TableRow(activity);
                    Button btn = new Button(activity);
                    JSONObject cat_data = new JSONObject(obj.getString(i));
                    final String circle_id = cat_data.getString("circle");
                    String down = cat_data.getString("down");
                    String p_down = cat_data.getString("partial_down");
                    String total_sites_cnt = cat_data.getString("total");
                    String perc = cat_data.getString("perc_availability");
                    total_down += Integer.parseInt(down);
                    total_partial +=Integer.parseInt(p_down);
                    total_cnt +=Integer.parseInt(total_sites_cnt);
                    btn.setText(circle_id);
                    tr.addView(btn);
                    btn.setOnClickListener((View v)->{
                        Intent intent = new Intent(activity, SsaWiseAvailability.class);
                        intent.putExtra("circle_id", circle_id);
                        activity.startActivity(intent);
                    });

                    /*btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, SsaWiseAvailability.class);
                            intent.putExtra("circle_id", circle_id);
                            startActivity(intent);
                        }
                    });*/
                    TextView down_txt = new TextView(activity);
                    down_txt.setText(down);
                    tr.addView(down_txt);
                    TextView pdown_txt =new TextView(activity);
                    pdown_txt.setText(p_down);
                    tr.addView(pdown_txt);
                    TextView sitecnt_txt = new TextView(activity);
                    sitecnt_txt.setText(total_sites_cnt);
                    tr.addView(sitecnt_txt);
                    TextView perc_txt = new TextView(activity);
                    perc_txt.setText(perc);
                    tr.addView(perc_txt);
                    TextView rank = new TextView(activity);
                    rank.setText(String.valueOf(i+1));
                    tr.addView(rank);
                    activity.tl.addView(tr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TableRow tr2 = new TableRow(activity);
            TextView tv2_1 = new TextView(activity);
            tv2_1.setText(R.string.header_total);
            tr2.addView(tv2_1);

            TextView tv2_2 = new TextView(activity);
            tv2_2.setText(String.valueOf(total_down));
            tr2.addView(tv2_2);

            TextView tv2_3 = new TextView(activity);
            tv2_3.setText(String.valueOf(total_partial));
            tr2.addView(tv2_3);

            TextView tv2_4 = new TextView(activity);
            tv2_4.setText(String.valueOf(total_cnt));
            tr2.addView(tv2_4);

            TextView tv2_5 = new TextView(activity);
            float percentage;
            try {
                percentage = 100- ((float) (total_down + total_partial)* 100 / total_cnt);
            } catch (ArithmeticException ae){
                percentage = 0.0f;
            }
            tv2_5.setText(String.format(Locale.getDefault(),"%2.02f", percentage));
            tr2.addView(tv2_5);

            TextView tv2_6 = new TextView(activity);
            tv2_6.setText("");
            tr2.addView(tv2_6);

            activity.tl.addView(tr2);
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
                    } else if(i==2 || i==3){
                        params.width =150;
                    } else {
                        params.width=200;
                    }
                    params.height = 90;
                    if(v instanceof TextView){
                        ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white));
                        v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.blue));
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

    private static class getCircleWiseFaults extends AsyncTask<String, String, String>{
        private final WeakReference<CircleWiseAvailability> activityReference1;

        // only retain a weak reference to the activity
        getCircleWiseFaults(CircleWiseAvailability context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            CircleWiseAvailability activity = activityReference1.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0],"UTF-8"), post_obj.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
