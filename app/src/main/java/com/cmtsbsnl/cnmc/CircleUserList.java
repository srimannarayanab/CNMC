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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
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

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CircleUserList extends SessionActivity {
	private List<CircleUserModel> circleUserModelList;
	private SharedPreferences sharedPreferences;
	private String circle_id;
	private static RecyclerView recyclerView;
	private CircleUserAdapter circleUserAdapter;

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_circle_user_list);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());

		ImageButton homeBtn = toolbar.findViewById(R.id.home);
		homeBtn.setOnClickListener(v -> startActivity(new Intent(CircleUserList.this, Navigational.class)));

		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		try {
			sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}

		Intent intent = getIntent();
		if(intent.getExtras() !=null){
			circle_id = intent.getStringExtra("circle_id");
		} else {
			circle_id = sharedPreferences.getString("circle_id", "");
		}

//        Uri Buider
		Uri.Builder uri_builder = new Uri.Builder()
						.scheme("https")
						.authority(Constants.getSecureBaseUrl())
						.appendPath(getString(R.string.url_get_circle_user_details));

		//    private String circle;
		MyTask myTask = new MyTask(this);
		myTask.execute(uri_builder.toString(), circle_id);

//        To Xlsx Generate
		ImageButton toXlsx = findViewById(R.id.toXlsx);
		toXlsx.setOnClickListener(v -> buttonCreateExcel(uri_builder.toString(), circle_id));

//        Recycler view
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getApplicationContext());
//        recyclerView.setLayoutManager(layoutManager);

//        Text Change adapter
		EditText editText = findViewById(R.id.ed1);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				List<CircleUserModel> filteredList = new ArrayList<>();
				for(CircleUserModel item:circleUserModelList){
					if(item.getMsisdn().contains(editable.toString())){
						filteredList.add(item);
					}
				}
				circleUserAdapter = new CircleUserAdapter(CircleUserList.this, filteredList);
				recyclerView.setAdapter(circleUserAdapter);
			}
		});
	}

	private Boolean isExternalStorageWritable(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	private void buttonCreateExcel(String url, String circle_id) {
		try {
			String flts = new getCircleUserList(this).execute(url, circle_id ).get();
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

//            int down =0, partial=0, total=0;
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
				File filepath = new File(Environment.getExternalStorageDirectory()+"/Download/CircleUserList.xls");
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
		private final WeakReference<CircleUserList> activityReference;

		ProgressDialog pd;

		private MyTask(CircleUserList context) {
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
			CircleUserList activity = activityReference.get();
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

		@Override
		protected void onPostExecute(String s) {
			CircleUserList activity = activityReference.get();
			pd.dismiss();
//            System.out.println(s);
			try {
				JSONObject url_obj = new JSONObject(s);
				if(!url_obj.getString("result").equals("true")){
					Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
					activity.startActivity(new Intent(activity, SesssionLogout.class));
				}
				JSONArray arr =new JSONArray(url_obj.getString("data"));
				activity.circleUserModelList = new ArrayList<>();
				for(int i=0; i<arr.length(); i++){
					JSONObject obj = arr.getJSONObject(i);
					String msisdn = obj.getString("msisdn");
					String name = obj.getString("name");
					String desg = obj.getString("desg");
					String hrms_no = obj.getString("hrms_no");
					String email = obj.getString("email");
					String circle = obj.getString("circle");
					String circle_id = obj.getString("circle_id");
					String last_login = obj.getString("last_login");
					String app_version = obj.getString("app_version");
					String lvl = obj.getString("lvl");
					String lvl2 = obj.getString("lvl2");
					String lvl3 = obj.getString("lvl3");
					CircleUserModel circleUserModel = new CircleUserModel(msisdn, name, desg, email, hrms_no, circle,
									circle_id, last_login, app_version, lvl, lvl2, lvl3 );
					activity.circleUserModelList.add(circleUserModel);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			recyclerView = activity.findViewById(R.id.recyclerView);
			activity.circleUserAdapter = new CircleUserAdapter(activity, activity.circleUserModelList);
			LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getApplicationContext());
			recyclerView.setLayoutManager(layoutManager);
			recyclerView.setAdapter(activity.circleUserAdapter);

		}
	}

	public static class getCircleUserList extends AsyncTask<String, String, String>{
		private final WeakReference<CircleUserList> activityReference1;

		public getCircleUserList(CircleUserList context) {
			activityReference1 = new WeakReference<>(context);
		}

		@Override
		protected String doInBackground(String... strings) {
			CircleUserList activity = activityReference1.get();
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