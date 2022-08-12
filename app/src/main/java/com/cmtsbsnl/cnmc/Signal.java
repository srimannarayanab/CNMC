package com.cmtsbsnl.cnmc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;

public class Signal extends Fragment {

//    ViewPager viewPager;
    public static Context ctx;
    public static final int PERMISSIONS = 10;
    private static final String TAG = "Permissions";
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.ctx = context;
    }

    public Signal() {

    }

//    @SuppressLint({"WrongViewCast", "MissingPermission", "NewApi"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_signal, container, false);
        if (!hasPermissions(this.ctx, permissions)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSIONS);
        }

//        viewPager = (ViewPager) rootview.findViewById(R.id.table_signal);
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = 0.0;
        double latitude =0.0;
        try {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } catch(NullPointerException ne){
            longitude =0.0;
            latitude =0.0;
        }
        TableRow.LayoutParams rowparams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);
        TableLayout tl = (TableLayout) rootview.findViewById(R.id.table_signal);
        rowparams.setMargins(5, 5, 5, 5);

        TableRow th = new TableRow(getActivity());
        th.setLayoutParams(rowparams);
        TextView tv0 = new TextView(getActivity());
        tv0.setText("Parameter");
        th.addView(tv0);

        TextView tv1 = new TextView(getActivity());
        tv1.setText("Sim-1");
        th.addView(tv1);

        TextView tv2 = new TextView(getActivity());
        tv2.setText("Sim-2");
        th.addView(tv2);
        tl.addView(th);
//        Get IMSI Details of the Carrier
        TableRow tr0 = new TableRow(getActivity());
        TextView tr0_0 = new TextView(getActivity());
        tr0_0.setText("IMSI");
        tr0.addView(tr0_0);

        TextView tr0_1 = new TextView(getActivity());
//        tr0_1.setText(tm.getSubscriberId());
        tr0_1.setText(getSimIMSI(0));
        tr0.addView(tr0_1);

        TextView tr0_2 = new TextView(getActivity());
        tr0_2.setText(getSimIMSI(1));
        tr0.addView(tr0_2);
        tl.addView(tr0);

//        Get the IMEI Details
        TableRow tr1 = new TableRow(getActivity());
        TextView tr1_0 = new TextView(getActivity());
        tr1_0.setText("IMEI");
        tr1.addView(tr1_0);
        TextView tr1_1 = new TextView(getActivity());
        tr1_1.setText(tm.getImei(0));
        tr1.addView(tr1_1);
        TextView tr1_2 = new TextView(getActivity());
        tr1_2.setText(tm.getImei(1));
        tr1.addView(tr1_2);
        tl.addView(tr1);

//        Get Line Number
        TableRow tr2 = new TableRow(getActivity());
        TextView tr2_0 = new TextView(getActivity());
        tr2_0.setText("Line Number");
        tr2.addView(tr2_0);
        TextView tr2_1 = new TextView(getActivity());
        tr2_1.setText(tm.getLine1Number());
        tr2.addView(tr2_1);
        TextView tr2_2 = new TextView(getActivity());
        tr2_2.setText(tm.getLine1Number());
        tr2.addView(tr2_2);
        tl.addView(tr2);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
                SubscriptionManager subManager = (SubscriptionManager) getActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                List<SubscriptionInfo> subscriptionInfoList=new ArrayList<>();
                subscriptionInfoList=subManager.getActiveSubscriptionInfoList();
//                Log.d(TAG ,subscriptionInfoList.toString());
//                Showing the details
                TableRow tr3 = new TableRow(getActivity());
                TextView tv3_0 = new TextView(getActivity());
                tv3_0.setText("MCC- MNC");
                tr3.addView(tv3_0);

                TextView tv3_1 = new TextView(getActivity());
                tv3_1.setText(subscriptionInfoList.get(0).getMcc()+"-"+subscriptionInfoList.get(0).getMnc());
                tr3.addView(tv3_1);

                TextView tv3_2 = new TextView(getActivity());
                if(subscriptionInfoList.size()>1) {
                    tv3_2.setText(subscriptionInfoList.get(1).getMcc() + "-" + subscriptionInfoList.get(1).getMnc());
                } else{
                    tv3_2.setText("");
                }
                tr3.addView(tv3_2);
                tl.addView(tr3);

