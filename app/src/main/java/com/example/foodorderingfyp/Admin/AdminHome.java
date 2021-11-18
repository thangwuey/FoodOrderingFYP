package com.example.foodorderingfyp.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.Driver.DriverHistory;
import com.example.foodorderingfyp.Driver.DriverOrder;
import com.example.foodorderingfyp.Driver.DriverProfile;
import com.example.foodorderingfyp.LoginActivity;
import com.example.foodorderingfyp.MainActivity;
import com.example.foodorderingfyp.R;
import com.google.android.material.snackbar.Snackbar;

import Prevalent.PrevalentAdmin;

public class AdminHome extends AppCompatActivity {

    boolean shouldExit = false; // press back button to exit
    Snackbar snackbar;
    RelativeLayout relativeLayout;

    CardView cvFoodMenu, cvOrder,cvHistory,cvDriver,cvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        cvFoodMenu = findViewById(R.id.cv_food_menu);
        cvOrder = findViewById(R.id.cv_order);
        cvHistory = findViewById(R.id.cv_history);
        cvDriver = findViewById(R.id.cv_driver);
        cvLogout = findViewById(R.id.cv_logout);
        relativeLayout = findViewById(R.id.admin_home_layout);
        TextView tvName = findViewById(R.id.admin_home_name);
        String strName = "Welcome, " + PrevalentAdmin.currentOnlineAdmin.getName();
        tvName.setText(strName);

        cvFoodMenu.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHome.this, AdminDeleteFoodMenu.class);
            startActivity(intent);
        });

        cvOrder.setOnClickListener(v -> {
            Intent intent;
            if (PrevalentAdmin.currentOnlineAdmin.getIsAdmin().equals("y")) {
                intent = new Intent(AdminHome.this, AdminSendOrder.class);
            } else {
                intent = new Intent(AdminHome.this, DriverOrder.class);
            }
            startActivity(intent);
        });

        cvHistory.setOnClickListener(v -> {
            Intent intent;
            if (PrevalentAdmin.currentOnlineAdmin.getIsAdmin().equals("y")) {
                intent = new Intent(AdminHome.this, AdminHistory.class);
            } else {
                intent = new Intent(AdminHome.this, DriverHistory.class);
            }
            startActivity(intent);
        });

        cvDriver.setOnClickListener(v -> {
            Intent intent;
            if (PrevalentAdmin.currentOnlineAdmin.getIsAdmin().equals("y")) {
                intent = new Intent(AdminHome.this, AdminManageDriver.class);
            } else {
                intent = new Intent(AdminHome.this, DriverProfile.class);
            }
            startActivity(intent);
        });

        cvLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHome.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back); // BACK effect
        });
    }

    @Override
    public void onBackPressed() {

        // DOUBLE PRESS back button to exit app
        if (shouldExit){
            snackbar.dismiss();

            // EXIT app, COMPLETE
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EXIT_TAG", "SINGLETASK");
            startActivity(intent);
        }else{
            snackbar = Snackbar.make(relativeLayout, "Please press BACK again to exit", Snackbar.LENGTH_SHORT);
            snackbar.show();
            shouldExit = true;

            // if NO press again in a PERIOD, will NOT EXIT
            Handler handler = new Handler();
            handler.postDelayed(() -> shouldExit = false, 1500);
        }
    }
}