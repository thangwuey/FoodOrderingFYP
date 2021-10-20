package com.example.foodorderingfyp.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.foodorderingfyp.LoginActivity;
import com.example.foodorderingfyp.MainActivity;
import com.example.foodorderingfyp.R;
import com.google.android.material.snackbar.Snackbar;

public class AdminHome extends AppCompatActivity {

    Button btnMFM;//for navigation used
    //new
    Button btnLogout; // navigate to login page
    boolean shouldExit = false; // press back button to exit
    Toast toast;
    Snackbar snackbar;
    RelativeLayout relativeLayout;

    CardView cvFoodMenu, cvOrder, cvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        /*//Navigation purpose
        btnMFM = findViewById(R.id.manage_food_menu);
        btnLogout = findViewById(R.id.admin_logout);//new*/

        //new
        cvFoodMenu = findViewById(R.id.cv_food_menu);
        cvOrder = findViewById(R.id.cv_order);
        cvLogout = findViewById(R.id.cv_logout);
        relativeLayout = findViewById(R.id.admin_home_layout);

        cvFoodMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminHome.this, AdminDeleteFoodMenu.class);
                startActivity(intent);
            }
        });

        cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminHome.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back); // BACK effect
            }
        });

        /*btnMFM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminHome.this,AdminManageFoodMenu.class);
                startActivity(intent);
            }
        });

        //new
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminHome.this, LoginActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public void onBackPressed() {

        // DOUBLE PRESS back button to exit app
        if (shouldExit){
            snackbar.dismiss();

            // EXIT app, have BUG
            /*moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());*/

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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldExit = false;
                }
            }, 1500);
        }

        // EXIT app, have BUG
        /*// double press back button to exit app
        if (shouldExit){
            snackbar.dismiss();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
            //finish();
            //super.onBackPressed();
        }else{
            *//*toast = Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT);
            toast.show();*//*
            snackbar = Snackbar.make(relativeLayout, "Please press BACK again to exit", Snackbar.LENGTH_SHORT);
            snackbar.show();
            shouldExit = true;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldExit = false;
                }
            }, 1500);
        }*/

    }
}