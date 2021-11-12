package com.example.foodorderingfyp.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Orders;
import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ViewHolder.AdminOrderViewHolder;

public class AdminSendOrder extends AppCompatActivity {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private final String strPrepare="Preparing";
    private final String strSend="Sending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_order);

        recyclerView = findViewById(R.id.rv_aso);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        ImageView ivSendOrderBack = (ImageView) findViewById(R.id.aso_back);

        // Back button
        ivSendOrderBack.setOnClickListener(v -> onBackPressed());
    }

    // combine Date and Time
    private Date combineDateTime(Date date, Date time)
    {
        Calendar calendarA = Calendar.getInstance();
        calendarA.setTime(date);
        Calendar calendarB = Calendar.getInstance();
        calendarB.setTime(time);

        calendarA.set(Calendar.HOUR_OF_DAY, calendarB.get(Calendar.HOUR_OF_DAY));
        calendarA.set(Calendar.MINUTE, calendarB.get(Calendar.MINUTE));
        calendarA.set(Calendar.SECOND, calendarB.get(Calendar.SECOND));
        calendarA.set(Calendar.MILLISECOND, calendarB.get(Calendar.MILLISECOND));

        return calendarA.getTime();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // use LIST instead of Firebase
        List<Orders> ordersFilter = new ArrayList<>();

        // DatabaseReference Users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // DatabaseReference Orders
        DatabaseReference adminOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        // User
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {
                    String userPhone;

                    // for loop, get user phone
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        userPhone = postSnapshot.getKey(); // get PHONE from TABLE/CHILD name

                        adminOrderRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists()) {

                                    // for loop, store specific Orders to ordersFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        // Add to list, if State is P
                                        if (ordersData.getState().equals("P") || ordersData.getState().equals("S"))
                                            ordersFilter.add(ordersData);
                                    }
                                }
                                Log.d("OrderID", String.valueOf(ordersFilter.size()));

                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(ordersFilter, (o1, o2) -> {
                                    try {
                                        // String to Date
                                        Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(o1.getDate());
                                        Date time1 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(o1.getTime());
                                        Date date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(o2.getDate());
                                        Date time2 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(o2.getTime());

                                        //Combine Date and Time to DateTime
                                        Date dt1 = combineDateTime(date1, time1);
                                        Date dt2 = combineDateTime(date2, time2);

                                        return dt2.compareTo(dt1);
                                    } catch (ParseException e) {
                                        return 0;
                                    }
                                });

                                // Adapter instead of Firebase Adapter (different declaration)
                                RecyclerView.Adapter<AdminOrderViewHolder> mAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + ordersFilter.get(position).getOrderID();
                                        String strTime = "Time : " + ordersFilter.get(position).getDate() + " " + ordersFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);

                                        if (ordersFilter.get(position).getState().equals("P"))
                                            holder.txtAdminSendOrderState.setText(strPrepare);
                                        else if (ordersFilter.get(position).getState().equals("S")) {
                                            holder.txtAdminSendOrderState.setText(strSend);
                                            holder.txtAdminSendOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                            holder.txtAdminSendOrderState.setTextColor(Color.parseColor("#007c33"));
                                        }

                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminSendOrder.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", ordersFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return ordersFilter.size();
                                    }
                                };

                                recyclerView.setAdapter(mAdapter);
                            }


                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}