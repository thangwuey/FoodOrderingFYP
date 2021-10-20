package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import Prevalent.Prevalent;

public class ConfirmFinalOrderActivity extends AppCompatActivity {
//Video 26

    private EditText nameEditText, phoneEditText,addressEdittext,cityEditText;
    private Button proceedOrderButton;

    private String totalAmount = "";
    private String orderID = ""; // new, for Cart List Admin View and Order
    private ImageView ivConfirmOrderBack; // BACK icon

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
        ivConfirmOrderBack = (ImageView) findViewById(R.id.cfo_back); // BACK icon

        // BACK icon
        ivConfirmOrderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ConfirmFinalOrderActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
            }
        });

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
        if(TextUtils.isEmpty(nameEditText.getText().toString().trim()))
        {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show();
            showKeyboard(nameEditText, this);
        }
        else if(TextUtils.isEmpty(phoneEditText.getText().toString().trim()))
        {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
            showKeyboard(phoneEditText, this);
        }
        else if(TextUtils.isEmpty(addressEdittext.getText().toString().trim()))
        {
            Toast.makeText(this, "Please provide your delivery address.", Toast.LENGTH_SHORT).show();
            showKeyboard(addressEdittext, this);
        }
        else if(TextUtils.isEmpty(cityEditText.getText().toString().trim()))
        {
            Toast.makeText(this, "Please provide your city.", Toast.LENGTH_SHORT).show();
            showKeyboard(cityEditText, this);
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

        // Store Cart List -> User View
        List<Cart> cartList = new ArrayList<>();

        // DatabaseReference Cart List
        DatabaseReference adminCartRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        // User View -> Phone -> Foods
        adminCartRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {

                    // for loop, store all FOOD to cartList
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Cart cartData = postSnapshot.getValue(Cart.class);
                        cartList.add(cartData);
                    }

                    // Check whether cartList is empty in Logcat, Debug
                    /*for (int i=0;i<cartList.size();i++)
                    {
                        Log.d("foodName",cartList.get(i).getFoodName());
                        Log.d("foodPrice",cartList.get(i).getFoodPrice());
                        Log.d("quantity",cartList.get(i).getQuantity());

                    }*/
                }

                // ORDER ID, FORMAT : 15 letter, MMddyyyymmHHss + 1 random number (0 - 9)
                String saveCurrentTimeID, saveCurrentDateID;

                Calendar calForDateID = Calendar.getInstance();
                SimpleDateFormat currentDateID = new SimpleDateFormat("MMddyyyy");
                saveCurrentDateID = currentDateID.format(calForDateID.getTime());

                SimpleDateFormat currentTimeID = new SimpleDateFormat("mmHHss");
                saveCurrentTimeID = currentTimeID.format(calForDateID.getTime());
                int randomNumber = (int) (Math.random() * (9));
                orderID = saveCurrentDateID.trim() + saveCurrentTimeID.trim() + randomNumber;

                // for each loop, Store ALL FOOD into Admin View
                for (Cart cartItem : cartList) {

                    HashMap<String,Object> cartAdminMap = new HashMap<>();
                    cartAdminMap.put("foodName", cartItem.getFoodName());
                    cartAdminMap.put("foodPrice", cartItem.getFoodPrice());
                    cartAdminMap.put("quantity", cartItem.getQuantity());

                    // Admin View -> Phone -> Order ID -> Food Name
                    adminCartRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone()).child(orderID).child(cartItem.getFoodName()).updateChildren(cartAdminMap).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                // Add Order, PUT here to get ORDER ID
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
                                ordersMap.put("orderID", orderID);
                                ordersMap.put("totalAmount",totalAmount);
                                ordersMap.put("name",nameEditText.getText().toString());
                                ordersMap.put("phone",phoneEditText.getText().toString());
                                ordersMap.put("address",addressEdittext.getText().toString());
                                ordersMap.put("city",cityEditText.getText().toString());
                                ordersMap.put("date",saveCurrentDate);
                                ordersMap.put("time",saveCurrentTime);
                                //for admin to change the status
                                ordersMap.put("state","Not Delivered");

                                ordersRef.child(orderID).updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                    });
                }

            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        // Cannot get ORDER ID, move inside
        /*//Video 27
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
        });*/

    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void hideSoftKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);

    }

    @Override
    public void onBackPressed() {

        // NO this will cause BUG
        finish();
        Intent intent = new Intent(ConfirmFinalOrderActivity.this, CartActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
    }
}