//                Carrier Name

                TableRow tr4 = new TableRow(getActivity());
                TextView tv4_0 = new TextView(getActivity());
                tv4_0.setText("Carrier Name");
                tr4.addView(tv4_0);

                TextView tv4_1 = new TextView(getActivity());
                tv4_1.setText(subscriptionInfoList.get(0).getCarrierName());
                tr4.addView(tv4_1);

                TextView tv4_2 = new TextView(getActivity());
                if(subscriptionInfoList.size()>1) {
                    tv4_2.setText(subscriptionInfoList.get(1).getCarrierName());
                } else{
                    tv4_2.setText("");
                }
                tr4.addView(tv4_2);

                tl.addView(tr4);
//          SubscriberId
                TableRow tr5 = new TableRow(getActivity());
                TextView tv5_0 = new TextView(getActivity());
                tv5_0.setText("ICCID");
                tr5.addView(tv5_0);

                TextView tv5_1 = new TextView(getActivity());
                if(subscriptionInfoList.size()>=1) {
//                    tv5_1.setText(subscriptionInfoList.get(0).getIccId().substring(10));
                    tv5_1.setText("");
                } else {
                    tv5_1.setText("");
                }
//                tv5_1.setTextSize(11);
                tr5.addView(tv5_1);

                TextView tv5_2 = new TextView(getActivity());
                if(subscriptionInfoList.size()>1) {
                    tv5_2.setText("");
//                    tv5_2.setText(subscriptionInfoList.get(1).getIccId().substring(10));
                } else{
                    tv5_2.setText("");
                }
//                tv5_2.setTextSize(11);
                tr5.addView(tv5_2);
                tl.addView(tr5);
            }
            TableRow tr5_1 = new TableRow(getActivity());
            TextView tv5_1_0 = new TextView(getActivity());
            tv5_1_0.setText("Latitude");
            tr5_1.addView(tv5_1_0);

            TextView tv5_1_1 = new TextView(getActivity());
            tv5_1_1.setText(String.valueOf(latitude));
            tr5_1.addView(tv5_1_1);

            TextView tv5_1_2 = new TextView(getActivity());
            tv5_1_2.setText(String.valueOf(latitude));
            tr5_1.addView(tv5_1_2);

            TableRow tr5_2 = new TableRow(getActivity());
            TextView tv5_2_0 = new TextView(getActivity());
            tv5_2_0.setText("Longitude");
            tr5_2.addView(tv5_2_0);

            TextView tv5_2_1 = new TextView(getActivity());
            tv5_2_1.setText(String.valueOf(longitude));
            tr5_2.addView(tv5_2_1);

            TextView tv5_2_2 = new TextView(getActivity());
            tv5_2_2.setText(String.valueOf(longitude));
            tr5_2.addView(tv5_2_2);

            tl.addView(tr5_1);
            tl.addView(tr5_2);
        }
