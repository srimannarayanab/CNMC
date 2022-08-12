package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;

public class UpdateLevel extends AsyncTask<String, String, String> {
    private final String url;
    private final String username;
    private final String lvl;
    private final String lvl2;
    private final String lvl3;
    private final Context context;
    private SharedPreferences sharedPreferences;

    public UpdateLevel(Context ctx, UserLevelUpdate.MyTaskParams params) {
        this.context = ctx;
        this.url = params.url;
        this.username = params.username;
        this.lvl = params.lvl;
        this.lvl2 = params.lvl2;
        this.lvl3 = params.lvl3;
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            sharedPreferences = new Preferences(this.context).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject post_obj = new JSONObject();
            post_obj.put("msisdn", sharedPreferences.getString("msisdn", ""));
            post_obj.put("circle_id", sharedPreferences.getString("circle_id", ""));
            post_obj.put("username", username);
            post_obj.put("lvl", this.lvl);
            post_obj.put("lvl2", this.lvl2);
            post_obj.put("lvl3", this.lvl3);
            return new MyHttpClient(this.context).getUrlConnection(URLDecoder.decode(this.url,"UTF-8"), post_obj.toString() );
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
