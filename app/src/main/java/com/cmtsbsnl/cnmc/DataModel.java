package com.cmtsbsnl.cnmc;

public class DataModel {
    public String bts_name;
    public String bts_type;
    boolean checked;

    DataModel(String bts_name, String bts_type, boolean checked) {
        this.bts_name = bts_name;
        this.bts_type = bts_type;
        this.checked = checked;
    }

    public String getBts_name() {
        return bts_name;
    }

    public void setBts_name(String bts_name) {
        this.bts_name = bts_name;
    }

    public String getBts_type() {
        return bts_type;
    }

    public void setBts_type(String bts_type) {
        this.bts_type = bts_type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}

