package com.cmtsbsnl.cnmc;

public class MyBtsUserModel {
    private String name, circle_name, desg, email, site_count, msisdn;
    /*public MyBtsUserModel() {

    }*/

    public MyBtsUserModel(String name, String circle_name, String desg, String email, String site_count, String msisdn) {
        this.name = name;
        this.circle_name = circle_name;
        this.desg = desg;
        this.email = email;
        this.site_count = site_count;
        this.msisdn = msisdn;
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

    public String getSite_count() {
        return site_count;
    }
    public String getMsisdn() {
        return msisdn;
    }
}
