package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.os.AsyncTask;

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

public class TestHttpAuth extends AsyncTask {
    Context context ;

    TestHttpAuth(Context ctx){context=ctx;}

    @Override
    protected Object doInBackground(Object[] objects) {
        String imsiurl = "http://" + Constants.SERVER_IP + "/cnmc/userAuth.php";
        String imsi = (String) objects[0];
        String imsi_1 = (String) objects[1];
        String imei = (String) objects[2];
        String imei_1 = (String) objects[3];
        String msisdn = (String) objects[4];
        String token = (String) objects[5];
        String access_key = (String) objects[6];
        try {
            URL url = new URL(imsiurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("imsi", "UTF-8") + "=" + URLEncoder.encode(imsi, "UTF-8") + "&"
                    + URLEncoder.encode("imsi_1", "UTF-8") + "=" + URLEncoder.encode(imsi_1, "UTF-8") + "&"
                    + URLEncoder.encode("imei", "UTF-8") + "=" + URLEncoder.encode(imei, "UTF-8") + "&"
                    + URLEncoder.encode("imei_1", "UTF-8") + "=" + URLEncoder.encode(imei_1, "UTF-8") + "&"
                    + URLEncoder.encode("msisdn", "UTF-8") + "=" + URLEncoder.encode(msisdn, "UTF-8") + "&"
                    + URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8") +"&"
                    +URLEncoder.encode("access_key","UTF-8")+"="+URLEncoder.encode(access_key,"UTF-8");
            bufferedWriter.write(post_data);
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
