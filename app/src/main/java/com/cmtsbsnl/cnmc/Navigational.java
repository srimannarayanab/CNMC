package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class Navigational extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener {

  public Toolbar toolbar;
  public DrawerLayout drawerLayout;
  public NavController navController;
  public NavigationView navigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_navigational);

    setupNavigation();

    SharedPreferences pref = getApplicationContext().getSharedPreferences("CnmcPref", Context.MODE_PRIVATE);
    NavigationView nview = findViewById(R.id.navigationView);
    View hview = nview.getHeaderView(0);
    TextView tv_h = (TextView) hview.findViewById(R.id.appCompatTextView);
    tv_h.setText(pref.getString("name","BSNL"));
    TextView tv_h1 = (TextView) hview.findViewById(R.id.appCompatTextView1);
    tv_h1.setText(pref.getString("email",""));

  }

  // Setting Up One Time Navigation
  private void setupNavigation() {
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);

    drawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.navigationView);
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
    NavigationUI.setupWithNavController(navigationView, navController);
    navigationView.setNavigationItemSelectedListener(this);
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment), drawerLayout);
  }

  @Override
  public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
    menuItem.setChecked(true);
    drawerLayout.closeDrawers();
    int id = menuItem.getItemId();
    switch (id) {
      case R.id.home:
        navController.navigate(R.id.home);
        break;

      case R.id.signal:
        navController.navigate(R.id.signal);
        break;

      case R.id.userinfo:
        navController.navigate(R.id.userinfo);
        break;

      case R.id.sync_btsmaster:
        navController.navigate(R.id.sync_btsmaster);
        break;

      case R.id.logout:
        navController.navigate(R.id.logout);
        break;
    }
    return true;
  }
}
