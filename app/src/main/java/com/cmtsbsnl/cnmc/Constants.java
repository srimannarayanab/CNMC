package com.cmtsbsnl.cnmc;


import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Constants {
  //    public static final String CHANNEL_ID = "cnmc";
//    public static final String CHANNEL_NAME="cnmc Notifications";
//    public static final String CHANNEL_DESCRIPTION="Notifications of cnmc";
  public static final String SERVER_IP=BuildConfig.SERVER_IP;

  static {
    System.loadLibrary("keys");
  }

  public static native String getBaseURL();
  public static native String getAPIUsername();
  public static native String getAPIPassword();
  public static native String getAuth();

  public static String getServerIP() {
    return SERVER_IP;
  }

  public static String getBasiAuth(){
    String api_auth = getAPIUsername()+":"+getAPIPassword();
    byte[] auth = Base64.encode(api_auth.getBytes(), Base64.DEFAULT);
    return "Basic "+new String(auth);
  }


  public static String getSecureBaseUrl() {
    String mUrl = getBaseURL();
    return new String(Base64.decode(mUrl, Base64.DEFAULT), StandardCharsets.UTF_8);
//        mUrl = "http://demo.example.com/";
//        return mUrl;
  }

  public static String getBtsDownInfo(JSONObject obj, HashMap<String,String> operators){
    final SimpleDateFormat sfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    try {
      String optr_id = obj.getString("operator_id");
      String opr_name = Config.getOperatorNames(operators, optr_id);
      if(obj.has("operator_names")){
        opr_name = obj.getString("operator_names");
      } else {
//                System.out.println(optr_id + "->" + opr_name);
        if (opr_name.equals("Not Applicable")) {
          opr_name = "";
        } else {
          opr_name = " - " + opr_name;
        }
      }
//
      String reason = !obj.getString("fault_type").equals("null") ? obj.getString("fault_type") : "";
      StringBuilder str = new StringBuilder();
      str.append("Bts Name :").append(obj.getString("bts_name"));
      str.append("\n");
      str.append("Bts Loc :").append(obj.optString("bts_location",""));
      str.append("\n");
      str.append("Bts Site ID :").append(obj.optString("bts_site_id",""));
      str.append("\n");
      str.append("Bts Type :").append(obj.getString("bts_type")).append("-").append(obj.getString("sitetype"));
      str.append("\n");
      if(opr_name.equals("Not Applicable")){
        str.append(obj.getString("ssa_name")).append("-").append(obj.getString("vendor_name"));
      } else {
        str.append(obj.getString("ssa_name")).append("-").append(obj.getString("vendor_name")).append("-").append(opr_name);
      }
      str.append("\n");
      str.append("Down Time: ").append(obj.getString("bts_status_dt"));
      str.append("\n");
      str.append("Cumulative Down Time :").append(Config.calculateTime(obj.getInt("cumm_down_time")));
      str.append("\n");
      str.append("Site Category :").append(obj.getString("site_category"));
      if (!obj.getString("outsrc_name").equalsIgnoreCase("NOT APPLICABLE")) {
        str.append("\n");
        str.append("OutSourced :").append(obj.getString("outsrc_name"));
      }
      if (!obj.getString("fault_updated_by").equals("null")) {
        if (Objects.requireNonNull(sfdt.parse(obj.getString("bts_status_dt"))).before(sfdt.parse(obj.getString("fault_update_date")))) {
          str.append("\n");
          str.append("Reason :").append(reason);
          str.append("\n");
          str.append("updated_by :").append(obj.getString("fault_updated_by"));
          str.append("\n");
          str.append("updated_date:").append(obj.getString("fault_update_date"));
        }
      }
      return str.toString();
    }catch (JSONException | ParseException e){
      e.printStackTrace();
    }
    return null;
  }

  public static String getCurrentTime(){
    return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.getDefault()).format(new Date());

  }

  public static String addZeroWidthSpaces(String input, int interval){
    StringBuilder result = new StringBuilder();
    for(int i=0 ; i< input.length();i++){
      result.append(input.charAt(i));
      if((i+1) % interval == 0){
        result.append("\u200B");
      }
    }
    return result.toString();
  }
}
