package com.cmtsbsnl.cnmc;

public class CircleUserModel {
    private final String msisdn;
    private final String name;
    private final String desg;
    private final String email;
    private final String hrms_no;
    private final String circle;
    private final String circle_id;
    private final String last_login;
    private final String app_version;
    private final String lvl;
    private final String lvl2;
    private final String lvl3;

    public CircleUserModel(String msisdn, String name, String desg, String email, String hrms_no, String circle,
                           String circle_id, String last_login, String app_version, String lvl, String lvl2, String lvl3) {
        this.msisdn = msisdn;
        this.name = name;
        this.desg = desg;
        this.email = email;
        this.hrms_no = hrms_no;
        this.circle = circle;
        this.circle_id = circle_id;
        this.last_login = last_login;
        this.app_version = app_version;
        this.lvl = lvl;
        this.lvl2 = lvl2;
        this.lvl3 = lvl3;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getName() {
        return name;
    }

    public String getDesg() {
        return desg;
    }

    public String getEmail() {
        return email;
    }

    public String getHrms_no() {
        return hrms_no;
    }

    public String getCircle() {
        return circle;
    }

    public String getCircle_id() {
        return circle_id;
    }

    public String getLast_login() {
        return last_login;
    }

    public String getApp_version() {
        return app_version;
    }

    public String getLvl() {
        return lvl;
    }

    public String getLvl2() {
        return lvl2;
    }

    public String getLvl3() {
        return lvl3;
    }
}
