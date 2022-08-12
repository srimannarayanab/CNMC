package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class Logout extends Fragment {
    AlertDialog alertDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.activity_logout, container, false);
        alertDialog = new AlertDialog.Builder(rootview.getContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Your are about to logout, press ok to continue");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                ((DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        startActivity(intent);
                }));
        alertDialog.show();

        return rootview;
    }

}