//      Cell informations
        List<CellInfo> ci = tm.getAllCellInfo();
        if(ci !=null ) {
            if(ci.size()>1){
                CellInfo info_sim1 = ci.get(0);
                CellInfo info_sim2 = ci.get(1);

                TableRow tr6 = new TableRow(getActivity());
                TextView tv6_0 = new TextView(getActivity());
                tv6_0.setText("Network Type");
                tr6.addView(tv6_0);

                TableRow tr7 = new TableRow(getActivity());
                TextView tv7_0 = new TextView(getActivity());
                tv7_0.setText("Lac");
                tr7.addView(tv7_0);

                TableRow tr8 = new TableRow(getActivity());
                TextView tv8_0 = new TextView(getActivity());
                tv8_0.setText("Cell-Id");
                tr8.addView(tv8_0);

                TableRow tr9 = new TableRow(getActivity());
                TextView tv9_0 = new TextView(getActivity());
                tv9_0.setText("(A/U/E)rfcn");
                tr9.addView(tv9_0);

                TableRow tr10 = new TableRow(getActivity());
                TextView tv10_0 = new TextView(getActivity());
                tv10_0.setText("BSIC");
                tr10.addView(tv10_0);

                TableRow tr11 = new TableRow(getActivity());
                TextView tv11_0 = new TextView(getActivity());
                tv11_0.setText("PSC");
                tr11.addView(tv11_0);

                TableRow tr12 = new TableRow(getActivity());
                TextView tv12_0 = new TextView(getActivity());
                tv12_0.setText("DBM");
                tr12.addView(tv12_0);

                TableRow tr13 = new TableRow(getActivity());
                TextView tv13_0 = new TextView(getActivity());
                tv13_0.setText("TAC");
                tr13.addView(tv13_0);

                TableRow tr14 = new TableRow(getActivity());
                TextView tv14_0 = new TextView(getActivity());
                tv14_0.setText("BandWidth(4G)");
                tr14.addView(tv14_0);

                TableRow tr15 = new TableRow(getActivity());
                TextView tv15_0 = new TextView(getActivity());
                tv15_0.setText("Physical CI 4G");
                tr15.addView(tv15_0);

                TableRow tr16 = new TableRow(getActivity());
                TextView tv16_0 = new TextView(getActivity());
                tv16_0.setText("Cell Identity(4G)");
                tr16.addView(tv16_0);

                if(info_sim1 instanceof CellInfoGsm && info_sim1.isRegistered()){
                    final CellSignalStrengthGsm gsm = ((CellInfoGsm) info_sim1).getCellSignalStrength();
                    final CellIdentityGsm identityGsm = ((CellInfoGsm) info_sim1).getCellIdentity();
                    // Network Type
                    TextView tv6_1 = new TextView(getActivity());
                    tv6_1.setText("GSM");
                    tr6.addView(tv6_1);
                    //  Lac
                    TextView tv7_1 = new TextView(getActivity());
                    tv7_1.setText(String.valueOf(identityGsm.getLac()));
                    tr7.addView(tv7_1);
                    //   Cellid
                    TextView tv8_1 = new TextView(getActivity());
                    tv8_1.setText(String.valueOf(identityGsm.getCid() & 0xffff));
                    tr8.addView(tv8_1);
                    // Arfcn
                    TextView tv9_1 = new TextView(getActivity());
                    tv9_1.setText(String.valueOf(identityGsm.getArfcn()));
                    tr9.addView(tv9_1);
                    // BSIC
                    TextView tv10_1 = new TextView(getActivity());
                    tv10_1.setText(String.valueOf(identityGsm.getBsic()));
                    tr10.addView(tv10_1);
                    // psc
                    TextView tv11_1 = new TextView(getActivity());
                    tv11_1.setText(String.valueOf(identityGsm.getPsc()));
                    tr11.addView(tv11_1);
                    // DBM
                    TextView tv12_1 = new TextView(getActivity());
                    tv12_1.setText(gsm.getDbm());
                    tr12.addView(tv12_1);
                    // TAC
                    TextView tv13_1 = new TextView(getActivity());
                    tv13_1.setText("-");
                    tr13.addView(tv13_1);
                    // BANDWIDTH
                    TextView tv14_1 = new TextView(getActivity());
                    tv14_1.setText("-");
                    tr14.addView(tv14_1);
                    // PCI
                    TextView tv15_1 = new TextView(getActivity());
                    tv15_1.setText("-");
                    tr15.addView(tv15_1);
                    // CEll Identity
                    TextView tv16_1 = new TextView(getActivity());
                    tv16_1.setText("-");
                    tr16.addView(tv16_1);

                } else if(info_sim1 instanceof CellInfoWcdma && info_sim1.isRegistered()){
                    final CellSignalStrengthWcdma wcdma= ((CellInfoWcdma) info_sim1).getCellSignalStrength();
                    final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info_sim1).getCellIdentity();
                    // Network Type
                    TextView tv6_1 = new TextView(getActivity());
                    tv6_1.setText("WCDMA-3G");
                    tr6.addView(tv6_1);
                    //   Lac
                    TextView tv7_1 = new TextView(getActivity());
                    tv7_1.setText(String.valueOf(identityWcdma.getLac()));
                    tr7.addView(tv7_1);
                    //   Cellid
                    TextView tv8_1 = new TextView(getActivity());
                    tv8_1.setText(String.valueOf(identityWcdma.getCid() & 0xffff));
                    tr8.addView(tv8_1);
                    // Arfcn
                    TextView tv9_1 = new TextView(getActivity());
                    tv9_1.setText(String.valueOf(identityWcdma.getUarfcn()));
                    tr9.addView(tv9_1);
                    // BSIC
                    TextView tv10_1 = new TextView(getActivity());
                    tv10_1.setText("-");
                    tr10.addView(tv10_1);
                    // PSC
                    TextView tv11_1 = new TextView(getActivity());
                    tv11_1.setText(String.valueOf(identityWcdma.getPsc()));
                    tr11.addView(tv11_1);
                    // DBM
                    TextView tv12_1 = new TextView(getActivity());
                    tv12_1.setText(String.valueOf(wcdma.getDbm()));
                    tr12.addView(tv12_1);
                    // TAC
                    TextView tv13_1 = new TextView(getActivity());
                    tv13_1.setText("-");
                    tr13.addView(tv13_1);
                    // BANDWIDTH
                    TextView tv14_1 = new TextView(getActivity());
                    tv14_1.setText("-");
                    tr14.addView(tv14_1);
                    // PCI
                    TextView tv15_1 = new TextView(getActivity());
                    tv15_1.setText("-");
                    tr15.addView(tv15_1);
                    // CEll Identity
                    TextView tv16_1 = new TextView(getActivity());
                    tv16_1.setText("-");
                    tr16.addView(tv16_1);
                } else if(info_sim1 instanceof CellInfoLte && info_sim1.isRegistered()){
                    final CellSignalStrengthLte lte= ((CellInfoLte) info_sim1).getCellSignalStrength();
                    final CellIdentityLte identityLte = ((CellInfoLte) info_sim1).getCellIdentity();
                    // Network Type
                    TextView tv6_1 = new TextView(getActivity());
                    tv6_1.setText("LTE");
                    tr6.addView(tv6_1);
                    //   Lac
                    TextView tv7_1 = new TextView(getActivity());
                    tv7_1.setText("-");
                    tr7.addView(tv7_1);
                    //   Cellid
                    TextView tv8_1 = new TextView(getActivity());
                    tv8_1.setText("-");
                    tr8.addView(tv8_1);
                    // Arfcn
                    TextView tv9_1 = new TextView(getActivity());
                    tv9_1.setText(String.valueOf(identityLte.getEarfcn()));
                    tr9.addView(tv9_1);
                    // BSIC
                    TextView tv10_1 = new TextView(getActivity());
                    tv10_1.setText("-");
                    tr10.addView(tv10_1);
                    // PSC
                    TextView tv11_1 = new TextView(getActivity());
                    tv11_1.setText("-");
                    tr11.addView(tv11_1);
                    // DBM
                    TextView tv12_1 = new TextView(getActivity());
                    tv12_1.setText(String.valueOf(lte.getDbm()));
                    tr12.addView(tv12_1);
                    // TAC
                    TextView tv13_1 = new TextView(getActivity());
                    tv13_1.setText(String.valueOf(identityLte.getTac()));
                    tr13.addView(tv13_1);
                    // BANDWIDTH
                    TextView tv14_1 = new TextView(getActivity());
                    tv14_1.setText(String.valueOf(identityLte.getBandwidth()));
                    tr14.addView(tv14_1);
                    // PCI
                    TextView tv15_1 = new TextView(getActivity());
                    tv15_1.setText(String.valueOf(identityLte.getPci()));
                    tr15.addView(tv15_1);
                    // CEll Identity
                    TextView tv16_1 = new TextView(getActivity());
                    tv16_1.setText(String.valueOf(identityLte.getCi()));
                    tr16.addView(tv16_1);
                }else {
                    // Network Type
                    TextView tv6_1 = new TextView(getActivity());
                    tv6_1.setText("-");
                    tr6.addView(tv6_1);
                    //   Lac
                    TextView tv7_1 = new TextView(getActivity());
                    tv7_1.setText("-");
                    tr7.addView(tv7_1);
                    //   Cellid
                    TextView tv8_1 = new TextView(getActivity());
                    tv8_1.setText("-");
                    tr8.addView(tv8_1);
                    // Arfcn
                    TextView tv9_1 = new TextView(getActivity());
                    tv9_1.setText("-");
                    tr9.addView(tv9_1);

                    TextView tv10_1 = new TextView(getActivity());
                    tv10_1.setText("-");
                    tr10.addView(tv10_1);

                    TextView tv11_1 = new TextView(getActivity());
                    tv11_1.setText("-");
                    tr11.addView(tv11_1);

                    TextView tv12_1 = new TextView(getActivity());
                    tv12_1.setText("-");
                    tr12.addView(tv12_1);

                    TextView tv13_1 = new TextView(getActivity());
                    tv13_1.setText("-");
                    tr13.addView(tv13_1);
                    // BANDWIDTH
                    TextView tv14_1 = new TextView(getActivity());
                    tv14_1.setText("-");
                    tr14.addView(tv14_1);
                    // PCI
                    TextView tv15_1 = new TextView(getActivity());
                    tv15_1.setText("-");
                    tr15.addView(tv15_1);
                    // CEll Identity
                    TextView tv16_1 = new TextView(getActivity());
                    tv16_1.setText("-");
                    tr16.addView(tv16_1);
                }

                // Sim2 Info

                if(info_sim2 instanceof CellInfoGsm && info_sim2.isRegistered()){
                    final CellSignalStrengthGsm gsm2 = ((CellInfoGsm) info_sim2).getCellSignalStrength();
                    final CellIdentityGsm identityGsm2 = ((CellInfoGsm) info_sim2).getCellIdentity();
                    // Network Type
                    TextView tv6_2 = new TextView(getActivity());
                    tv6_2.setText("GSM");
                    tr6.addView(tv6_2);
                    //  Lac
                    TextView tv7_2 = new TextView(getActivity());
                    tv7_2.setText(String.valueOf(identityGsm2.getLac()));
                    tr7.addView(tv7_2);
                    //   Cellid
                    TextView tv8_2 = new TextView(getActivity());
                    tv8_2.setText(String.valueOf(identityGsm2.getCid() & 0xffff));
                    tr8.addView(tv8_2);
                    // Arfcn
                    TextView tv9_2 = new TextView(getActivity());
                    tv9_2.setText(String.valueOf(identityGsm2.getArfcn()));
                    tr9.addView(tv9_2);
                    // BSIC
                    TextView tv10_2 = new TextView(getActivity());
                    tv10_2.setText(String.valueOf(identityGsm2.getBsic()));
                    tr10.addView(tv10_2);
                    // psc
                    TextView tv11_2 = new TextView(getActivity());
                    tv11_2.setText(String.valueOf(identityGsm2.getPsc()));
                    tr11.addView(tv11_2);
                    // DBM
                    TextView tv12_2 = new TextView(getActivity());
                    tv12_2.setText(gsm2.getDbm());
                    tr12.addView(tv12_2);
                    // TAC
                    TextView tv13_2 = new TextView(getActivity());
                    tv13_2.setText("-");
                    tr13.addView(tv13_2);
                    // BANDWIDTH
                    TextView tv14_2 = new TextView(getActivity());
                    tv14_2.setText("-");
                    tr14.addView(tv14_2);
                    // PCI
                    TextView tv15_2 = new TextView(getActivity());
                    tv15_2.setText("-");
                    tr15.addView(tv15_2);
                    // CEll Identity
                    TextView tv16_2 = new TextView(getActivity());
                    tv16_2.setText("-");
                    tr16.addView(tv16_2);

                } else if(info_sim2 instanceof CellInfoWcdma && info_sim2.isRegistered()){
                    final CellSignalStrengthWcdma wcdma2= ((CellInfoWcdma) info_sim2).getCellSignalStrength();
                    final CellIdentityWcdma identityWcdma2 = ((CellInfoWcdma) info_sim2).getCellIdentity();
                    // Network Type
                    TextView tv6_2 = new TextView(getActivity());
                    tv6_2.setText("WCDMA-3G");
                    tr6.addView(tv6_2);
                    //   Lac
                    TextView tv7_2 = new TextView(getActivity());
                    tv7_2.setText(String.valueOf(identityWcdma2.getLac()));
                    tr7.addView(tv7_2);
                    //   Cellid
                    TextView tv8_2 = new TextView(getActivity());
                    tv8_2.setText(String.valueOf(identityWcdma2.getCid() & 0xffff));
                    tr8.addView(tv8_2);
                    // Arfcn
                    TextView tv9_2 = new TextView(getActivity());
                    tv9_2.setText(String.valueOf(identityWcdma2.getUarfcn()));
                    tr9.addView(tv9_2);
                    // BSIC
                    TextView tv10_2 = new TextView(getActivity());
                    tv10_2.setText("-");
                    tr10.addView(tv10_2);
                    // PSC
                    TextView tv11_2 = new TextView(getActivity());
                    tv11_2.setText(String.valueOf(identityWcdma2.getPsc()));
                    tr11.addView(tv11_2);
                    // DBM
                    TextView tv12_2 = new TextView(getActivity());
                    tv12_2.setText(String.valueOf(wcdma2.getDbm()));
                    tr12.addView(tv12_2);
                    // TAC
                    TextView tv13_2 = new TextView(getActivity());
                    tv13_2.setText("-");
                    tr13.addView(tv13_2);
                    // BANDWIDTH
                    TextView tv14_2 = new TextView(getActivity());
                    tv14_2.setText("-");
                    tr14.addView(tv14_2);
                    // PCI
                    TextView tv15_2 = new TextView(getActivity());
                    tv15_2.setText("-");
                    tr15.addView(tv15_2);
                    // CEll Identity
                    TextView tv16_2 = new TextView(getActivity());
                    tv16_2.setText("-");
                    tr16.addView(tv16_2);
                } else if(info_sim2 instanceof CellInfoLte && info_sim2.isRegistered()){
                    final CellSignalStrengthLte lte2 = ((CellInfoLte) info_sim2).getCellSignalStrength();
                    final CellIdentityLte identityLte2 = ((CellInfoLte) info_sim2).getCellIdentity();
                    // Network Type
                    TextView tv6_2 = new TextView(getActivity());
                    tv6_2.setText("LTE");
                    tr6.addView(tv6_2);
                    //   Lac
                    TextView tv7_2 = new TextView(getActivity());
                    tv7_2.setText("-");
                    tr7.addView(tv7_2);
                    //   Cellid
                    TextView tv8_2 = new TextView(getActivity());
                    tv8_2.setText("-");
                    tr8.addView(tv8_2);
                    // Arfcn
                    TextView tv9_2 = new TextView(getActivity());
                    tv9_2.setText(String.valueOf(identityLte2.getEarfcn()));
                    tr9.addView(tv9_2);
                    // BSIC
                    TextView tv10_2 = new TextView(getActivity());
                    tv10_2.setText("-");
                    tr10.addView(tv10_2);
                    // PSC
                    TextView tv11_2 = new TextView(getActivity());
                    tv11_2.setText("-");
                    tr11.addView(tv11_2);
                    // DBM
                    TextView tv12_2 = new TextView(getActivity());
                    tv12_2.setText(String.valueOf(lte2.getDbm()));
                    tr12.addView(tv12_2);
                    // TAC
                    TextView tv13_2 = new TextView(getActivity());
                    tv13_2.setText(String.valueOf(identityLte2.getTac()));
                    tr13.addView(tv13_2);
                    // BANDWIDTH
                    TextView tv14_2 = new TextView(getActivity());
                    tv14_2.setText(String.valueOf(identityLte2.getBandwidth()));
                    tr14.addView(tv14_2);
                    // PCI
                    TextView tv15_2 = new TextView(getActivity());
                    tv15_2.setText(String.valueOf(identityLte2.getPci()));
                    tr15.addView(tv15_2);
                    // CEll Identity
                    TextView tv16_2 = new TextView(getActivity());
                    tv16_2.setText(String.valueOf(identityLte2.getCi()));
                    tr16.addView(tv16_2);
                }else {
                    // Network Type
                    TextView tv6_2 = new TextView(getActivity());
                    tv6_2.setText("-");
                    tr6.addView(tv6_2);
                    //   Lac
                    TextView tv7_2 = new TextView(getActivity());
                    tv7_2.setText("-");
                    tr7.addView(tv7_2);
                    //   Cellid
                    TextView tv8_2 = new TextView(getActivity());
                    tv8_2.setText("-");
                    tr8.addView(tv8_2);
                    // Arfcn
                    TextView tv9_2 = new TextView(getActivity());
                    tv9_2.setText("-");
                    tr9.addView(tv9_2);

                    TextView tv10_2 = new TextView(getActivity());
                    tv10_2.setText("-");
                    tr10.addView(tv10_2);

                    TextView tv11_2 = new TextView(getActivity());
                    tv11_2.setText("-");
                    tr11.addView(tv11_2);

                    TextView tv12_2 = new TextView(getActivity());
                    tv12_2.setText("-");
                    tr12.addView(tv12_2);

                    TextView tv13_2 = new TextView(getActivity());
                    tv13_2.setText("-");
                    tr13.addView(tv13_2);
                    // BANDWIDTH
                    TextView tv14_2 = new TextView(getActivity());
                    tv14_2.setText("-");
                    tr14.addView(tv14_2);
                    // PCI
                    TextView tv15_2 = new TextView(getActivity());
                    tv15_2.setText("-");
                    tr15.addView(tv15_2);
                    // CEll Identity
                    TextView tv16_2 = new TextView(getActivity());
                    tv16_2.setText("-");
                    tr16.addView(tv16_2);
                }
                tl.addView(tr6);
                tl.addView(tr7);
                tl.addView(tr8);
                tl.addView(tr9);
                tl.addView(tr10);
                tl.addView(tr11);
                tl.addView(tr12);
                tl.addView(tr13);
                tl.addView(tr14);
                tl.addView(tr15);
                tl.addView(tr16);
            }
        }

