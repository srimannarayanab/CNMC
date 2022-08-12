package com.cmtsbsnl.cnmc;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Config {
    public static final String server_ip="61.0.234.2";
    public static final String encrypt ="";


    public static String getServerIp(){
        return server_ip;
    }

    public static String calculateTime(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
        return day +" Days "+ hours + ":" + minute + ":" + second;
    }

    public static String getOperatorNames(HashMap<String,String> optrnames, String optr_id){
        if(optrnames.containsKey(optr_id)){
            return optrnames.get(optr_id);
        } else{
            return "";
        }
    }

}
