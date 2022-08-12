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
import android.util.Log;
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

public class TechWise extends SessionActivity {
    private TableLayout tl;
    private String circle_id, user_privs;
    private TextView tv_header;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_wise);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException |IOException e) {
            e.printStackTrace();
        }

        MyTask myTask;
        ImageButton toXlsx, shareBy;

        circle_id = sharedPreferences.getString("circle_id",null);
        user_privs = sharedPreferences.getString("user_privs", null);
        tv_header = findViewById(R.id.textView1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener((View v)-> onBackPressed());
        }

        ImageButton homeBtn = toolbar.findViewById(R.id.home);
        homeBtn.setOnClickListener((View v) -> startActivity(new Intent(this, Navigational.class)));

        Uri.Builder uri_builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath(getString(R.string.url_circlewise_tech_down));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

//        Presenting the data
        tl = findViewById(R.id.tbl_lyt);
        myTask = new MyTask(this);
        myTask.execute(uri_builder.toString());


        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
        pullToRefresh.setOnRefreshListener(()-> {
                tl.removeAllViews();
                new MyTask(this).execute(uri_builder.toString());
                pullToRefresh.setRefreshing(false);
        });

        toXlsx = findViewById(R.id.toXlsx);
        toXlsx.setOnClickListener((View v)-> buttonCreateExcel(uri_builder.toString()));

        shareBy = findViewById(R.id.shareby);
        shareBy.setOnClickListener((View v) -> {
//                Toast.makeText(SsaWiseAvailability.this, "Share it is clicked",Toast.LENGTH_SHORT).show();
//                Bitmap bitmap = takeScreenshot();
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = rootView.getDrawingCache();
            ScreeShare screeShare = new ScreeShare(getApplicationContext());
            screeShare.saveBitmap(bitmap, "Circlewise Tech wise btsdown count "+Constants.getCurrentTime());
        });
    }

    private static class MyTask extends AsyncTask<String, String, String> {
        private final WeakReference<TechWise> activityReference;
        private MyTask(TechWise context) {
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
        protected String doInBackground(String... params) {
            TechWise activity = activityReference.get();
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
            TechWise activity = activityReference.get();
            pd.dismiss();
//            System.out.println(s);
            TableRow tr1 = new TableRow(activity);
            TextView tv1 = new TextView(activity);
            tv1.setText(R.string.header_circle);
            tr1.addView(tv1);
            TextView tv2 = new TextView(activity);
            tv2.setText(R.string.header_2g);
            tr1.addView(tv2);
            TextView tv3 = new TextView(activity);
            tv3.setText(R.string.header_3g);
            tr1.addView(tv3);
            TextView tv4 = new TextView(activity);
            tv4.setText(R.string.header_4g);
            tr1.addView(tv4);
            TextView tv5 = new TextView(activity);
            tv5.setText(R.string.header_total);
            tr1.addView(tv5);
            TextView tv6 = new TextView(activity);
            tv6.setText(R.string.header_perc);
            tr1.addView(tv6);
            for (int k = 0; k < tr1.getChildCount(); k++) {
                View v = tr1.getChildAt(k);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                    v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.blue));
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
                JSONArray u_obj =new JSONArray(url_obj.getString("data"));
                JSONArray obj = new JSONArray();
                if(activity.circle_id.equals("HR") || activity.circle_id.equals("UW")){
                    for(int i=0; i<u_obj.length(); i++){
                        JSONObject n_obj = new JSONObject(u_obj.getString(i));
                        String circle = n_obj.getString("circle_id");
                        if(circle.equals(activity.circle_id) || circle.equals("DL")){
                            obj.put(n_obj);
                        }
                    }
                    activity.tv_header.setText(activity.getString(R.string.header_tech,activity.circle_id));
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
                    JSONObject obj1 = new JSONObject(obj.getString(i));
                    final String circle_id = obj1.getString("circle_id");
                    String bts_2g_cnt = obj1.getString("bts_2g_cnt");
                    String bts_3g_cnt = obj1.getString("bts_3g_cnt");
                    String bts_4g_cnt = obj1.getString("bts_4g_cnt");
                    String bts_2g_down_cnt = obj1.getString("bts_2g_down_cnt");
                    String bts_3g_down_cnt = obj1.getString("bts_3g_down_cnt");
                    String bts_4g_down_cnt = obj1.getString("bts_4g_down_cnt");
                    String down_cnt = obj1.getString("down_cnt");
                    String total = obj1.getString("total_cnt");
                    double perc = obj1.getDouble("perc");
                    btn.setText(circle_id);
                    tr.addView(btn);
                    btn.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, TechWiseSsa.class);
                        intent.putExtra("circle_id", circle_id);
                        activity.startActivity(intent);
                    });

                    Button btn_2g = new Button(activity);
//                    btn_2g.setText(bts_2g_down_cnt+"/"+bts_2g_cnt);
                    if(circle_id.equals("Total")){
                        btn_2g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_2g_down_cnt,  bts_2g_cnt));
                    } else {
                        btn_2g.setText(activity.getString(R.string.down_wrt_total, bts_2g_down_cnt, bts_2g_cnt));
                    }
//                    btn_2g.setText(activity.getString(R.string.down_wrt_total, bts_2g_down_cnt, bts_2g_cnt));
                    btn_2g.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, TechWiseDetails.class);
                        intent.putExtra("circle_id",circle_id);
                        intent.putExtra("ssa_id","%");
                        intent.putExtra("bts_type","G");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn_2g);

                    Button btn_3g = new Button(activity);
