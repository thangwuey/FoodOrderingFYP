package com.example.foodorderingfyp.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.foodorderingfyp.R;

public class AdminManageFoodMenu extends AppCompatActivity {

    //This page is useless, no uses already

    Button btnAddFood, btnDeleteFood;//for navigation used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_food_menu);

        //Navigation purpose
        btnAddFood = findViewById(R.id.add_food_item);
        btnDeleteFood = findViewById(R.id.delete_food_item);

        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminManageFoodMenu.this,AdminAddFood.class);
                startActivity(intent);
            }
        });

        btnDeleteFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AdminManageFoodMenu.this,AdminDeleteFoodMenu.class);
                startActivity(intent);
            }
        });
    }
}