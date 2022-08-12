package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GetCircles extends AsyncTask<String, Void, String> {
    public static Context ctx;
    public GetCircles(Context ctx) {
        GetCircles.ctx = ctx;
    }
    Uri.Builder uri_builder = new Uri.Builder()
            .scheme("https")
            .authority(Constants.getSecureBaseUrl())
            .appendPath("getCircles");

    @Override
    protected String doInBackground(String... strings) {
        JSONObject post_obj = new JSONObject();
        try {
            post_obj.put("auth", MD5.getMd5(Constants.getAuth()));
            return new MyHttpClient(ctx.getApplicationContext()).getUrlConnection(URLDecoder.decode(uri_builder.toString(), "UTF-8"), post_obj.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
