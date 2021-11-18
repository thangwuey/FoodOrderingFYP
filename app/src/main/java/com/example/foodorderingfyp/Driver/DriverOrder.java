package com.example.foodorderingfyp.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.foodorderingfyp.Admin.AdminSendOrder;
import com.example.foodorderingfyp.Admin.AdminSendOrderDetails;
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

import Prevalent.PrevalentAdmin;
import ViewHolder.AdminOrderViewHolder;

public class DriverOrder extends AppCompatActivity {
    private RecyclerView recyclerView,recyclerViewSend;
    TextView tvReady, tvSend;
    RecyclerView.LayoutManager layoutManager,layoutManagerSend;
    private final String strReady="Ready";
    private final String strSend="Sending";
    RadioGroup radioGroup;
    RadioButton radioSend,radioReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_order);

        recyclerView = findViewById(R.id.rv_do_ready);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Sending List
        recyclerViewSend = findViewById(R.id.rv_do_send);
        recyclerViewSend.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerSend = new LinearLayoutManager(this);
        recyclerViewSend.setLayoutManager(layoutManagerSend);


        ImageView ivBack = findViewById(R.id.do_back);
        tvSend = findViewById(R.id.do_sending_order);
        tvReady = findViewById(R.id.do_ready_order);
        radioGroup = findViewById(R.id.do_radio_group);
        radioSend = findViewById(R.id.do_radio_sending);
        radioReady = findViewById(R.id.do_radio_ready);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroup.getCheckedRadioButtonId()==radioSend.getId()) {
                recyclerView.setVisibility(View.GONE);
                tvReady.setVisibility(View.GONE);
                recyclerViewSend.setVisibility(View.VISIBLE);
                tvSend.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvReady.setVisibility(View.VISIBLE);
                recyclerViewSend.setVisibility(View.GONE);
                tvSend.setVisibility(View.GONE);
            }
        });

        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());
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
        List<Orders> readyFilter = new ArrayList<>();
        List<Orders> sendFilter = new ArrayList<>();

        // DatabaseReference Users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // DatabaseReference Orders
        DatabaseReference adminOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        DatabaseReference DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");

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

                                    // for loop, store specific Orders to readyFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        // Add to list, if State is R
                                        if (ordersData.getState().equals("R"))
                                            readyFilter.add(ordersData);
                                        else if (ordersData.getState().equals("S"))
                                            sendFilter.add(ordersData);
                                    }
                                }
                                // Ready
                                Log.d("OrderID", String.valueOf(readyFilter.size()));

                                // Ready HEADER
                                String strReadyQty = "Ready to Send : " + readyFilter.size();
                                tvReady.setText(strReadyQty);

                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(readyFilter, (o1, o2) -> {
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

                                        String strOrderID = "Order No : " + readyFilter.get(position).getOrderID();
                                        String strTime = "Time : " + readyFilter.get(position).getDate() + " " + readyFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);

                                        holder.txtAdminSendOrderState.setText(strReady);
                                        holder.txtAdminSendOrderState.setBackgroundResource(R.drawable.bg_order_state_ready);
                                        holder.txtAdminSendOrderState.setTextColor(Color.parseColor("#007c33"));
                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(DriverOrder.this, DriverOrderDetails.class);
                                            intent.putExtra("orderID", readyFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return readyFilter.size();
                                    }
                                };

                                recyclerView.setAdapter(mAdapter);


                                // Sending
                                DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // for loop, order ID
                                            for (int i=0; i<sendFilter.size();i++) {
                                                Orders ordersData = sendFilter.get(i);
                                                String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();

                                                if (snapshot.hasChild(ordersData.getOrderID())) {
                                                    String phone = snapshot.child(ordersData.getOrderID())
                                                            .child("driverPhone").getValue().toString();
                                                    if (!prePhone.equals(phone)) {
                                                        sendFilter.remove(ordersData);
                                                    }

                                                }
                                                Log.d("OrderIDdr2", String.valueOf(i));
                                            }

                                            // Send HEADER
                                            String strSendQty = "Order Sending : " + sendFilter.size();
                                            tvSend.setText(strSendQty);

                                            // Date
                                            // Sorting, LATEST date and time will be FIRST
                                            Collections.sort(sendFilter, (o1, o2) -> {
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

                                                    String strOrderID = "Order No : " + sendFilter.get(position).getOrderID();
                                                    String strTime = "Time : " + sendFilter.get(position).getDate() + " " + sendFilter.get(position).getTime();

                                                    // holder from AdminOrderViewHolder.java
                                                    holder.txtAdminSendOrderID.setText(strOrderID);
                                                    holder.txtAdminSendOrderTime.setText(strTime);

                                                    holder.txtAdminSendOrderState.setText(strSend);
                                                    holder.txtAdminSendOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                    holder.txtAdminSendOrderState.setTextColor(Color.parseColor("#9b870c"));


                                                    // get Order ID
                                                    holder.itemView.setOnClickListener((view) -> {
                                                        Intent intent = new Intent(DriverOrder.this,DriverOrderDetails.class);
                                                        intent.putExtra("orderID", sendFilter.get(position).getOrderID());
                                                        startActivity(intent);
                                                    });
                                                }

                                                @Override
                                                public int getItemCount() {
                                                    return sendFilter.size();
                                                }
                                            };

                                            recyclerViewSend.setAdapter(sendingAdapter);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
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