package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MyBtsUsers extends SessionActivity {
  private TableLayout tl;
  private SharedPreferences sharedPreferences;
  private String circle_id;

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_bts_users);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    ImageButton homeBtn = toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener(v -> startActivity(new Intent(this, Navigational.class)));

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

//        Get Circle/ssawise user list count
    Uri.Builder uri_builder = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_mybts_users));
//        Get user details
    Uri.Builder uri_builder1 = new Uri.Builder()
        .scheme("https")
        .authority(Constants.getSecureBaseUrl())
        .appendPath(getString(R.string.url_get_circle_user_details));

//       Verify the intent is called or not
    String user_privs;
    Intent intent = getIntent();
    if(intent.getExtras() !=null){
      user_privs = intent.getStringExtra("user_privs");
      circle_id = intent.getStringExtra("circle_id");
    } else {
      user_privs = sharedPreferences.getString("user_privs", "");
      circle_id = sharedPreferences.getString("circle_id", "");
    }

    if(Objects.requireNonNull(user_privs).equals("co")){
      circle_id ="%";
    }
    tl = findViewById(R.id.tbl_lyt);
//        System.out.println(user_privs + " "+circle_id);

    MyTask myTask = new MyTask(this);
    myTask.execute(uri_builder.toString(), circle_id, user_privs);

//        Generate total user list
    ImageButton toXlsx = findViewById(R.id.toXlsx);
    String finalCircle_id = circle_id;
    toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder1.toString(), finalCircle_id));
  }

  private Boolean isExternalStorageWritable(){
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }

  private void buttonCreateExcel(String url, String circle_id) {
    try {
      String flts = new getUserList(this).execute(url, circle_id).get();
      System.out.println(flts);
//            Writing to Data to XLS file
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("UserList");

      HSSFRow row = sheet.createRow(0);
      row.createCell(0).setCellValue("msisdn");
      row.createCell(1).setCellValue("name");
      row.createCell(2).setCellValue("desg");
      row.createCell(3).setCellValue("email");
      row.createCell(4).setCellValue("hrms_no");
      row.createCell(5).setCellValue("circle");
      row.createCell(6).setCellValue("circle_id");
      row.createCell(7).setCellValue("circle");
      row.createCell(8).setCellValue("ssaname");
      row.createCell(9).setCellValue("ssa_id");
      row.createCell(10).setCellValue("app_version");
      row.createCell(11).setCellValue("lvl");
      row.createCell(12).setCellValue("lvl2");
      row.createCell(13).setCellValue("lvl3");

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
        drow.createCell(3).setCellValue(obj.getString("email"));
        drow.createCell(4).setCellValue(obj.getString("hrms_no"));
        drow.createCell(5).setCellValue(obj.getString("circle"));
        drow.createCell(6).setCellValue(obj.getString("circle_id"));
        drow.createCell(7).setCellValue(obj.getString("ssaname"));
        drow.createCell(8).setCellValue(obj.getString("ssa_id"));
        drow.createCell(9).setCellValue(obj.getString("last_login"));
        drow.createCell(10).setCellValue(obj.getString("app_version"));
        drow.createCell(11).setCellValue(obj.getString("lvl"));
        drow.createCell(12).setCellValue(obj.getString("lvl2"));
        drow.createCell(13).setCellValue(obj.getString("lvl3"));

      }

      if(isExternalStorageWritable()) {
        File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/AppUserList.xls");
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
    private final WeakReference<MyBtsUsers> activityReference;
    ProgressDialog pd;

    private MyTask(MyBtsUsers context) {
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
    protected String doInBackground(String... strings) {
      MyBtsUsers activity = activityReference.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle_id", strings[1]);
        post_obj.put("user_privs", strings[2]);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      MyBtsUsers activity = activityReference.get();
      pd.dismiss();
//            System.out.println(s);
      TableRow tr1 = new TableRow(activity);
      TextView tv1 = new TextView(activity);
      tv1.setText(R.string.header_circle);
      tr1.addView(tv1);

      TextView tv2 = new TextView(activity);
      tv2.setText(R.string.header_users);
      tr1.addView(tv2);

      TextView tv3 = new TextView(activity);
      tv3.setText(R.string.header_mybts);
      tr1.addView(tv3);

      TextView tv4 = new TextView(activity);
      tv4.setText(R.string.header_todaylogin);
      tr1.addView(tv4);

      activity.tl.addView(tr1);

      try {
        JSONObject url_obj = new JSONObject(s);
        if(!url_obj.getString("result").equals("true")){
          Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
          activity.startActivity(new Intent(activity, SesssionLogout.class));
        }
        JSONArray arr =new JSONArray(url_obj.getString("data"));
        for(int x=0 ; x<arr.length(); x++){
          JSONObject obj = arr.getJSONObject(x);
          final String circle = obj.getString("c_name");
          String total_users = obj.getString("total_users");
          String mybts_users = obj.getString("mybts_cnt");
          String login = obj.getString("login_count");
          TableRow tr_1 = new TableRow(activity);
//                    TextView tv_1 = new TextView(activity);
//                    tv_1.setText(circle);
//                    tr_1.addView(tv_1);
          Button btn_ssausers = new Button(activity);
          btn_ssausers.setText(circle);
          tr_1.addView(btn_ssausers);

          btn_ssausers.setOnClickListener(v -> {
            Intent intent = new Intent(activity, MyBtsUsers.class);
            intent.putExtra("circle_id", circle);
            intent.putExtra("user_privs", "circle");
            activity.startActivity(intent);
          });

          Button btn_users = new Button(activity);
          btn_users.setText(total_users);
          tr_1.addView(btn_users);

          btn_users.setOnClickListener(v -> {
            Intent intent = new Intent(activity, CircleUserList.class);
            intent.putExtra("circle_id", circle);
            activity.startActivity(intent);
          });

          Button btn_mybts = new Button(activity);
          btn_mybts.setText(mybts_users);
          tr_1.addView(btn_mybts);

          btn_mybts.setOnClickListener(v -> {
            Intent intent = new Intent(activity,MyBtsUserList.class);
            intent.putExtra("circle_id",activity.circle_id);
            intent.putExtra("ssa_id", circle); // As for co and circle same page is using hence ssa_id is with circle
            activity.startActivity(intent);
          });

//          TextView tv_4 = new TextView(activity);
//          tv_4.setText(login);
//          tr_1.addView(tv_4);

          Button btn_login = new Button(activity);
          btn_login.setText(login);
          tr_1.addView(btn_login);

          btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(activity, TodayLogins.class);
              intent.putExtra("circle_id",activity.circle_id);
              intent.putExtra("ssa_id", circle); // As for co and circle same page is using hence ssa_id is with circle
              activity.startActivity(intent);


            }
          });

          activity.tl.addView(tr_1);

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
          params.height = 80;
          if(v instanceof TextView){
            ((TextView) v).setTextColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.white));
            v.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.blue));
            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            ((TextView) v).setGravity(Gravity.CENTER);
            ((TextView) v).setTextSize(13);
          }
          if(v instanceof Button){
            v.setPadding(0,0,0,0);
          }
        }
      }
    }
  }

  public static class getUserList extends AsyncTask<String, String, String>{
    private final WeakReference<MyBtsUsers> activityReference1;

    private getUserList(MyBtsUsers context) {
      activityReference1 = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... strings) {
      MyBtsUsers activity = activityReference1.get();
      try {
        JSONObject post_obj = new JSONObject();
        post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
        post_obj.put("circle_id", strings[1]);
        return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
            post_obj.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}
