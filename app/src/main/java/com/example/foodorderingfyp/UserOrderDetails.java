package com.example.foodorderingfyp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Prevalent.Prevalent;
import ViewHolder.UserOrderFoodItemViewHolder;

public class UserOrderDetails extends AppCompatActivity {
    private TextView tvOrderNo, tvOrderTime, tvOrderDate, tvOrderAmount;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvCustomerCity;
    private DatabaseReference OrdersRef, CartsRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_details);


        tvOrderNo = findViewById(R.id.uod_order_details_no);
        tvOrderTime = findViewById(R.id.uod_order_details_time);
        tvOrderDate = findViewById(R.id.uod_order_details_date);
        tvOrderAmount = findViewById(R.id.uod_total);
        tvCustomerName = findViewById(R.id.uod_customer_info_name);
        tvCustomerPhone = findViewById(R.id.uod_customer_info_phone);
        tvCustomerAddress = findViewById(R.id.uod_customer_info_address);
        tvCustomerCity = findViewById(R.id.uod_customer_info_city);
        ImageView ivBack = findViewById(R.id.uod_back);
        recyclerView = findViewById(R.id.rv_uod);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        String orderID = getIntent().getStringExtra("orderID");

        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone()).child(orderID);
        CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View")
                .child(Prevalent.currentOnlineUser.getPhone()).child(orderID);

        // display food info
        displayOrderInfo();

        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void displayOrderInfo() {
        OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String tempOrderID = snapshot.child("orderID").getValue(String.class);
                    String tempOrderTime = snapshot.child("time").getValue(String.class);
                    String tempOrderDate = snapshot.child("date").getValue(String.class);
                    String tempOrderAmount = "RM " + snapshot.child("totalAmount").getValue(String.class) + ".00";
                    String tempCustomerName = snapshot.child("name").getValue(String.class);
                    String tempCustomerPhone = snapshot.child("phone").getValue(String.class);
                    String tempCustomerCity = snapshot.child("city").getValue(String.class);


                    String hourMinute;
                    String strHour = tempOrderTime.substring(0,2);
                    int hour;
                    try {
                        if (tempOrderTime.length() < 8)
                            hourMinute = tempOrderTime.substring(0,4) + " AM";
                        else {
                            hour = Integer.parseInt(strHour);
                            if (hour > 12)
                                hourMinute = tempOrderTime.substring(0,5) + " PM";
                            else
                                hourMinute = tempOrderTime.substring(0,5) + " AM";
                        }

                    } catch (Exception e) {
                        hourMinute = "00:00";
                    }

                    tvOrderNo.setText(tempOrderID);
                    tvOrderTime.setText(hourMinute);
                    tvOrderDate.setText(tempOrderDate);
                    tvOrderAmount.setText(tempOrderAmount);
                    tvCustomerName.setText(tempCustomerName);
                    tvCustomerPhone.setText(tempCustomerPhone);
                    tvCustomerAddress.setText(snapshot.child("address").getValue(String.class));
                    tvCustomerCity.setText(tempCustomerCity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Cart List -> Admin View -> Phone -> Order ID -> GET Food Details
        ArrayList<Cart> cartList = new ArrayList<>();

        CartsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.exists()) {
                            Cart cartData = postSnapshot.getValue(Cart.class);
                            cartList.add(cartData);
                        }
                    }

                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<UserOrderFoodItemViewHolder> mAdapter = new RecyclerView.Adapter<UserOrderFoodItemViewHolder>() {
                        @NonNull
                        @Override
                        public UserOrderFoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.user_order_food_item, parent, false);
                            return new UserOrderFoodItemViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull UserOrderFoodItemViewHolder holder, int position) {
                            String strQty = cartList.get(position).getQuantity() + "x";
                            int subTotal = ((Integer.parseInt(cartList.get(position).getFoodPrice()))) * Integer.parseInt(cartList.get(position).getQuantity());
                            String strSubTotal = "RM " + subTotal + ".00";

                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = cartList.get(position).getFoodName();
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
                            return cartList.size();
                        }
                    };

                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}