package com.cmtsbsnl.cnmc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;
import java.security.GeneralSecurityException;

import androidx.appcompat.widget.Toolbar;

public class IpSitesAdministration extends SessionActivity {

  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ip_sites_administration);
    
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() !=null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      toolbar.setNavigationOnClickListener((View v)->onBackPressed());
    }

    ImageButton homeBtn =  toolbar.findViewById(R.id.home);
    homeBtn.setOnClickListener((View v)->startActivity(new Intent(this, Navigational.class)));

    try {
      sharedPreferences = new Preferences(this).getEncryptedSharedPreferences();
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    Button btn_ip_add_users = findViewById(R.id.btn_ip_add_user);
    btn_ip_add_users.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(IpSitesAdministration.this, IpSitesAddUsers.class);
        startActivity(intent);
      }
    });
    Button btn_ip_add_sites = findViewById(R.id.btn_ip_add_sites);
    btn_ip_add_sites.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(IpSitesAdministration.this, IpSitesAddSites.class);
        startActivity(intent);
      }
    });
    Button btn_ip_view_sites = findViewById(R.id.btn_ip_view_sites);
    btn_ip_view_sites.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(IpSitesAdministration.this, IpSitesViewSites.class);
        startActivity(intent);
      }
    });

    Button btn_ip_change_msisdn = findViewById(R.id.btn_ip_change_msisdn);
    btn_ip_change_msisdn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(IpSitesAdministration.this, IpSitesChangeMsisdn.class);
        startActivity(intent);
      }
    });
  }
}