//        Formating the Table in the View
        ViewGroup firstrow = (ViewGroup) tl.getChildAt(0);
        for (int j = 0; j < tl.getChildCount(); j++) {
            ViewGroup tabrows = (ViewGroup) tl.getChildAt(j);
            for (int i = 0; i < tabrows.getChildCount(); i++) {
                View v = tabrows.getChildAt(i);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.rightMargin = 1;
                params.bottomMargin = 1;
                if (i == 0) {
                    params.width = 40;
                } else {
                    params.width = 150;
                }
                params.height = 90;
                if(v instanceof TextView){
                    ((TextView) v).setTextColor(getResources().getColor(R.color.white));
                    ((TextView) v).setBackgroundColor(getResources().getColor(R.color.blue));
                    ((TextView) v).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    ((TextView) v).setGravity(Gravity.CENTER);
                }
            }
        }

        return rootview;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public String getSimIMSI(int slot) {
        String imsi = null;
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        try {
            try {
                Method getSubId = TelephonyManager.class.getMethod("getSubscriberId", int.class);
                SubscriptionManager sm = (SubscriptionManager) getActivity().getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
                imsi = (String) getSubId.invoke(tm, sm.getActiveSubscriptionInfoForSimSlotIndex(slot).getSubscriptionId()); // Sim slot 1 IMSI
            } catch (NullPointerException ne){
                imsi="-";
            }
            return imsi;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return imsi;
    }

    public static Boolean checkInternetConnection(Context ctx){
        ConnectivityManager con_manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (con_manager.getActiveNetworkInfo() !=null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

    public void exitApp(){
        getActivity().finish();
        System.exit(0);
    }

    public static boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }




}


