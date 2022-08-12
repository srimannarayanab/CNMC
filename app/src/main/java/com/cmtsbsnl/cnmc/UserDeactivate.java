package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

public class UserDeactivate extends AsyncTask<String, Void, String> {
    private final Context context;
    private SharedPreferences sharedPreferences;
    private ProgressDialog pd;
    public UserDeactivate(Context ctx){
        this.context = ctx;
        pd = new ProgressDialog(this.context);
    }



    @Override
    protected void onPreExecute() {
        pd.setMessage("User deletion is in progress");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        pd.dismiss();
//        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            sharedPreferences = new Preferences(this.context).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
//        strUrl="http://"+Constants.SERVER_IP+"/cnmc/deactivateUser.php";
//        pref = activity.getSharedPreferences("CnmcPref", Context.MODE_PRIVATE);
//        msisdn = strings[0];
//        circle_id = pref.getString("circle_id","");
        try {
            String url = strings[0];
            String username = strings[1];
            JSONObject post_obj = new JSONObject();
            post_obj.put("msisdn", sharedPreferences.getString("msisdn",""));
            post_obj.put("username", username);
            return new MyHttpClient(context).getUrlConnection(URLDecoder.decode(url,"UTF-8"), post_obj.toString() );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
