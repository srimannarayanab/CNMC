package com.cmtsbsnl.cnmc;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public class ApiResponse {
  @SerializedName("result")
  private String result;
  @SerializedName("error")
  private String error;
  @SerializedName("rows")
  private List<HashMap<String,String>> rows;


  public ApiResponse(String result,  List<HashMap<String, String>> rows, String error) {
    this.result = result;
    this.error = error;
    this.rows = rows;
  }

  public String getResult() {
    return result;
  }

  public String getError() {
    return error;
  }

  public List<HashMap<String, String>> getRows() {
    return rows;
  }
}
