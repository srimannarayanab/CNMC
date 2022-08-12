package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

public class ModifyUserType extends AsyncTask<String, Void, String> {
    private final Context context;
    private SharedPreferences sharedPreferences;

    public ModifyUserType(Context ctx){
        this.context = ctx;
    }
    @Override
    protected String doInBackground(String... strings) {
        try {
            sharedPreferences = new Preferences(this.context).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        try {
            String url = strings[0];
            String username = strings[1];
            String user_privs = strings[2];
            JSONObject post_obj = new JSONObject();
            post_obj.put("msisdn", sharedPreferences.getString("msisdn",""));
            post_obj.put("username", username);
            post_obj.put("user_privs", user_privs);
            post_obj.put("circle_id", sharedPreferences.getString("circle_id", ""));
            return new MyHttpClient(context).getUrlConnection(URLDecoder.decode(url,"UTF-8"), post_obj.toString() );

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
