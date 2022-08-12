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

public class PartialDown extends SessionActivity {
    private TableLayout tl;
    private SharedPreferences sharedPreferences;
    private String circle_id;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partial_down);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MyTask myTask;
        ImageButton toXlsx, shareBy;
        TextView tv_header;

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        circle_id = sharedPreferences.getString("circle_id",null);
        tv_header = findViewById(R.id.textView1);
        tv_header.setText(getString(R.string.header_partialdown));

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
                .appendPath(getString(R.string.url_circlewise_partial));

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
            screeShare.saveBitmap(bitmap, "cricle wise Partial btsdown count "+Constants.getCurrentTime());
        });
    }

    private Boolean isExternalStorageWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private void buttonCreateExcel(String url) {
        try {
            String flts = new getPartialDownFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("PartialDown");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("CircleID");
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
                drow.createCell(0).setCellValue(obj.getString("circle"));
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("pdown_g")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("pdown_u")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("pdown_l")));
                drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("Total")));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/PartialFaults.xls");
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<PartialDown> activityReference;
        ProgressDialog pd;
        private MyTask(PartialDown context) {
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
            PartialDown activity = activityReference.get();
            try {
                JSONObject post_obj = new JSONObject();
                post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn", ""));
                return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(params[0],"UTF-8"), post_obj.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            PartialDown activity = activityReference.get();
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
                    String gsm = cat_data.getString("pdown_g");
                    String umts = cat_data.getString("pdown_u");
                    String lte = cat_data.getString("pdown_l");
                    String total = cat_data.getString("Total");
                    btn.setText(circle_id);
                    tr.addView(btn);
                    btn.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownSsa.class);
                        intent.putExtra("circle_id", circle_id);
                        activity.startActivity(intent);
                    });
                    Button gsm_txt = new Button(activity);
                    gsm_txt.setText(gsm);
                    gsm_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle", circle_id);
                        intent.putExtra("ssaname", "%");
                        intent.putExtra("criteria","G");
                        activity.startActivity(intent);
                    });
                    tr.addView(gsm_txt);
                    Button umts_txt =new Button(activity);
                    umts_txt.setText(umts);
                    umts_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle", circle_id);
                        intent.putExtra("ssaname", "%");
                        intent.putExtra("criteria","U");
                        activity.startActivity(intent);
                    });
                    tr.addView(umts_txt);
                    Button lte_txt = new Button(activity);
                    lte_txt.setText(lte);
                    lte_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle", circle_id);
                        intent.putExtra("ssaname", "%");
                        intent.putExtra("criteria","L");
                        activity.startActivity(intent);
                    });

                    tr.addView(lte_txt);
                    Button total_txt = new Button(activity);
                    total_txt.setText(total);
                    total_txt.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, PartialDownDetails.class);
                        intent.putExtra("circle", circle_id);
                        intent.putExtra("ssaname", "%");
                        intent.putExtra("criteria","%");
                        activity.startActivity(intent);
                    });
                    tr.addView(total_txt);
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
                        params.width = 200;
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

    private static class getPartialDownFaults extends AsyncTask<String, String, String>{
        private final WeakReference<PartialDown> activityReference1;
        private getPartialDownFaults(PartialDown context) {
            activityReference1 = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            PartialDown activity = activityReference1.get();
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
