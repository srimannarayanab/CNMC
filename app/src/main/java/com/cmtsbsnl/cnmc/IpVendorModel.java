package com.cmtsbsnl.cnmc;

import com.google.gson.annotations.SerializedName;

public class IpVendorModel {
  @SerializedName("ip_vendor_name")
  private String ip_vendor_name;

  public IpVendorModel(String ip_vendor_name) {
    this.ip_vendor_name = ip_vendor_name;
  }

  public String getIp_vendor_name() {
    return ip_vendor_name;
  }
}
