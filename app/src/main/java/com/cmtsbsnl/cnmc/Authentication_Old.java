package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Authentication_Old extends AppCompatActivity {

    public static final int MULTIPLE_PERMISSIONS = 10;
    private static final String TAG = "Permissions";
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
//        Source :https://medium.com/mindorks/multiple-runtime-permissions-in-android-without-any-third-party-libraries-53ccf7550d0
        if (checkAndRequestPermissions()) {
            initApp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initApp() {
        if(checkInternetConnection(this)) {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Authentication_Old.this, new OnSuccessListener<InstanceIdResult>() {
//                    @Override
//                    public void onSuccess(InstanceIdResult instanceIdResult) {
//                        String newToken = instanceIdResult.getToken();
//                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("CnmcPref", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.putString("FCMId", newToken);
//                        editor.commit();
//                        Log.e("newToken", newToken);
//                    }
//            });
//            String imsi = null;
            String imsi_1 = "";
            String imei_1 = "";

            try {
                Method getSubId = TelephonyManager.class.getMethod("getSubscriberId", int.class);
                SubscriptionManager sm = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
                try {
                    if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
//                    imsi = (String) getSubId.invoke(tm, sm.getActiveSubscriptionInfoForSimSlotIndex(0).getSubscriptionId());
                } catch (Exception ne){
//                    imsi = "";
                }
                /*try {
                    imsi_1 = (String) getSubId.invoke(tm, sm.getActiveSubscriptionInfoForSimSlotIndex(1).getSubscriptionId());
                } catch (Exception ne){
                    imsi_1 ="";
                }
                try{
                    imei_1 = tm.getImei(1);
                } catch (Exception ne){
                    imei_1="";
                }*/

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            final String imsi = tm.getSubscriberId();
            final String imei = tm.getImei();
            final String msisdn = tm.getLine1Number();
            String token = getApplicationContext().getSharedPreferences("CnmcPref", Context.MODE_PRIVATE).getString("FCMId","");
            Log.i("Imsi Number", imsi + "," +imsi_1+","+ imei +","+imei_1+"," + msisdn + "," + "");
            String access_key = MD5.getMd5(imsi + imei+ UUID.randomUUID().toString());
//            Storing all the data into shared preference
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("CnmcPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("FCMId", token);
            editor.putString("imsi",imsi);
            editor.putString("imei",imei);
            editor.putString("imsi_1", imsi_1);
            editor.putString("imei_1",imei_1);
            editor.commit();

//            String token="";
            TestHttpAuth auth = new TestHttpAuth(this);
            try{
                String result = (String) auth.execute(imsi, imsi_1, imei, imei_1, msisdn, token, access_key).get();
                JSONObject obj = new JSONObject(result);
                String obj_result = obj.getString("result");
                if(obj_result.contains("true")){
                    JSONObject obj_output = obj.getJSONObject("remarks");
                    Log.i("User_Details", obj_output.toString());
                    String update_profile = obj_output.get("update_profile").toString();
                    if(update_profile.equals("Y")) {
                        startActivity(new Intent(Authentication_Old.this, Navigational.class));
                        editor.putString("name", obj_output.getString("name"));
                        editor.putString("email", obj_output.getString("email"));
                        editor.putString("cricle", obj_output.getString("circle"));
                        editor.commit();
                    } else {
                        startActivity(new Intent(Authentication_Old.this, NewUserCreation.class));
                    }
                } else{
                    Log.i("result_1", obj_result);

                }
//                startActivity(new Intent(Authentication_Old.this, Navigational.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
//            Exit the app when there is no internet connection
            Toast.makeText(getApplicationContext(), "Unable to connect to Server ", Toast.LENGTH_SHORT).show();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    exitApp();
                }
            }, 2000);
        }

    }

    public static Boolean checkInternetConnection(Context ctx){
        ConnectivityManager con_manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (con_manager.getActiveNetworkInfo() !=null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

    public void exitApp(){
        finishAffinity();
        System.exit(0);
    }

    private  boolean checkAndRequestPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        System.out.println(listPermissionsNeeded);
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    // Check for both permissions
                    boolean fine_location = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean phone_state = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if ( fine_location && phone_state) {
                        Log.d(TAG, "Phone State & location services permission granted");
                        initApp();
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                ||ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_PHONE_STATE)) {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
            break;
        }
    }
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
