package com.example.foodorderingfyp.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.foodorderingfyp.R;

public class AdminHome extends AppCompatActivity {

    Button btnMFM;//for navigation used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //Navigation purpose
        btnMFM = findViewById(R.id.manage_food_menu);

        btnMFM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminHome.this,AdminManageFoodMenu.class);
                startActivity(intent);
            }
        });
    }
}