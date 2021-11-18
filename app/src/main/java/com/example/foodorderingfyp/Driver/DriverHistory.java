package com.example.foodorderingfyp.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.foodorderingfyp.Admin.AdminHistory;
import com.example.foodorderingfyp.Admin.AdminSendOrderDetails;
import com.example.foodorderingfyp.ModelClass.Orders;
import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.whiteelephant.monthpicker.MonthPickerDialog;

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

public class DriverHistory extends AppCompatActivity {
    private RecyclerView recyclerViewOverview, recyclerViewDate, recyclerViewMonth;
    RecyclerView.LayoutManager layoutManagerOverview, layoutManagerDate, layoutManagerMonth;
    RadioGroup radioGroup;
    RadioButton radioOverview, radioDate, radioMonth;
    private DatePickerDialog datePickerDialog;
    Button btnDatePicker;
    int year,month,dayOfMonth;
    Calendar calendar;
    DatabaseReference userRef,adminOrderRef,DelRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_history);

        // DatabaseReference Users
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // DatabaseReference Orders
        adminOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");

        // Overview
        recyclerViewOverview = findViewById(R.id.rv_dh_overview);
        recyclerViewOverview.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerOverview = new LinearLayoutManager(this);
        recyclerViewOverview.setLayoutManager(layoutManagerOverview);

        // Date
        recyclerViewDate = findViewById(R.id.rv_dh_date);
        recyclerViewDate.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerDate = new LinearLayoutManager(this);
        recyclerViewDate.setLayoutManager(layoutManagerDate);

        // Month
        recyclerViewMonth = findViewById(R.id.rv_dh_month);
        recyclerViewMonth.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerMonth = new LinearLayoutManager(this);
        recyclerViewMonth.setLayoutManager(layoutManagerMonth);

        ImageView ivHistoryBack = findViewById(R.id.dh_back);
        radioGroup = findViewById(R.id.dh_radio_group);
        radioOverview = findViewById(R.id.dh_radio_overview);
        radioDate = findViewById(R.id.dh_radio_date);
        radioMonth = findViewById(R.id.dh_radio_month);
        btnDatePicker = findViewById(R.id.dh_date_picker);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroup.getCheckedRadioButtonId()==radioOverview.getId()) {
                recyclerViewOverview.setVisibility(View.VISIBLE);
                recyclerViewDate.setVisibility(View.GONE);
                recyclerViewMonth.setVisibility(View.GONE);
                btnDatePicker.setVisibility(View.GONE);
            } else if (radioGroup.getCheckedRadioButtonId()==radioDate.getId()){
                recyclerViewOverview.setVisibility(View.GONE);
                recyclerViewDate.setVisibility(View.VISIBLE);
                recyclerViewMonth.setVisibility(View.GONE);
                btnDatePicker.setVisibility(View.VISIBLE);
                btnDatePicker.setText(getTodayDate());
                searchOrderDate(getTodayDate());
            } else {
                recyclerViewOverview.setVisibility(View.GONE);
                recyclerViewDate.setVisibility(View.GONE);
                recyclerViewMonth.setVisibility(View.VISIBLE);
                btnDatePicker.setVisibility(View.VISIBLE);
                btnDatePicker.setText(getTodayDate());
                searchOrderMonth(getTodayDate());
            }
        });

        btnDatePicker.setOnClickListener(v -> {
            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if (radioGroup.getCheckedRadioButtonId()==radioDate.getId()) { // Search Date

                datePickerDialog = new DatePickerDialog(DriverHistory.this,
                        (datePicker, year, month, day) -> {
                            String date = day + "/" + (month + 1) + "/" + year;
                            btnDatePicker.setText(date);
                            searchOrderDate(date);
                        }, year, month, dayOfMonth);

                datePickerDialog.show();
            } else if (radioGroup.getCheckedRadioButtonId()==radioMonth.getId()) { // Search Month
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(DriverHistory.this,
                        (selectedMonth, selectedYear) -> {
                            String month = (selectedMonth + 1) + "/" + selectedYear;
                            btnDatePicker.setText(month);
                            searchOrderMonth(month);
                        }, year, month);

                builder.setActivatedMonth(month)
                        .setMinYear(1990)
                        .setActivatedYear(year)
                        .setMaxYear(2030)
                        .setTitle("Select month year")
                        .build().show();
            }
        });

        // Back button
        ivHistoryBack.setOnClickListener(v -> onBackPressed());
    }

    private String getTodayDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (radioGroup.getCheckedRadioButtonId()==radioMonth.getId()) {
            return month + "/" + year;
        } else {
            return day + "/" + month + "/" + year;
        }
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

    private void searchOrderDate(String date) {
        List<Orders> dateFilter = new ArrayList<>();

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
                                        Log.d("date123",date);
                                        Orders ordersData = postSnapshot.getValue(Orders.class);

                                        // Add to list, if State is D & search date
                                        if (ordersData.getState().equals("D") && ordersData.getDate().equals(date)) {
                                            dateFilter.add(ordersData);
                                        }
                                    }
                                }
                                DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // for loop, order ID
                                            for (int i=0; i<dateFilter.size();i++) {
                                                Orders ordersData = dateFilter.get(i);
                                                String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();

                                                if (snapshot.hasChild(ordersData.getOrderID())) {
                                                    String phone = snapshot.child(ordersData.getOrderID())
                                                            .child("driverPhone").getValue().toString();
                                                    if (!prePhone.equals(phone)) {
                                                        dateFilter.remove(ordersData);
                                                    }

                                                }
                                                Log.d("OrderIDdr2", String.valueOf(i));
                                            }

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
                                                        Intent intent = new Intent(DriverHistory.this, AdminSendOrderDetails.class);
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

    private void searchOrderMonth(String month) {
        List<Orders> monthFilter = new ArrayList<>();

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
                                        Log.d("date123",month);
                                        Orders ordersData = postSnapshot.getValue(Orders.class);
                                        String strMonthYear;

                                        if (ordersData != null) {
                                            // IF date NOT LIKE 1/11/2021
                                            if (ordersData.getDate().length() > 9)
                                                strMonthYear = ordersData.getDate().substring(3);
                                            else
                                                strMonthYear = ordersData.getDate().substring(2);

                                            // Add to list, if State is D & search MONTH YEAR
                                            if (ordersData.getState().equals("D") && strMonthYear.equals(month)) {
                                                monthFilter.add(ordersData);
                                            }
                                        }

                                    }
                                }
                                DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // for loop, order ID
                                            for (int i=0; i<monthFilter.size();i++) {
                                                Orders ordersData = monthFilter.get(i);
                                                String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();

                                                if (snapshot.hasChild(ordersData.getOrderID())) {
                                                    String phone = snapshot.child(ordersData.getOrderID())
                                                            .child("driverPhone").getValue().toString();
                                                    if (!prePhone.equals(phone)) {
                                                        monthFilter.remove(ordersData);
                                                    }

                                                }
                                                Log.d("OrderIDdr2", String.valueOf(i));
                                            }

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
                                                        Intent intent = new Intent(DriverHistory.this,AdminSendOrderDetails.class);
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

    @Override
    protected void onStart() {
        super.onStart();


        // use LIST instead of Firebase
        List<Orders> driversFilter = new ArrayList<>();
        List<Orders> overviewFilter = new ArrayList<>();
        List<Orders> dateFilter = new ArrayList<>();
        List<Orders> monthFilter = new ArrayList<>();

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
                                            //driversFilter.add(ordersData);
                                            overviewFilter.add(ordersData);
                                            dateFilter.add(ordersData);
                                            monthFilter.add(ordersData);
                                        }

                                    }
                                }
                                Log.d("OrderIDdr", String.valueOf(driversFilter.size()));

                                DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // for loop, order ID
                                            for (int i=0; i<overviewFilter.size();i++) {
                                                Orders ordersData = overviewFilter.get(i);
                                                String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();

                                                if (snapshot.hasChild(ordersData.getOrderID())) {
                                                    String phone = snapshot.child(ordersData.getOrderID())
                                                            .child("driverPhone").getValue().toString();
                                                    if (!prePhone.equals(phone)) {
                                                        overviewFilter.remove(ordersData);
                                                        dateFilter.remove(ordersData);
                                                        monthFilter.remove(ordersData);
                                                    }

                                                }
                                                Log.d("OrderIDdr2", String.valueOf(i));
                                            }
                                            /*int i=0;
                                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                if (i<driversFilter.size()) {
                                                    Orders ordersData = driversFilter.get(i);
                                                    Log.d("OrderIDData", ordersData.getOrderID());
                                                    String deliveryID = postSnapshot.getKey();
                                                    String phone = postSnapshot.child("driverPhone").getValue(String.class);
                                                    String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();
                                                    if (ordersData.getOrderID().equals(deliveryID) && !phone.equals(prePhone)) {
                                                        driversFilter.remove(i);
                                                        if (i==0)
                                                            i=0;
                                                        else
                                                            i++;
                                                    }
                                                    //i++;
                                                }

                                            }*/
                                            /*Log.d("OrderIDdr2", String.valueOf(driversFilter.size()));
                                            for (i=0; i<driversFilter.size();i++) {
                                                Orders ordersData = driversFilter.get(i);
                                                overviewFilter.add(ordersData);
                                                dateFilter.add(ordersData);
                                                monthFilter.add(ordersData);
                                            }*/
                                            /*for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                Log.d("OrderIDPost", postSnapshot.getKey());
                                                for (int i=0; i<driversFilter.size();i++) {
                                                    Orders ordersData = driversFilter.get(i);
                                                    Log.d("OrderIDData", ordersData.getOrderID());
                                                    String deliveryID = postSnapshot.getKey();
                                                    String phone = postSnapshot.child("driverPhone").getValue(String.class);
                                                    String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();
                                                    if (ordersData.getOrderID().equals(deliveryID) && phone.equals(prePhone)) {
                                                        overviewFilter.add(ordersData);
                                                        dateFilter.add(ordersData);
                                                        monthFilter.add(ordersData);
                                                    }
                                                }


                                            }*/
                                            Log.d("OrderIDov", String.valueOf(overviewFilter.size()));
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
                                                        Intent intent = new Intent(DriverHistory.this,DriverOrderDetails.class);
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
                                                        Intent intent = new Intent(DriverHistory.this,DriverOrderDetails.class);
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
                                                        Intent intent = new Intent(DriverHistory.this,DriverOrderDetails.class);
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