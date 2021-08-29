package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import Prevalent.Prevalent;

public class WalletActivity extends AppCompatActivity {

    private Button reload10,reload20,reload50,reload100;
    private TextView showBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        //bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //activity default
        bottomNav.setSelectedItemId(R.id.wallet);

        reload10 = (Button) findViewById(R.id.reload10MYR);
        reload20 = (Button) findViewById(R.id.reload20MYR);
        reload50 = (Button) findViewById(R.id.reload50MYR);
        reload100 = (Button) findViewById(R.id.reload100MYR);
        showBalance = (TextView) findViewById(R.id.textViewMoney);

        final DatabaseReference WalletRef;
        WalletRef = FirebaseDatabase.getInstance().getReference();

        WalletRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists())
                {
                    ///Retrieve from database
                    String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                    //amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                    showBalance.setText(amount);
                }/*else{
                        Toast.makeText(WalletActivity.this,"Error!!!!!!!!!!!!!",Toast.LENGTH_SHORT).show();
                    }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reload10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Integer walletAmount = 10;
                //Integer price = 0;
                //final DatabaseReference WalletRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
                final DatabaseReference WalletRef;
                WalletRef = FirebaseDatabase.getInstance().getReference();

                WalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(!(snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists()))
                        {
                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone()); // this maybe cannot run
                            walletdataMap.put("walletAmount",walletAmount);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                        //showBalance.setText(walletAmount);
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            //Reload the amount to the database
                            String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                            Integer price = Integer.parseInt(amount);

                            price += walletAmount;

                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                            walletdataMap.put("walletAmount",price);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully Reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        reload20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Integer walletAmount = 20;
                //Integer price = 0;
                //final DatabaseReference WalletRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
                final DatabaseReference WalletRef;
                WalletRef = FirebaseDatabase.getInstance().getReference();

                WalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(!(snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists()))
                        {
                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone()); // this maybe cannot run
                            walletdataMap.put("walletAmount",walletAmount);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully reload complete 20 MYR !",Toast.LENGTH_SHORT).show();
                                        //showBalance.setText(walletAmount);
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            //Reload the amount to the database
                            String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                            Integer price = Integer.parseInt(amount);

                            price += walletAmount;

                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                            walletdataMap.put("walletAmount",price);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully Reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        reload50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Integer walletAmount = 50;
                //Integer price = 0;
                //final DatabaseReference WalletRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
                final DatabaseReference WalletRef;
                WalletRef = FirebaseDatabase.getInstance().getReference();

                WalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(!(snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists()))
                        {
                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone()); // this maybe cannot run
                            walletdataMap.put("walletAmount",walletAmount);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully reload complete 50 MYR !",Toast.LENGTH_SHORT).show();
                                        //showBalance.setText(walletAmount);
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            //Reload the amount to the database
                            String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                            Integer price = Integer.parseInt(amount);

                            price += walletAmount;

                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                            walletdataMap.put("walletAmount",price);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully Reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        reload100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Integer walletAmount = 100;
                //Integer price = 0;
                //final DatabaseReference WalletRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
                final DatabaseReference WalletRef;
                WalletRef = FirebaseDatabase.getInstance().getReference();

                WalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(!(snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists()))
                        {
                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone()); // this maybe cannot run
                            walletdataMap.put("walletAmount",walletAmount);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully reload complete 100 MYR !",Toast.LENGTH_SHORT).show();
                                        //showBalance.setText(walletAmount);
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            //Reload the amount to the database
                            String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                            Integer price = Integer.parseInt(amount);

                            price += walletAmount;

                            HashMap<String,Object> walletdataMap = new HashMap<>();
                            walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                            walletdataMap.put("walletAmount",price);

                            WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(WalletActivity.this,"Successfully Reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                        displayBalance();
                                    }else{
                                        Toast.makeText(WalletActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void displayBalance()
    {
        final DatabaseReference WalletRef;
        WalletRef = FirebaseDatabase.getInstance().getReference();

        WalletRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    ///Retrieve from database
                    String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                    //amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                    showBalance.setText(amount);
                }else{
                    Toast.makeText(WalletActivity.this,"Error!!!!!!!!!!!!!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                    //startActivity(new Intent(getApplicationContext(),Test3.class));
                    //overridePendingTransition(0,0);
                    return true;
                case R.id.profile:
                    //selectedFragment = new ProfileFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0,0);
                    return true;}
            return false;
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,selectedFragment).commit();
            //return true;
        }
    };
}