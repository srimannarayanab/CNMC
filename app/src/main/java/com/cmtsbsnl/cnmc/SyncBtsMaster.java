package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class SyncBtsMaster extends Fragment {

  private SharedPreferences sharedPreferences;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View rootview = inflater.inflate(R.layout.activity_sync_bts_master, container, false);

    try {
      sharedPreferences = new Preferences(this.getContext()).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(rootview.getContext())
        .setTitle("Syncing the Master data")
        .setMessage("Syncing the master data from cnmc master table")
        .setPositiveButton("Sync", (dialogInterface, i) -> {
					Uri.Builder uri_builder = new Uri.Builder()
							.scheme("https")
							.authority(Constants.getSecureBaseUrl())
							.appendPath(getString(R.string.ulr_get_bts_details));

					GetBtsDetails getBtsDetails = new GetBtsDetails(SyncBtsMaster.this);
					getBtsDetails.execute(uri_builder.toString());

				})
        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
    builder.show();
    return rootview;
  }

  public static class GetBtsDetails extends AsyncTask<String, String, String> {
		private final WeakReference<SyncBtsMaster> activityReference;
		ProgressDialog pd;
		private GetBtsDetails(SyncBtsMaster context){
			activityReference = new WeakReference<>(context);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(activityReference.get().getContext());
			pd.setTitle("Syncing BTS Details ");
			pd.setMessage("Data started syncing kindly wait for 1-2 minutes to sync from database");
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected void onPostExecute(String s) {
//			System.out.println(s);
			SyncBtsMaster activity = activityReference.get();
			DatabaseHelper databaseHelper = new DatabaseHelper(activity.getContext());
			try {
				List<BtsMasterModal> btsMasterModalList = new ArrayList<>();
				JSONObject bts_obj = new JSONObject(s);
				if(bts_obj.getString("result").equals("true")){
					JSONArray arr = new JSONArray(bts_obj.getString("rows"));
					for(int i=0;i<arr.length(); i++){
						JSONObject arr_obj = arr.getJSONObject(i);
						BtsMasterModal btsMasterModal = new BtsMasterModal(arr_obj.getString("bts_id"),arr_obj.getString("bts_name"),
                          arr_obj.getString("bts_type"), arr_obj.getString("ssa_id"),
                arr_obj.getString("site_type"), arr_obj.getString("operator_name"));
						btsMasterModalList.add(btsMasterModal);
					}
					databaseHelper.addAll(btsMasterModalList);
					pd.dismiss();

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.getContext())
							.setIcon(R.mipmap.ic_launcher)
							.setTitle("Sync Bts Master")
							.setMessage("Sucessfully completed master data sync")
							.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
									Intent intent = new Intent(activity.getActivity(), SearchByBts.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									activity.getContext().startActivity(intent);

								}
							});
					builder.show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... strings) {
			SyncBtsMaster activity = activityReference.get();
			try {
				JSONObject post_obj = new JSONObject();
				post_obj.put("msisdn", activity.sharedPreferences.getString("msisdn",""));
				post_obj.put("circle_id", activity.sharedPreferences.getString("circle_id",""));
				return new MyHttpClient(activity.getContext()).getUrlConnection(URLDecoder.decode(strings[0], "UTF-8"),
						post_obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}