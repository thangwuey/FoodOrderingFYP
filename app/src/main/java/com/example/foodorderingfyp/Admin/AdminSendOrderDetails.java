package com.example.foodorderingfyp.Admin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.example.foodorderingfyp.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import ViewHolder.AdminOrderFoodItemViewHolder;

public class AdminSendOrderDetails extends AppCompatActivity {
    private Button btnSendOrder;
    private TextView tvOrderID, tvOrderState, tvOrderAmount, tvOrderNo, tvOrderTime, tvOrderDate;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvCustomerCity;
    private TextView tvDriverNameHeader, tvDriverName;
    private DatabaseReference OrdersRef, CartsRef, UserRef, DelRef;
    private String orderID = "";
    private final String strPrepare="Preparing";
    private final String strSend="Sending";
    private final String strDone="Done Order";
    private final String strReady="Ready";
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private ProgressDialog loadingBar;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int deniedCount = 0;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_order_details);

        btnSendOrder = findViewById((R.id.food_ready_button));
        tvOrderID = findViewById(R.id.send_order_id);
        tvOrderState = findViewById(R.id.send_order_state);
        tvOrderAmount = findViewById(R.id.send_order_food_amount);
        tvOrderNo = findViewById(R.id.send_order_no);
        tvOrderTime = findViewById(R.id.send_order_time);
        tvOrderDate = findViewById(R.id.send_order_date);
        tvCustomerName = findViewById(R.id.send_order_customer_name);
        tvCustomerPhone = findViewById(R.id.send_order_customer_phone);
        tvCustomerAddress = findViewById(R.id.send_order_customer_address);
        tvCustomerCity = findViewById(R.id.send_order_customer_city);
        tvDriverNameHeader = findViewById(R.id.send_order_driver_name_header);
        tvDriverName = findViewById(R.id.send_order_driver_name);
        ImageView ivSendOrderDetailsBack = findViewById(R.id.asod_back);
        recyclerView = findViewById(R.id.rv_asod);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        orderID = getIntent().getStringExtra("orderID");

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View");
        DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // Initialize fusedLocationProviderClient, for GPS location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize locationRequest, for GPS location UPDATES
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // display food info
        displayOrderInfo();

        ivSendOrderDetailsBack.setOnClickListener(v -> onBackPressed());

        btnSendOrder.setOnClickListener(v -> {
            // change order state
            changeState();
        });
    }

    // Change Order State
    private void changeState() {
        // loading bar declaration
        loadingBar.setTitle("Order Ready to Send");
        loadingBar.setMessage("Dear Admin, please wait while we are changing order state.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // User
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    String userPhone;

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

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("R");
                                                //Toast.makeText(AdminSendOrderDetails.this, "Order Sent", Toast.LENGTH_SHORT).show();
                                                btnSendOrder.setVisibility(View.GONE);

                                                tvOrderState.setText(strReady);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#9b870c"));
                                                // CLOSE loading bar
                                                loadingBar.dismiss();
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
                    String userPhone;

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

                                            for (String s : strArray) {
                                                if (tmpString.length() > 20) {
                                                    builder.append(tmpString).append("\n");
                                                    tmpString = s + " ";
                                                } else if (s.equals(strArray[strArray.length - 1])) {
                                                    tmpString += s;
                                                    builder.append(tmpString);
                                                } else {
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
                                            if (tempOrderState.equals("P")){// Preparing State
                                                tvOrderState.setText(strPrepare);
                                                btnSendOrder.setVisibility(View.VISIBLE);
                                            } else if (tempOrderState.equals("S")) { // Sending State
                                                tvOrderState.setText(strSend);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#9b870c"));
                                                /*btnSendOrder.setText(strDone);*/
                                                btnSendOrder.setVisibility(View.GONE);

                                                // Driver Name
                                                tvDriverNameHeader.setVisibility(View.VISIBLE);
                                                tvDriverName.setVisibility(View.VISIBLE);
                                                DelRef.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.hasChild("driverName")) {
                                                            tvDriverName.setText(snapshot.child("driverName").getValue().toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else { // Done State
                                                tvOrderState.setVisibility(View.INVISIBLE);
                                                btnSendOrder.setVisibility(View.GONE);

                                                // Driver Name
                                                tvDriverNameHeader.setVisibility(View.VISIBLE);
                                                tvDriverName.setVisibility(View.VISIBLE);
                                                DelRef.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.hasChild("driverName")) {
                                                            String strDriverName = snapshot.child("driverName").getValue().toString();
                                                            tvDriverName.setText(strDriverName);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }

                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // Cart List -> Admins View -> Phone -> Order ID -> GET Food Details
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