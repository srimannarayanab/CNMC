package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class GenerateCardString {
  private final SimpleDateFormat sfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  private final Context context;

  // Constructor to pass Context
  public GenerateCardString(Context context) {
    this.context = context;
  }
  public SpannableStringBuilder CardString(JSONObject obj, String opr_name){
    try {
//      System.out.println(obj.toString());
      obj.getString("fault_type");
      String reason = !obj.getString("fault_type").isEmpty()
          ? obj.getString("fault_type")
          : "";
      if(opr_name.equals("Not Applicable")){
        opr_name="";
      } else {
        opr_name=" - "+opr_name;
      }

      SpannableStringBuilder spannableStr = new SpannableStringBuilder();

// Append and style "Bts Name"
      spannableStr.append("Bts Name: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      StringBuilder btsname_str = new StringBuilder();
      String b_name = obj.getString("bts_name");
      String rpid = "";
//      Log.d("API Response", obj.toString());

      if(obj.getString("circle_id").equals("AP")) {
        try {
          String bts_site_id = obj.getString("bts_site_id");
          if (bts_site_id.startsWith("T4") && bts_site_id.length() == 20) {
            rpid = bts_site_id.substring(14, 20);
          } else if (bts_site_id.startsWith("T4") && bts_site_id.length() > 20) {
            rpid = obj.getString("bts_ip_id");
          }
        } catch (Exception e) {
          Log.e("TCS 4G ID Missing", "BTS_SITE is missing");
        }

        if (b_name.contains(rpid)) {
          btsname_str.append(b_name);
        } else {
          btsname_str.append(b_name).append("_").append(rpid);
        }
        spannableStr.append(addZeroWidthSpaces(btsname_str.toString().replaceAll("_$", ""),25));
      } else {
        spannableStr.append(addZeroWidthSpaces(b_name.replaceAll("_$", ""),25));
      }

      spannableStr.append("\n");

// Append and style "Bts Location"
      spannableStr.append("Bts Location: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.append(addZeroWidthSpaces(obj.optString("bts_location", ""),25));
      spannableStr.append("\n");

// Append and style "Bts Site ID"
      spannableStr.append("Bts Site ID: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.append(obj.optString("bts_site_id", ""));
      spannableStr.append("\n");

// Append and style "Bts Type"
      spannableStr.append("Bts Type: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      int btstype_start = spannableStr.length();
      StringBuilder btstype_str = new StringBuilder();
      btstype_str.append(obj.getString("bts_type"))
          .append("/")
          .append(obj.getString("sitetype"))
          .append("/")
          .append(obj.getString("ssa_name"))
          .append("/")
          .append(obj.getString("vendor_name"))
          .append(opr_name);
      if (obj.getString("bts_site_id").startsWith("T4")) {
        String bts_site_id = obj.getString("bts_site_id");
        String band = bts_site_id.substring(4,6);
        boolean sa_proj = obj.getString("bts_site_id").toLowerCase().endsWith("sa");
        boolean lw_proj = obj.getString("bts_site_id").toLowerCase().endsWith("lw");
        btstype_str.append("/").append("B-").append(band);
        if(sa_proj){
          btstype_str.append("/").append("SA");
        } else if(lw_proj){
          btstype_str.append("/").append("LW");
        }
      }
      spannableStr.append(addZeroWidthSpaces(btstype_str.toString(),25));
      int btstype_end = spannableStr.length();
      spannableStr.setSpan(new StyleSpan(Typeface.BOLD), btstype_start, btstype_end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//      spannableStr.setSpan(new BackgroundColorSpan(Color.RED), btstype_start, btstype_end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//      spannableStr.setSpan(new ForegroundColorSpan(Color.WHITE), btstype_start, btstype_end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new UnderlineSpan(), btstype_start, btstype_end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

      spannableStr.append("\n");

//  If BTS is of Tejas
//      try {
//
//        if (obj.getString("bts_site_id").startsWith("T4")) {
//          String bts_site_id = obj.getString("bts_site_id");
//          String band = bts_site_id.substring(4,6);
//          boolean sa_proj = obj.getString("bts_site_id").toLowerCase().endsWith("sa");
////          Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_tcs_4g);
////          if (drawable != null) {
////            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
////          }
//          spannableStr.append(" "); // Placeholder for the image
////          Drawable drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery);
////          Log.d("ImageSpan", "Drawable: " + drawable);
////          Log.d("ImageSpan", "SpannableString length: " + spannableStr.length());
////
////          spannableStr.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), start_img, start_img + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//          try {
//
//            spannableStr.append(" ");
//            int start_img = spannableStr.length() -1;
//            int end_img = spannableStr.length();
//
//
////            if(sa_proj){
////              Drawable drawable = AppCompatResources.getDrawable(context, R.mipmap.ic_tcs_4g_sa);
////              assert drawable != null;
////              drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
////              spannableStr.setSpan(new ImageSpan(drawable), start_img, end_img, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////            } else {
////              Drawable drawable = AppCompatResources.getDrawable(context, R.mipmap.ic_tcs_4g_1);
////              assert drawable != null;
////              drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
////              spannableStr.setSpan(new ImageSpan(drawable), start_img, end_img, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////            }
//          } catch (Exception e) {
//            Log.e("GenerateCardString", "Error in ImageSpan logic", e);
//          }
//
////          try {
////            if(band.equals("01")) {
////              Drawable drawable_b_01 = AppCompatResources.getDrawable(context, R.mipmap.ic_tcs_4g_b01);
////              spannableStr.append(" ");
////              int start_img = spannableStr.length() - 1;
////              int end_img = spannableStr.length();
////              assert drawable_b_01 != null;
////              drawable_b_01.setBounds(0, 0, drawable_b_01.getIntrinsicWidth(), drawable_b_01.getIntrinsicHeight());
//////            drawable.setBounds(0,0, 100, 100);
////              spannableStr.setSpan(new ImageSpan(drawable_b_01), start_img, end_img, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////            } else if(band.equals("28")){
////              Drawable drawable_b_28 = AppCompatResources.getDrawable(context, R.mipmap.ic_tcs_4g_b28);
////              spannableStr.append(" ");
////              int start_img = spannableStr.length() - 1;
////              int end_img = spannableStr.length();
////              assert drawable_b_28 != null;
////              drawable_b_28.setBounds(0, 0, drawable_b_28.getIntrinsicWidth(), drawable_b_28.getIntrinsicHeight());
//////            drawable.setBounds(0,0, 100, 100);
////              spannableStr.setSpan(new ImageSpan(drawable_b_28), start_img, end_img, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////            } else if(band.equals("41")){
////              Drawable drawable_b_41 = AppCompatResources.getDrawable(context, R.mipmap.ic_tcs_4g_b41);
////              spannableStr.append(" ");
////              int start_img = spannableStr.length() - 1;
////              int end_img = spannableStr.length();
////              assert drawable_b_41 != null;
////              drawable_b_41.setBounds(0, 0, drawable_b_41.getIntrinsicWidth(), drawable_b_41.getIntrinsicHeight());
//////            drawable.setBounds(0,0, 100, 100);
////              spannableStr.setSpan(new ImageSpan(drawable_b_41), start_img, end_img, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////            }
////
////          } catch (Exception e) {
////            Log.e("GenerateCardString", "Error in ImageSpan logic", e);
////          }
////          int start_btsid = spannableStr.length();
////          spannableStr.append(" TCS").append(" B-").append(band);
////          spannableStr.append(" ");
////          int end_btsid = spannableStr.length();
////          spannableStr.setSpan(new BackgroundColorSpan(Color.BLUE), start_btsid, end_btsid, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
////          spannableStr.setSpan(new ForegroundColorSpan(Color.WHITE), start_btsid, end_btsid, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
////          spannableStr.setSpan(new AbsoluteSizeSpan(16, true), start_btsid, end_btsid, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//          spannableStr.append("\n");
//        }
//      } catch (JSONException je){
//        throw new RuntimeException(je);
//      }

// Append and style "Down Time"
      spannableStr.append("Down Time: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      int start = spannableStr.length(); // Save the starting point
      spannableStr.append(obj.getString("bts_status_dt")); // Append the text
      int end = spannableStr.length(); // Save the ending point

// Apply styles and spans to the newly added text
      spannableStr.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new BackgroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new ForegroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.append("\n");

// Append and style "Cumulative Down Time"
      spannableStr.append("Cumulative Down Time: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      int start_cdt = spannableStr.length(); // Save the starting point
      spannableStr.append(Config.calculateTime(obj.getInt("cumm_down_time")));
      int end_cdt = spannableStr.length(); // Save the ending point
      spannableStr.setSpan(new BackgroundColorSpan(Color.RED), start_cdt, end_cdt, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new ForegroundColorSpan(Color.WHITE), start_cdt, end_cdt, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.setSpan(new UnderlineSpan(), start_cdt, end_cdt, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

      spannableStr.append("\n");

// Append and style "Site Category"
      spannableStr.append("Site Category: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableStr.append(obj.getString("site_category"));
      spannableStr.append("\n");

// Append and style "OutSourced" if applicable
      if (!obj.getString("outsrc_name").equals("NOT APPLICABLE")) {
        spannableStr.append("OutSourced: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStr.append(obj.getString("outsrc_name"));
        spannableStr.append("\n");
      }

// Append and style fault update details if applicable
      if (!obj.getString("fault_updated_by").equals("null")) {
        if (Objects.requireNonNull(sfdt.parse(obj.getString("bts_status_dt")))
            .before(sfdt.parse(obj.getString("fault_update_date")))) {
          spannableStr.append("Reason: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          spannableStr.append(reason);
          spannableStr.append("\n");

          spannableStr.append("Updated By: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          spannableStr.append(obj.getString("fault_updated_by"));
          spannableStr.append("\n");

          spannableStr.append("Updated Date: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          spannableStr.append(obj.getString("fault_update_date"));
          spannableStr.append("\n");
        }
      }

      return spannableStr;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

//  ZeroWith Spaces
  public String addZeroWidthSpaces(String input, int interval){
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


