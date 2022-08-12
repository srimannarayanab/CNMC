package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SearchByBts extends SessionActivity {

	private SharedPreferences sharedPreferences;
	private EditText ed_search_bts;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_by_bts);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if(getSupportActionBar() !=null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
			toolbar.setNavigationOnClickListener((View v)->onBackPressed());
		}

		ImageButton homeBtn =  toolbar.findViewById(R.id.home);
		homeBtn.setOnClickListener((View v)->startActivity(new Intent(this, Navigational.class)));

		try {
			sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}

		ed_search_bts = findViewById(R.id.search_bts);

		listView = findViewById(R.id.listView);

		ListAllBts listAllBts = new ListAllBts(this);
		listAllBts.execute();
	}



//	List all the Bts
	public class ListAllBts extends AsyncTask<String, String, List<BtsMasterModal>> {
		private final WeakReference<SearchByBts> activityReference1;
		private ListAllBts(SearchByBts context){
			activityReference1 = new WeakReference<>(context);
		}
		ProgressDialog pd;

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(activityReference1.get());
		pd.setTitle("Get Btss");
		pd.setMessage("Geting the bts details from database...");
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.show();
	}

	@Override
		protected void onPostExecute(List<BtsMasterModal> btsMasterModals) {
		  pd.dismiss();
			SearchByBts activity = activityReference1.get();
			List<String> btsList = new ArrayList<>();
			HashMap<String, BtsMasterModal> btsMasterModalHashMap = new HashMap<>();
			for(BtsMasterModal bm: btsMasterModals){
				btsList.add(bm.toString());
				btsMasterModalHashMap.put(bm.toString(), bm);
			}
			Integer btsMasterSize = btsMasterModalHashMap.size();
			if(btsMasterSize==0){
				AlertDialog.Builder builder = new AlertDialog.Builder(activity)
						.setTitle("Sync Master Data")
						.setMessage("The bts count is zero and need to Sync with the database\nGo to Navigation menu and click the \"Sync BTS Master \" ,the process will take 1-2 minutes based on network conditions\n\nClick Ok to sync Bts Master")
						.setPositiveButton("ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
//                Sync the bts master data from server over phone
                DatabaseHelper databaseHelper = new DatabaseHelper(SearchByBts.this);
                OkHttpClient client = ApiAdapter.getUnsafeOkHttpClient();
                //    Using retrofit
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://61.0.234.2/cnmc/v1/")
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

//                Creating the body for api call
                JSONObject obj = new JSONObject();
                try {
                  obj.put("msisdn", sharedPreferences.getString("msisdn", ""));
                  obj.put("circle_id", sharedPreferences.getString("circle_id",""));
                } catch (JSONException e) {
                  e.printStackTrace();
                }

                MyInterface myInterface = retrofit.create(MyInterface.class);
                Call<ApiResponse> call_bts = myInterface.getBtsDetails(sharedPreferences.getString("web_token",""), obj.toString());

                pd = new ProgressDialog(SearchByBts.this);
                pd.setTitle("Syncing BTS Details ");
                pd.setMessage("Data started syncing kindly wait for 1-2 minutes to sync from database");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(false);
                pd.show();
                call_bts.enqueue(new Callback<ApiResponse>() {
                  @Override
                  public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    try {
                      JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                      JSONArray arr = new JSONArray(jsonObject.getString("rows"));
                      List<BtsMasterModal> btsMasterModalList = new ArrayList<>();
                      for(int i=0; i<arr.length(); i++){
                        JSONObject arr_obj = arr.getJSONObject(i);
                        BtsMasterModal btsMasterModal = new BtsMasterModal(arr_obj.getString("bts_id"),arr_obj.getString("bts_name"),
                          arr_obj.getString("bts_type"), arr_obj.getString("ssa_id"),
                          arr_obj.getString("site_type"), arr_obj.getString("operator_name"));
                        btsMasterModalList.add(btsMasterModal);
                       }
                      databaseHelper.addAll(btsMasterModalList);
                      pd.dismiss();
                      Toast.makeText(SearchByBts.this, "Sucessfully Synced BTS Master", Toast.LENGTH_SHORT).show();
                      } catch (JSONException e) {
                        e.printStackTrace();
                      }

                  }

                  @Override
                  public void onFailure(Call<ApiResponse> call, Throwable t) {
//                    System.out.println(t.getMessage());
                    Toast.makeText(SearchByBts.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                  }
                });

							}
						});
				builder.setIcon(R.mipmap.ic_launcher);
				builder.show();
        return;
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.my_bts_list_view,
					R.id.textview, btsList);
			activity.listView.setAdapter(adapter);
			activity.listView.setTextFilterEnabled(true);
			activity.listView.setClickable(true);

			activity.listView.setOnItemClickListener((adapterView, view, i, l) -> {
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				String clickedItem  = (String) activity.listView.getItemAtPosition(i);
				BtsMasterModal bm = btsMasterModalHashMap.get(clickedItem);
				Uri.Builder uri_builder = new Uri.Builder()
						.scheme("https")
						.authority(Constants.getSecureBaseUrl())
						.appendPath(activity.getString(R.string.ulr_get_bts_info));

				GetBtsInfo getBtsInfo = new GetBtsInfo(activity);
				getBtsInfo.execute(uri_builder.toString(), Objects.requireNonNull(bm).getBts_id());
			});

			activity.ed_search_bts.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void afterTextChanged(Editable editable) {
					List<String> filteredList = new ArrayList<>();
					for(String item: btsList){
						if(item.toLowerCase().contains(editable)){
							filteredList.add(item);
						}
					}
//                dataModels = filteredList;
					ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.my_bts_list_view,
							R.id.textview, filteredList);
					activity.listView.setAdapter(adapter);
				}
			});
		}

		@Override
		protected List<BtsMasterModal> doInBackground(String... strings) {
			SearchByBts activity = activityReference1.get();
			DatabaseHelper databaseHelper = new DatabaseHelper(activity);

			return databaseHelper.getEveryOne();
		 }
  }

	public static class GetBtsInfo extends AsyncTask<String, String, String>{
		private final WeakReference<SearchByBts> activityReference2;
		private GetBtsInfo(SearchByBts context){
			activityReference2 = new WeakReference<>(context);
		}
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(activityReference2.get());
			pd.setTitle("Get BTS Info");
			pd.setMessage("Getting the Bts Info from the server...");
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(false);
			pd.show();

		}

		@Override
		protected void onPostExecute(String s) {
			pd.dismiss();
			SearchByBts activity = activityReference2.get();
//			System.out.println(s);
			try {
				JSONObject obj = new JSONObject(s);
				JSONObject data = new JSONObject(obj.getString("data"));
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("BTS Info");
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setCancelable(false);
				View layoutInflater = activity.getLayoutInflater().inflate(R.layout.bts_info, null, true);
				List<TextView> textViews = new ArrayList<>();
				TextView tv_btsname = layoutInflater.findViewById(R.id.bts_name);
				tv_btsname.setText(data.getString("bts_name"));
				textViews.add(tv_btsname);
				TextView tv_btstype = layoutInflater.findViewById(R.id.bts_type);
				tv_btstype.setText(data.getString("bts_type"));
				textViews.add(tv_btstype);
        TextView tv_sitetype = layoutInflater.findViewById(R.id.site_type);
        tv_sitetype.setText(data.getString("site_type"));
				textViews.add(tv_sitetype);
				TextView tv_circlename = layoutInflater.findViewById(R.id.circle_name);
				tv_circlename.setText(data.getString("circle_name"));
				textViews.add(tv_circlename);
				TextView tv_ssaname = layoutInflater.findViewById(R.id.ssa_name);
				tv_ssaname.setText(data.getString("ssa_name"));
				textViews.add(tv_ssaname);
				TextView tv_vendorname = layoutInflater.findViewById(R.id.vendor_name);
				tv_vendorname.setText(data.getString("vendor_name"));
				textViews.add(tv_vendorname);
				TextView tv_sitecategory = layoutInflater.findViewById(R.id.site_category);
				tv_sitecategory.setText(data.getString("site_category"));
				textViews.add(tv_sitecategory);
        TextView tv_btsstatus = layoutInflater.findViewById(R.id.bts_status);
        tv_btsstatus.setText(data.getString("bts_status"));
				textViews.add(tv_btsstatus);
				TextView tv_btsstatusdt = layoutInflater.findViewById(R.id.bts_status_dt);
				tv_btsstatusdt.setText(data.getString("bts_status_dt"));
				textViews.add(tv_btsstatus);
				TextView tv_btslatlng = layoutInflater.findViewById(R.id.bts_lat_lng);
				tv_btslatlng.setText(data.getString("bts_latitude")+","+data.getString("bts_longitude"));
				textViews.add(tv_btslatlng);
				TextView tv_faulttype = layoutInflater.findViewById(R.id.fault_type);
				tv_faulttype.setText(data.getString("fault_type"));
				textViews.add(tv_faulttype);
				CardView cv = layoutInflater.findViewById(R.id.card_view);
				if(!data.getString("bts_status").equals("Site UP")){
					cv.setBackgroundColor(Color.RED);
				} else {
					cv.setBackgroundColor(Color.GREEN);
					for(int i=0; i<textViews.size(); i++){
						if(textViews.get(i) instanceof TextView){
							textViews.get(i).setTextColor(Color.BLACK);
						}
					}
				}



				builder.setView(layoutInflater)
						.setPositiveButton("ok", (dialogInterface, i) -> dialogInterface.dismiss());
				builder.create();
				builder.show();
			} catch (JSONException e) {
				e.printStackTrace();
			}



		}

		@Override
		protected String doInBackground(String... strings) {
			SearchByBts activity = activityReference2.get();
			try {
				JSONObject post_obj = new JSONObject();
				post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
				post_obj.put("bts_id", strings[1] );
				System.out.println(strings[1]);
				return new MyHttpClient(activity).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
						post_obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}


}