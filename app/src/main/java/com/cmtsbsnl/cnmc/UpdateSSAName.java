package com.cmtsbsnl.cnmc;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UpdateSSAName extends AsyncTask<String, String, String> {

    private Context context;
    private final ProgressDialog pd;

    public UpdateSSAName(Context context) {
        this.context = context;
        pd = new ProgressDialog(this.context);
    }

    @Override
    protected void onPostExecute(String s) {
        pd.dismiss();
        Toast.makeText(this.context, "SSA Name updated sucessfully", Toast.LENGTH_SHORT).show();
//        try {
//            JSONObject obj = new JSONObject(s);
//            String result = obj.getString("result");
//            String remarks = obj.getString("remarks");
//            if(result.equals("true")){
//                alertDialog.setTitle("CNMC Alert ....");
//                alertDialog.setMessage(remarks);
//                alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
//                        (dialog, which) -> {
////                            this.context.finish();
//                            System.exit(0);
//                        });
//            } else{
//                String alert = remarks;
//                if(remarks.startsWith("Duplicate")){
//                    alert = "Already User exists";
//                }
//                alertDialog.setTitle("CNMC Alert....");
//                alertDialog.setMessage(alert);
//                alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "OK",
//                        (dialog, which) -> dialog.dismiss());
//            }
//            alertDialog.show();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onPreExecute() {
        pd.setMessage("Updation of SSA Name is in progress");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String ssaname = strings[0];
        String username = strings[1];
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(Constants.getSecureBaseUrl())
                .appendPath("updateSSAName");
        try {
            JSONObject pos_obj = new JSONObject();
            pos_obj.put("ssaname", ssaname);
            pos_obj.put("username", username);
            pos_obj.put("auth", MD5.getMd5(Constants.getAuth()));
            return new MyHttpClient(context).getUrlConnection(URLDecoder.decode(builder.toString(), "UTF-8"), pos_obj.toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
