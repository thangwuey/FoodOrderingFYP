package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import Prevalent.Prevalent;

public class ConfirmFinalOrderActivity extends AppCompatActivity {
//Video 26

    private EditText nameEditText, phoneEditText,addressEdittext,cityEditText;
    private Button proceedOrderButton;

    private String totalAmount = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        //Video 26
        totalAmount = getIntent().getStringExtra("Total Price");
        Toast.makeText(this,"Total Price =  MYR " + totalAmount,Toast.LENGTH_SHORT).show();

        //Video
        proceedOrderButton = (Button) findViewById(R.id.proceed_to_payment_button);

        nameEditText = (EditText) findViewById(R.id.delivery_name);
        phoneEditText = (EditText) findViewById(R.id.delivery_phone_number);
        addressEdittext = (EditText) findViewById(R.id.delivery_address);
        cityEditText = (EditText) findViewById(R.id.delivery_city);

        proceedOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CheckDeliveryInfo();
            }
        });
    }

    private void CheckDeliveryInfo()
    {
        final DatabaseReference CheckBalanceRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
        //Video 27
        if(TextUtils.isEmpty(nameEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(addressEdittext.getText().toString()))
        {
            Toast.makeText(this, "Please provide your delivery address.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cityEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your city.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            CheckBalanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(!(snapshot.exists()))
                    {
                        Toast.makeText(ConfirmFinalOrderActivity.this,"Please Reload Your E-Wallet Balance to Complete Order!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ConfirmFinalOrderActivity.this,WalletActivity.class);
                        startActivity(intent);

                    }else{
                        //Reload the amount to the database
                        //String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                        String amount = snapshot.child("walletAmount").getValue().toString();

                        Integer price = Integer.parseInt(amount);
                        Integer totalAmount1 = Integer.parseInt(totalAmount);

                        if(price < totalAmount1){
                            Toast.makeText(ConfirmFinalOrderActivity.this,"Insufficient Balance, please reload your balance! ",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ConfirmFinalOrderActivity.this,WalletActivity.class);
                            startActivity(intent);
                        }else{
                            ConfirmOrder();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }

    private void ConfirmOrder()
    {
        final DatabaseReference CheckBalanceRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());

        CheckBalanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if((snapshot.exists()))
                {
                    String amount = snapshot.child("walletAmount").getValue().toString();

                    Integer price = Integer.parseInt(amount);
                    Integer totalAmount1 = Integer.parseInt(totalAmount);

                    price -= totalAmount1;

                    HashMap<String,Object> walletdataMap = new HashMap<>();
                    walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                    walletdataMap.put("walletAmount",price);

                    /*CheckBalanceRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(ConfirmFinalOrderActivity.this,"Wallet Balance Deducted!",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ConfirmFinalOrderActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });*/

                    CheckBalanceRef.updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(ConfirmFinalOrderActivity.this,"Wallet Balance Deducted!",Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(ConfirmFinalOrderActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }else{
                    //Reload the amount to the database
                    //String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                    String amount = snapshot.child("walletAmount").getValue().toString();

                    Integer price = Integer.parseInt(amount);
                    Integer totalAmount1 = Integer.parseInt(totalAmount);

                    if(price < totalAmount1){
                        Toast.makeText(ConfirmFinalOrderActivity.this,"Insufficient Balance, please reload your balance! ",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ConfirmFinalOrderActivity.this,WalletActivity.class);
                        startActivity(intent);
                    }else{
                        ConfirmOrder();

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }

        });

        //Video 27
        final String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        //Create new child for the firebase and get the specific users phone name
        final DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        //maybe can use for user to create e-wallet amount
        HashMap<String,Object> ordersMap = new HashMap<>();
        ordersMap.put("Order ID",ordersRef.push().getKey());
        ordersMap.put("totalAmount",totalAmount);
        ordersMap.put("name",nameEditText.getText().toString());
        ordersMap.put("phone",phoneEditText.getText().toString());
        ordersMap.put("address",addressEdittext.getText().toString());
        ordersMap.put("city",cityEditText.getText().toString());
        ordersMap.put("date",saveCurrentDate);
        ordersMap.put("time",saveCurrentTime);
        //for admin to change the status
        ordersMap.put("state","Not Delivered");

        ordersRef.child(ordersRef.push().getKey()).updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task)
            {//if user confirm order need to clear the cart item
                FirebaseDatabase.getInstance().getReference().child("Cart List").child("User View").child(Prevalent.currentOnlineUser.getPhone()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ConfirmFinalOrderActivity.this,"Your final order has been placed successfully!",Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ConfirmFinalOrderActivity.this, MainActivity2.class);
                            //use to prevent the user come back the previous section
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

    }
}