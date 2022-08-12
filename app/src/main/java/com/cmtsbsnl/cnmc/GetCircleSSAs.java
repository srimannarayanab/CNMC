package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GetCircleSSAs extends AsyncTask<String, Void, String> {
    public static Context ctx;
    public GetCircleSSAs(Context ctx) {
        GetCircleSSAs.ctx = ctx;
    }

    Uri.Builder uri_builder = new Uri.Builder()
            .scheme("https")
            .authority(Constants.getSecureBaseUrl())
            .appendPath("getCircleSSAs");

    @Override
    protected String doInBackground(String... strings) {
        try {
            String circle = strings[0];
            JSONObject post_obj = new JSONObject();
            post_obj.put("auth", MD5.getMd5(Constants.getAuth()));
            post_obj.put("circle", circle);
            return new MyHttpClient(ctx.getApplicationContext()).getUrlConnection(URLDecoder.decode(uri_builder.toString(), "UTF-8"),post_obj.toString());

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