//                    btn_3g.setText(bts_3g_down_cnt+"/"+bts_3g_cnt);
                    if(circle_id.equals("Total")){
                        btn_3g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_3g_down_cnt,  bts_3g_cnt));
                    } else {
                        btn_3g.setText(activity.getString(R.string.down_wrt_total, bts_3g_down_cnt, bts_3g_cnt));
                    }
                    btn_3g.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, TechWiseDetails.class);
                        intent.putExtra("circle_id",circle_id);
                        intent.putExtra("ssa_id","%");
                        intent.putExtra("bts_type","U");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn_3g);

                    Button btn_4g = new Button(activity);
//                    btn_4g.setText(bts_4g_down_cnt+"/"+bts_4g_cnt);
                    if(circle_id.equals("Total")){
                        btn_4g.setText(activity.getString(R.string.down_wrt_total_for_total, bts_4g_down_cnt,  bts_4g_cnt));
                    } else {
                        btn_4g.setText(activity.getString(R.string.down_wrt_total, bts_4g_down_cnt, bts_4g_cnt));
                    }
                    btn_4g.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, TechWiseDetails.class);
                        intent.putExtra("circle_id",circle_id);
                        intent.putExtra("ssa_id","%");
                        intent.putExtra("bts_type","L");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn_4g);

                    Button btn_down = new Button(activity);
//                    btn_down.setText(down_cnt+"/"+total);
                    if(circle_id.equals("Total")){
                        btn_down.setText(activity.getString(R.string.down_wrt_total_for_total, down_cnt,  total));
                    } else {
                        btn_down.setText(activity.getString(R.string.down_wrt_total, down_cnt, total));
                    }
//                    btn_down.setText(activity.getString(R.string.down_wrt_total, down_cnt, total));
                    btn_down.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, TechWiseDetails.class);
                        intent.putExtra("circle_id",circle_id);
                        intent.putExtra("ssa_id","%");
                        intent.putExtra("bts_type","%");
                        activity.startActivity(intent);
                    });
                    tr.addView(btn_down);

                    TextView tv = new TextView(activity);
                    tv.setText(String.valueOf(perc));
                    tr.addView(tv);


                    if(perc<10) {
                        for (int k = 0; k < tr.getChildCount(); k++) {
                            View v = tr.getChildAt(k);
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                ((TextView) v).setGravity(Gravity.CENTER);
                                ((TextView) v).setTextSize(13);
                            }
                        }
                    } else {
                        for (int k = 0; k < tr.getChildCount(); k++) {
                            View v = tr.getChildAt(k);
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.red));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                ((TextView) v).setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
                                ((TextView) v).setTextSize(13);
                                v.setPadding(0, 0, 0, 0);
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
                    if(i ==1 || i==2 || i==3) {
                        params.width = 150;
                    } else if(i==4) {
                        params.width = 200;
                    } else {
                        params.width=120;
                    }
                    params.height = 80;
                    if(j == activity.tl.getChildCount()-1){
                        params.height =120;
                    } else {
                        params.height=80;
                    }
                    if(v instanceof Button){
                        v.setPadding(0,0,0,0);
                    }
                }

//                Last row Format
                if(activity.user_privs.equals("co")) {
                    if (j == activity.tl.getChildCount() - 1) {
                        ViewGroup tabrow = (ViewGroup) activity.tl.getChildAt(j);
                        for (int k = 0; k < tabrow.getChildCount(); k++) {
                            View v = tabrow.getChildAt(k);
                            if (v instanceof TextView) {
                                ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.maroon));
                                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                ((TextView) v).setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
                                ((TextView) v).setTextSize(11);
                                v.setPadding(0, 0, 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    public void buttonCreateExcel(String url){
        try {
            String flts = new getTechwiseFaults(this).execute(url).get();
//            System.out.println(flts);
//            Writing to Data to XLS file
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("TechWise");

            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("CircleID");
            row.createCell(1).setCellValue("2G");
            row.createCell(2).setCellValue("3G");
            row.createCell(3).setCellValue("4G");
            row.createCell(4).setCellValue("Total");
            row.createCell(5).setCellValue("Perc");

            JSONObject url_obj = new JSONObject(flts);
            if(!url_obj.getString("result").equals("true")){
                Toast.makeText(this, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SesssionLogout.class));
            }
            JSONArray arr =new JSONArray(url_obj.getString("data"));
            for(int i=0; i<arr.length(); i++){
                HSSFRow drow = sheet.createRow(i+1);
                JSONObject obj = arr.getJSONObject(i);
                drow.createCell(0).setCellValue(obj.getString("circle_id"));
                drow.createCell(1).setCellValue(Integer.parseInt(obj.getString("bts_2g_down_cnt")));
                drow.createCell(2).setCellValue(Integer.parseInt(obj.getString("bts_3g_down_cnt")));
                drow.createCell(3).setCellValue(Integer.parseInt(obj.getString("bts_4g_down_cnt")));
                drow.createCell(4).setCellValue(Integer.parseInt(obj.getString("down_cnt")));
                drow.createCell(5).setCellValue(Double.parseDouble(obj.getString("perc")));
            }

            if(isExternalStorageWritable()) {
                File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/TechwiseFaults.xls");
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


//        Writing to FIle

    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("ExternalStorage","Yes it is writeable");
            return true;
        } else {
            return false;
        }
    }

    private static class getTechwiseFaults extends  AsyncTask<String, String, String>{
        private final WeakReference<TechWise> activityReference1;

        private getTechwiseFaults(TechWise context) {
            activityReference1 = new WeakReference<>(context);
        }


        @Override
        protected String doInBackground(String... strings) {
            TechWise activity = activityReference1.get();
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