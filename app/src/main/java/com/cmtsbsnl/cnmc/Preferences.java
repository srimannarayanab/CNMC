package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.security.GeneralSecurityException;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class Preferences {
    private Context context;

    public Preferences(Context context) {
        this.context =context;
        
    }

    public SharedPreferences getEncryptedSharedPreferences() throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "CnmcPref",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        return sharedPreferences;
    }
}
