package com.cmtsbsnl.cnmc;

public class TodayLoginsModel {
  private String name, circle_name, desg, email, msisdn, last_login;

  public TodayLoginsModel(String name, String circle_name, String desg, String email, String msisdn, String last_login) {
    this.name = name;
    this.circle_name = circle_name;
    this.desg = desg;
    this.email = email;
    this.msisdn = msisdn;
    this.last_login = last_login;
  }

  public String getName() {
    return name;
  }

  public String getCircle_name() {
    return circle_name;
  }

  public String getDesg() {
    return desg;
  }

  public String getEmail() {
    return email;
  }

  public String getMsisdn() {
    return msisdn;
  }

  public String getLast_login() {
    return last_login;
  }
}
