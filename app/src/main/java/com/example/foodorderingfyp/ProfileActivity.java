package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class ProfileActivity extends AppCompatActivity {

    private Button logoutBtn;
    private Button trackDeliveryBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutBtn = (Button) findViewById(R.id.logout);
        trackDeliveryBtn = (Button) findViewById(R.id.trackDelivery) ;


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //activity default
        bottomNav.setSelectedItemId(R.id.profile);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this,"Thanks For Using WuJiak See You Again !:)",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                //use to prevent the user come back the previous section
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            //Fragment selectedFragment = null;
            switch (item.getItemId()){

                case R.id.menu:
                    //selectedFragment = new HomeFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(),MainActivity2.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.cart:
                    //selectedFragment = new CartFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), CartActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.wallet:
                    //selectedFragment = new WalletFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), WalletActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.profile:
                    //selectedFragment = new ProfileFragment();
                    //break;
                    return true;
            }
            return false;
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,selectedFragment).commit();
            //return true;
        }
    };
}