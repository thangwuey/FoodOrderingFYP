package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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

public class AdminHistory extends AppCompatActivity {

    private RecyclerView recyclerViewOverview, recyclerViewDate, recyclerViewMonth;
    RecyclerView.LayoutManager layoutManagerOverview, layoutManagerDate, layoutManagerMonth;
    RadioGroup radioGroup;
    RadioButton radioOverview, radioDate, radioMonth;
    TextView tvSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_history);

        // Overview
        recyclerViewOverview = findViewById(R.id.rv_ah_overview);
        recyclerViewOverview.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerOverview = new LinearLayoutManager(this);
        recyclerViewOverview.setLayoutManager(layoutManagerOverview);

        // Date
        recyclerViewDate = findViewById(R.id.rv_ah_date);
        recyclerViewDate.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerDate = new LinearLayoutManager(this);
        recyclerViewDate.setLayoutManager(layoutManagerDate);

        // Month
        recyclerViewMonth = findViewById(R.id.rv_ah_month);
        recyclerViewMonth.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerMonth = new LinearLayoutManager(this);
        recyclerViewMonth.setLayoutManager(layoutManagerMonth);

        ImageView ivHistoryBack = findViewById(R.id.ah_back);
        radioGroup = findViewById(R.id.ah_radio_group);
        radioOverview = findViewById(R.id.ah_radio_overview);
        radioDate = findViewById(R.id.ah_radio_date);
        radioDate = findViewById(R.id.ah_radio_month);
        tvSearch = findViewById(R.id.ah_search);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radioGroup.getCheckedRadioButtonId()==radioOverview.getId()) {
                    recyclerViewOverview.setVisibility(View.VISIBLE);
                    recyclerViewDate.setVisibility(View.GONE);
                    recyclerViewMonth.setVisibility(View.GONE);
                    tvSearch.setVisibility(View.GONE);
                } else if (radioGroup.getCheckedRadioButtonId()==radioDate.getId()){
                    recyclerViewOverview.setVisibility(View.GONE);
                    recyclerViewDate.setVisibility(View.VISIBLE);
                    recyclerViewMonth.setVisibility(View.GONE);
                    tvSearch.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewOverview.setVisibility(View.GONE);
                    recyclerViewDate.setVisibility(View.GONE);
                    recyclerViewMonth.setVisibility(View.VISIBLE);
                    tvSearch.setVisibility(View.VISIBLE);
                }
            }
        });

        // Back button
        ivHistoryBack.setOnClickListener(v -> onBackPressed());
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
        List<Orders> overviewFilter = new ArrayList<>();
        List<Orders> dateFilter = new ArrayList<>();
        List<Orders> monthFilter = new ArrayList<>();

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

                                    // for loop, store specific Orders to overviewFilter
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("OrderID",postSnapshot.getKey());
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        // Add to list, if State is P
                                        if (ordersData.getState().equals("D")) {
                                            overviewFilter.add(ordersData);
                                            dateFilter.add(ordersData);
                                            monthFilter.add(ordersData);
                                        }

                                    }
                                }
                                Log.d("OrderID", String.valueOf(overviewFilter.size()));

                                // Overview
                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(overviewFilter, (o1, o2) -> {
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

                                // Overview
                                // Adapter instead of Firebase Adapter (different declaration)
                                RecyclerView.Adapter<AdminOrderViewHolder> overviewAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + overviewFilter.get(position).getOrderID();
                                        String strTime = "Time : " + overviewFilter.get(position).getDate() + " " + overviewFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);
                                        holder.txtAdminSendOrderState.setVisibility(View.GONE);


                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminHistory.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", overviewFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return overviewFilter.size();
                                    }
                                };

                                recyclerViewOverview.setAdapter(overviewAdapter);

                                // Date
                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(dateFilter, (o1, o2) -> {
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

                                // Date
                                // Adapter instead of Firebase Adapter (different declaration)
                                RecyclerView.Adapter<AdminOrderViewHolder> dateAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + dateFilter.get(position).getOrderID();
                                        String strTime = "Time : " + dateFilter.get(position).getDate() + " " + dateFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);
                                        holder.txtAdminSendOrderState.setVisibility(View.GONE);


                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminHistory.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", dateFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return dateFilter.size();
                                    }
                                };

                                recyclerViewDate.setAdapter(dateAdapter);

                                // Month
                                // Sorting, LATEST date and time will be FIRST
                                Collections.sort(monthFilter, (o1, o2) -> {
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

                                // Month
                                // Adapter instead of Firebase Adapter (different declaration)
                                RecyclerView.Adapter<AdminOrderViewHolder> monthAdapter = new RecyclerView.Adapter<AdminOrderViewHolder>() {
                                    @NonNull
                                    @Override
                                    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.admin_order_item_layout, parent, false);
                                        return new AdminOrderViewHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {

                                        String strOrderID = "Order No : " + monthFilter.get(position).getOrderID();
                                        String strTime = "Time : " + monthFilter.get(position).getDate() + " " + monthFilter.get(position).getTime();

                                        // holder from AdminOrderViewHolder.java
                                        holder.txtAdminSendOrderID.setText(strOrderID);
                                        holder.txtAdminSendOrderTime.setText(strTime);
                                        holder.txtAdminSendOrderState.setVisibility(View.GONE);


                                        // get Order ID
                                        holder.itemView.setOnClickListener((view) -> {
                                            Intent intent = new Intent(AdminHistory.this,AdminSendOrderDetails.class);
                                            intent.putExtra("orderID", monthFilter.get(position).getOrderID());
                                            startActivity(intent);
                                        });
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return monthFilter.size();
                                    }
                                };

                                recyclerViewMonth.setAdapter(monthAdapter);
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