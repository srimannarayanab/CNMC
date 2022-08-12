package com.cmtsbsnl.cnmc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;

public class UnlinkBts extends SessionActivity {
	private String mybtsMsisdn;
	private SharedPreferences sharedPreferences;
	private List<String> myBtsModalList;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlink_bts);

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		toolbar.setNavigationOnClickListener(v -> {
			onBackPressed();
//                finish();
		});

		ImageButton homeBtn = (ImageButton) toolbar.findViewById(R.id.home);
		homeBtn.setOnClickListener(v -> startActivity(new Intent(UnlinkBts.this, Navigational.class)));

		try {
			sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}

		findViewById(R.id.search_bts).setVisibility(View.INVISIBLE);

		EditText editText = findViewById(R.id.search_msisdn);

		listView = findViewById(R.id.listView);
		Button btn_submit = findViewById(R.id.btn);
		btn_submit.setOnClickListener(view -> {
			mybtsMsisdn = String.valueOf(editText.getText());
			if(mybtsMsisdn.length()!=10){
				editText.setError("Msisdn length should be 10");
				return;
			}

//				System.out.println(mybtsMsisdn);
			Uri.Builder uri_builder = new Uri.Builder()
							.scheme("https")
							.authority(Constants.getSecureBaseUrl())
							.appendPath(getString(R.string.ulr_get_mybts_list));
			try {
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			} catch (Exception e) {
				// TODO: handle exception
			}

			MyTask myTask = new MyTask(UnlinkBts.this);
			myTask.execute(uri_builder.toString());
		});



	}

	private class MyTask extends AsyncTask<String, String, String> {
		private final WeakReference<UnlinkBts> activityReference;
		ProgressDialog pd;
		private MyTask(UnlinkBts context){
			activityReference = new WeakReference<>(context);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(activityReference.get());
			pd.setTitle("Fetching my Bts configured list");
			pd.setMessage("Processing.... ");
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected void onPostExecute(String s) {
			UnlinkBts activity = activityReference.get();
			pd.dismiss();
			HashMap<String, MyBtsModal> linkedBts = new HashMap<>();
			activity.listView.setAdapter(null);
			try {
				JSONObject url_obj = new JSONObject(s);
				if(!url_obj.getString("result").equals("true")){
					Toast.makeText(activity, url_obj.getString("error"), Toast.LENGTH_SHORT).show();
					activity.startActivity(new Intent(activity, SesssionLogout.class));
				}
				JSONArray arr =new JSONArray(url_obj.getString("data"));
				if(arr.length()==0){
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setMessage("No Bts are configured")
									.setTitle("MyBTS Configuration")
									.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.dismiss();

										}
									});
					builder.show();
				} else {
					activity.myBtsModalList = new ArrayList<>();
					for (int i = 0; i < arr.length(); i++) {
						JSONObject obj = arr.getJSONObject(i);
						String bts_name = obj.getString("bts_name");
						String bts_type = obj.getString("bts_type");
						String ssa_id = obj.getString("ssa_id");
						int mybts_id = obj.getInt("mybts_id");

						MyBtsModal myBtsModal = new MyBtsModal(bts_name, bts_type, ssa_id, mybts_id);
						linkedBts.put(myBtsModal.toString(), myBtsModal);
						activity.myBtsModalList.add(myBtsModal.toString());
					}

					// Create a list adapter
					ArrayAdapter<String> adapter = new ArrayAdapter<>(UnlinkBts.this, R.layout.my_bts_list_view,
									R.id.textview, activity.myBtsModalList);
					listView.setAdapter(adapter);
					listView.setTextFilterEnabled(true);
					listView.setClickable(true);

					EditText ed = findViewById(R.id.search_bts);
					ed.setVisibility(View.VISIBLE);

					ed.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//					adapter.getFilter().filter(charSequence);
//					adapter.notifyDataSetChanged();

						}

						@Override
						public void afterTextChanged(Editable editable) {
							List<String> filteredList = new ArrayList<>();
							for(String item: myBtsModalList){
								if(item.toLowerCase().contains(editable)){
									filteredList.add(item);
								}
							}
//                dataModels = filteredList;
							ArrayAdapter<String> adapter = new ArrayAdapter<>(UnlinkBts.this, R.layout.my_bts_list_view,
											R.id.textview, filteredList);
							listView.setAdapter(adapter);

						}
					});
//			list view on click listner
					listView.setOnItemClickListener((adapterView, view, i, l) -> {
						AlertDialog.Builder builder = new AlertDialog.Builder(activity)
							.setTitle("Deletion Bts from MyBts List")
							.setMessage("Deletion of bts from mybts can we proceeded?")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int d) {
									String data = (String) listView.getItemAtPosition(i);
									adapter.remove(data);
									adapter.notifyDataSetChanged();

									DeleteMyBts deleteMyBts = new DeleteMyBts(UnlinkBts.this);
									Uri.Builder uri_builder_mybtsid = new Uri.Builder()
													.scheme("https")
													.authority(Constants.getSecureBaseUrl())
													.appendPath(getString(R.string.url_delete_mybts_id));
//									System.out.println(data);
									deleteMyBts.execute(uri_builder_mybtsid.toString(), String.valueOf(linkedBts.get(data).getMybts_id()));
								}
							})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
								}
							});
						builder.show();
//						Toast.makeText(UnlinkBts.this, String.valueOf(Objects.requireNonNull(linkedBts.get(data)).getMybts_id()), Toast.LENGTH_SHORT).show();
					});
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... strings) {
			UnlinkBts activity = activityReference.get();
			try {
				JSONObject post_obj = new JSONObject();
				post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
				post_obj.put("mybts_msisdn", activity.mybtsMsisdn);
				return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
								post_obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}


	private static class DeleteMyBts extends AsyncTask<String, String ,String>{
		private final WeakReference<UnlinkBts> activityReference1;
		ProgressDialog pd;

		private DeleteMyBts(UnlinkBts context){
			activityReference1 = new WeakReference<>(context);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(activityReference1.get());
			pd.setTitle("Deletion in progress");
			pd.setMessage("Processing.... ");
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected void onPostExecute(String s) {
			UnlinkBts activity = activityReference1.get();
			pd.dismiss();
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage("Sucessfully deleted bts from mybts list")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
								}
							});
			builder.show();
		}

		@Override
		protected String doInBackground(String... strings) {
			UnlinkBts activity = activityReference1.get();
			try {
				JSONObject post_obj = new JSONObject();
				post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
				post_obj.put("mybts_id", strings[1]);
				return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
								post_obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}