package com.cmtsbsnl.cnmc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

public class HttpGet extends AsyncTask {
    Context context ;
    AlertDialog alertDialog;
    ProgressDialog pd;

    public HttpGet(Context ctx){
        context=ctx;
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        String url_name = (String) objects[0];
        String post_data = "";
        if(objects.length>1) {
            Log.i("Object_cnt", objects[1].toString());
            HashMap<String, String> x = (HashMap<String, String>) objects[1];
            Iterator<String> itr = x.keySet().iterator();
            while(itr.hasNext()){
                try {
                    String k = itr.next();
                    post_data += URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(x.get(k), "UTF-8")+"&";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("Post_data",post_data);

        String genurl = "http://" + Constants.SERVER_IP + "/cnmc/"+url_name+".php";
        try {
            URL url = new URL(genurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            if(objects.length>1){
                bufferedWriter.write(post_data);
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            conn.disconnect();
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
