package com.example.foodorderingfyp.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ViewHolder.AdminOrderFoodItemViewHolder;

public class AdminSendOrderDetails extends AppCompatActivity {

    private ImageView ivSendOrderDetailsBack;
    private Button btnSendOrder;
    private TextView tvOrderID, tvOrderState, tvOrderAmount, tvOrderNo, tvOrderTime, tvOrderDate;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvCustomerCity;
    private DatabaseReference OrdersRef, CartsRef, UserRef;
    private String orderID = "", userPhone = "";
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_order_details);

        btnSendOrder = (Button) findViewById((R.id.send_order_button));
        tvOrderID = (TextView) findViewById(R.id.send_order_id);
        tvOrderState = (TextView) findViewById(R.id.send_order_state);
        tvOrderAmount = (TextView) findViewById(R.id.send_order_food_amount);
        tvOrderNo = (TextView) findViewById(R.id.send_order_no);
        tvOrderTime = (TextView) findViewById(R.id.send_order_time);
        tvOrderDate = (TextView) findViewById(R.id.send_order_date);
        tvCustomerName = (TextView) findViewById(R.id.send_order_customer_name);
        tvCustomerPhone = (TextView) findViewById(R.id.send_order_customer_phone);
        tvCustomerAddress = (TextView) findViewById(R.id.send_order_customer_address);
        tvCustomerCity = (TextView) findViewById(R.id.send_order_customer_city);
        ivSendOrderDetailsBack = (ImageView) findViewById(R.id.asod_back);
        recyclerView = findViewById(R.id.rv_asod);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        orderID = getIntent().getStringExtra("orderID");
        Log.d("orderID", String.valueOf(orderID));

        //userPhone = getIntent().getStringExtra("phone");

        //OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(userPhone).child(orderID);
        //CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View").child(userPhone).child(orderID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // display food info
        displayOrderInfo();

        ivSendOrderDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Got Bug, it can GO BACK if user click PHONE BACK BUTTON
                /*Intent intent = new Intent(AdminSendOrderDetails.this, AdminSendOrder.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);*/

                onBackPressed();
            }
        });

        btnSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change order state
                changeState();
            }
        });
    }

    private void changeState() {
        if (tvOrderState.getText().equals("Preparing")) {
            // loading when processing to SEND ORDER
            loadingBar.setTitle("Send Order");
            loadingBar.setMessage("Dear Admin, please wait while we are sending order.");
        } else {
            // loading when processing to DONE ORDER
            loadingBar.setTitle("Done Order");
            loadingBar.setMessage("Dear Admin, please wait while we have delivered the order.");
        }
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // User
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    String userPhone = "";

                    // for loop, get user phone
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        userPhone = postSnapshot.getKey(); // get PHONE from TABLE/CHILD name

                        // get Order Info, Orders -> Phone
                        OrdersRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    // Order ID
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(postSnapshot.getKey())) {

                                            if (postSnapshot.child("state").getValue().equals("P")) {

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("S");
                                                Toast.makeText(AdminSendOrderDetails.this, "Order Sent", Toast.LENGTH_SHORT).show();
                                                btnSendOrder.setText("Done Order");

                                                tvOrderState.setText("Sending");
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#007c33"));
                                                // CLOSE loading bar
                                                loadingBar.dismiss();
                                            } else {

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("D");
                                                Toast.makeText(AdminSendOrderDetails.this, "Order Done", Toast.LENGTH_SHORT).show();
                                                // CLOSE loading bar
                                                loadingBar.dismiss();

                                                Intent intent = new Intent(AdminSendOrderDetails.this, AdminSendOrder.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
                                                finish();
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayOrderInfo() {

        // User
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    String userPhone = "";

                    // for loop, get user phone
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        userPhone = postSnapshot.getKey(); // get PHONE from TABLE/CHILD name

                        // get Order Info
                        OrdersRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(postSnapshot.getKey())) {
                                            String tempOrderID = postSnapshot.child("orderID").getValue().toString();
                                            String tempOrderTime = postSnapshot.child("time").getValue().toString();
                                            String tempOrderDate = postSnapshot.child("date").getValue().toString();
                                            String tempOrderState = postSnapshot.child("state").getValue().toString();
                                            String tempOrderAmount = "RM " + postSnapshot.child("totalAmount").getValue().toString() + ".00";
                                            String tempCustomerName = postSnapshot.child("name").getValue().toString();
                                            String tempCustomerPhone = postSnapshot.child("phone").getValue().toString();
                                            String tempCustomerCity = postSnapshot.child("city").getValue().toString();

                                            // Address, LINE BREAK when more than 20 characters
                                            String str = postSnapshot.child("address").getValue().toString();
                                            String[] strArray = str.split(" ");
                                            StringBuilder builder = new StringBuilder();
                                            String tmpString = "";

                                            for (int i=0;i<strArray.length;i++) {
                                                String s = strArray[i];
                                                if (tmpString.length() > 20) {
                                                    builder.append(tmpString).append("\n");
                                                    tmpString = s + " ";
                                                }else if (s.equals(strArray[strArray.length - 1])) {
                                                    tmpString += s;
                                                    builder.append(tmpString);
                                                }
                                                else {
                                                    tmpString += s + " ";
                                                }
                                            }

                                            tvOrderID.setText(tempOrderID);
                                            tvOrderNo.setText(tempOrderID);
                                            tvOrderTime.setText(tempOrderTime);
                                            tvOrderDate.setText(tempOrderDate);
                                            tvOrderAmount.setText(tempOrderAmount);
                                            tvCustomerName.setText(tempCustomerName);
                                            tvCustomerPhone.setText(tempCustomerPhone);
                                            tvCustomerAddress.setText(builder.toString());
                                            tvCustomerCity.setText(tempCustomerCity);
                                            if (tempOrderState.equals("P")) // Preparing State
                                                tvOrderState.setText("Preparing");
                                            else if (tempOrderState.equals("S")) { // Sending State
                                                tvOrderState.setText("Sending");
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#007c33"));
                                                btnSendOrder.setText("Done Order");
                                            }

                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // Cart List -> Admin View -> Phone -> Order ID -> GET Food Details
                        ArrayList<Cart> orderFoodList = new ArrayList<>();

                        CartsRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("userPhone", postSnapshot.getKey());
                                        Log.d("userPhone123", orderID);
                                        if (orderID.equals(postSnapshot.getKey())) {
                                            for (DataSnapshot cartSnapshot : postSnapshot.getChildren()) {
                                                Cart cartData = cartSnapshot.getValue(Cart.class);
                                                Log.d("name", cartData.getFoodName());
                                                orderFoodList.add(cartData);
                                            }

                                            // Adapter instead of Firebase Adapter (different declaration)
                                            RecyclerView.Adapter<AdminOrderFoodItemViewHolder> mAdapter = new RecyclerView.Adapter<AdminOrderFoodItemViewHolder>() {
                                                @NonNull
                                                @Override
                                                public AdminOrderFoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                                    View view = LayoutInflater.from(parent.getContext())
                                                            .inflate(R.layout.admin_order_food_item, parent, false);
                                                    return new AdminOrderFoodItemViewHolder(view);
                                                }

                                                @Override
                                                public void onBindViewHolder(@NonNull AdminOrderFoodItemViewHolder holder, int position) {
                                                    String strQty = orderFoodList.get(position).getQuantity() + "x";
                                                    int subTotal = ((Integer.parseInt(orderFoodList.get(position).getFoodPrice()))) * Integer.parseInt(orderFoodList.get(position).getQuantity());
                                                    String strSubTotal = "RM " + subTotal + ".00";

                                                    // First letter of each word to UPPERCASE in FOOD NAME
                                                    String str = orderFoodList.get(position).getFoodName();
                                                    String[] strArray = str.split(" ");
                                                    StringBuilder builder = new StringBuilder();
                                                    for (String s : strArray) {
                                                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                                        builder.append(cap).append(" ");
                                                    }

                                                    // holder from AdminOrderFoodItemViewHolder.java
                                                    holder.txtAdminOrderFoodQuantity.setText(strQty);
                                                    holder.txtAdminOrderFoodName.setText(builder.toString());
                                                    holder.txtAdminOrderFoodSubtotal.setText(strSubTotal);

                                                }

                                                @Override
                                                public int getItemCount() {
                                                    return orderFoodList.size();
                                                }
                                            };

                                            recyclerView.setAdapter(mAdapter);
                                        }
                                    }

                                }
                                //Log.d("sizeM", String.valueOf(orderFoodList.size()));


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}