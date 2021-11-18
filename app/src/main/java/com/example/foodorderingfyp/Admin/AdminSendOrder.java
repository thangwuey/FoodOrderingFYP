package com.example.foodorderingfyp.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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

    private RecyclerView recyclerViewPrepare, recyclerViewSending, recyclerViewReady;
    RecyclerView.LayoutManager layoutManagerPrepare, layoutManagerSending, layoutManagerReady;
    private final String strPrepare="Preparing";
    private final String strSend="Sending";
    private final String strReady="Ready";
    RadioGroup radioGroup;
    RadioButton radioPrepare,radioSend,radioReady;
    TextView tvPrepare,tvSend,tvReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_order);

        // Order Prepare
        recyclerViewPrepare = findViewById(R.id.rv_aso_prepare);
        recyclerViewPrepare.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerPrepare = new LinearLayoutManager(this);
        recyclerViewPrepare.setLayoutManager(layoutManagerPrepare);


        // Order Sending
        recyclerViewSending = findViewById(R.id.rv_aso_sending);
        recyclerViewSending.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerSending = new LinearLayoutManager(this);
        recyclerViewSending.setLayoutManager(layoutManagerSending);

        // Order Ready
        recyclerViewReady = findViewById(R.id.rv_aso_ready);
        recyclerViewReady.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerReady = new LinearLayoutManager(this);
        recyclerViewReady.setLayoutManager(layoutManagerReady);


        ImageView ivSendOrderBack = findViewById(R.id.aso_back);
        tvPrepare = findViewById(R.id.aso_prepare_order);
        tvReady = findViewById(R.id.aso_ready_order);
        tvSend = findViewById(R.id.aso_sending_order);
        radioGroup = findViewById(R.id.aso_radio_group);
        radioPrepare = findViewById(R.id.aso_radio_prepare);
        radioSend = findViewById(R.id.aso_radio_send);
        radioReady = findViewById(R.id.aso_radio_ready);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroup.getCheckedRadioButtonId()==radioPrepare.getId()) {
                recyclerViewReady.setVisibility(View.GONE);
                recyclerViewPrepare.setVisibility(View.VISIBLE);
                recyclerViewSending.setVisibility(View.GONE);
                tvReady.setVisibility(View.GONE);
                tvPrepare.setVisibility(View.VISIBLE);
                tvSend.setVisibility(View.GONE);
            } else if (radioGroup.getCheckedRadioButtonId()==radioSend.getId()) {
                recyclerViewReady.setVisibility(View.GONE);
                recyclerViewSending.setVisibility(View.VISIBLE);
                recyclerViewPrepare.setVisibility(View.GONE);
                tvReady.setVisibility(View.GONE);
                tvPrepare.setVisibility(View.GONE);
                tvSend.setVisibility(View.VISIBLE);
            } else {
                recyclerViewReady.setVisibility(View.VISIBLE);
                recyclerViewPrepare.setVisibility(View.GONE);
                recyclerViewSending.setVisibility(View.GONE);
                tvReady.setVisibility(View.VISIBLE);
                tvPrepare.setVisibility(View.GONE);
                tvSend.setVisibility(View.GONE);
            }
        });

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
        List<Orders> ordersPrepareFilter = new ArrayList<>();
        List<Orders> ordersSendingFilter = new ArrayList<>();
        List<Orders> ordersReadyFilter = new ArrayList<>();

        // DatabaseReference Users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // DatabaseReference Orders
        DatabaseReference adminOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        // Order Prepare
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

                                    // for loop, store specific Orders to ordersPrepareFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        if (ordersData != null) {
                                            // Add to list, if State is P
                                            if (ordersData.getState().equals("P"))
                                                ordersPrepareFilter.add(ordersData);
                                        }
                                    }
                                }
                                Log.d("OrderID", String.valueOf(ordersPrepareFilter.size()));

                                // Prepare HEADER
                                String strPrepareQty = "Order Preparing : " + ordersPrepareFilter.size();
                                tvPrepare.setText(strPrepareQty);

                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(ordersPrepareFilter, (o1, o2) -> {
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

                                        String strOrderID = "Order No : " + ordersPrepareFilter.get(position).getOrderID();
                                        String strTime = "Time : " + ordersPrepareFilter.get(position).getDate() + " " + ordersPrepareFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);
                                        holder.txtAdminSendOrderState.setText(strPrepare);

                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminSendOrder.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", ordersPrepareFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return ordersPrepareFilter.size();
                                    }
                                };

                                recyclerViewPrepare.setAdapter(mAdapter);
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


        // Order Sending
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

                                    // for loop, store specific Orders to ordersPrepareFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        if (ordersData != null) {
                                            // Add to list, if State is P
                                            if (ordersData.getState().equals("S"))
                                                ordersSendingFilter.add(ordersData);
                                        }
                                    }
                                }
                                Log.d("OrderID", String.valueOf(ordersSendingFilter.size()));

                                // Ready HEADER
                                String strSendQty = "Order Sending : " + ordersSendingFilter.size();
                                tvSend.setText(strSendQty);

                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(ordersSendingFilter, (o1, o2) -> {
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
                                RecyclerView.Adapter<AdminOrderViewHolder> sendingAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + ordersSendingFilter.get(position).getOrderID();
                                        String strTime = "Time : " + ordersSendingFilter.get(position).getDate() + " " + ordersSendingFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);

                                        holder.txtAdminSendOrderState.setText(strSend);
                                        holder.txtAdminSendOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                        holder.txtAdminSendOrderState.setTextColor(Color.parseColor("#9b870c"));


                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminSendOrder.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", ordersSendingFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return ordersSendingFilter.size();
                                    }
                                };

                                recyclerViewSending.setAdapter(sendingAdapter);
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


        // Order Ready
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

                                    // for loop, store specific Orders to ordersPrepareFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        if (ordersData != null) {
                                            // Add to list, if State is P
                                            if (ordersData.getState().equals("R"))
                                                ordersReadyFilter.add(ordersData);
                                        }
                                    }
                                }
                                Log.d("OrderID", String.valueOf(ordersReadyFilter.size()));

                                // Ready HEADER
                                String strReadyQty = "Ready to Send : " + ordersReadyFilter.size();
                                tvReady.setText(strReadyQty);

                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(ordersReadyFilter, (o1, o2) -> {
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
                                RecyclerView.Adapter<AdminOrderViewHolder> readyAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + ordersReadyFilter.get(position).getOrderID();
                                        String strTime = "Time : " + ordersReadyFilter.get(position).getDate() + " " + ordersReadyFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);

                                        holder.txtAdminSendOrderState.setText(strReady);
                                        holder.txtAdminSendOrderState.setBackgroundResource(R.drawable.bg_order_state_ready);
                                        holder.txtAdminSendOrderState.setTextColor(Color.parseColor("#007c33"));


                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminSendOrder.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", ordersReadyFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return ordersReadyFilter.size();
                                    }
                                };

                                recyclerViewReady.setAdapter(readyAdapter);
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