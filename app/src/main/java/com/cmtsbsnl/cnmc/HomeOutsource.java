package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.security.GeneralSecurityException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeOutsource extends Fragment {

    public HomeOutsource() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.activity_home_outsource, container, false);

        try {
            SharedPreferences sharedPreferences = new Preferences(rootview.getContext()).getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        //    private ConstraintSet constraintSet =new ConstraintSet();
        Button btn_faults = rootview.findViewById(R.id.btn_faults);
        btn_faults.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyBtsDown.class);
            startActivity(intent);
        });

        Button btn_mybts = rootview.findViewById(R.id.btn_mybts);
        btn_mybts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyBts.class);
            startActivity(intent);
        });

        return rootview;
    }
}