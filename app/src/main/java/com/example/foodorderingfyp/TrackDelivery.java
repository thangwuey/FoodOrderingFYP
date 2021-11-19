package com.example.foodorderingfyp;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Orders;
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

import Prevalent.Prevalent;
import ViewHolder.TrackDeliveryViewHolder;

public class TrackDelivery extends AppCompat {

    private DatabaseReference OrdersRef;
    private RecyclerView currentRecyclerView, pastRecyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_delivery);

        // Database Reference Orders
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());

        // current order
        currentRecyclerView = findViewById(R.id.td_current_rv);
        currentRecyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        currentRecyclerView.setLayoutManager(layoutManager);

        // past order
        pastRecyclerView = findViewById(R.id.td_pass_rv);
        pastRecyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        pastRecyclerView.setLayoutManager(layoutManager);


        ImageView ivTrackDeliveryBack = findViewById(R.id.td_back);

        ivTrackDeliveryBack.setOnClickListener(v -> onBackPressed());
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

        // Current Orders, TRACKING Purpose
        List<Orders> currentOrdersFilter = new ArrayList<>();

        OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {
                    // for loop, store specific Orders to currentOrdersFilter
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Log.d("OrderID",postSnapshot.getKey());
                        Orders ordersData = postSnapshot.getValue(Orders.class);
                        Log.d("orderstate123",ordersData.getState());

                        // Add to list, if State is P or S or R
                        if (ordersData.getState().equals("P") || ordersData.getState().equals("S") ||
                                ordersData.getState().equals("R"))
                            currentOrdersFilter.add(ordersData);
                    }

                }
                Log.d("OrderID", String.valueOf(currentOrdersFilter.size()));

                // Sorting, LATEST date and time will be FIRST
                Collections.sort(currentOrdersFilter, (o1, o2) -> {
                    try {
                        // String to Date
                        Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                                .parse(o1.getDate());
                        Date time1 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                                .parse(o1.getTime());
                        Date date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                                .parse(o2.getDate());
                        Date time2 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                                .parse(o2.getTime());

                        //Combine Date and Time to DateTime
                        Date dt1 = combineDateTime(date1, time1);
                        Date dt2 = combineDateTime(date2, time2);

                        return dt2.compareTo(dt1);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                // Adapter instead of Firebase Adapter (different declaration)
                RecyclerView.Adapter<TrackDeliveryViewHolder> mAdapter = new
                        RecyclerView.Adapter<TrackDeliveryViewHolder>() {
                            @NonNull
                            @Override
                            public TrackDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.track_delivery_item_layout, parent, false);
                                return new TrackDeliveryViewHolder(view);
                            }

                            @Override
                            public void onBindViewHolder(@NonNull TrackDeliveryViewHolder holder, int position) {

                                try {
                                    // String time only HOUR, MINUTE
                                    Date hourMinute = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                                            .parse(currentOrdersFilter.get(position).getTime());
                                    SimpleDateFormat formatter_to = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

                                    String strOrderID = "Order No : " + currentOrdersFilter.get(position).getOrderID();
                                    String strTime = "Time : " + currentOrdersFilter.get(position).getDate() +
                                            ", " + formatter_to.format(hourMinute);
                                    String strAmount = "Amount : " + currentOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                                    // holder from TrackDeliveryViewHolder.java
                                    holder.txtTrackOrderID.setText(strOrderID);
                                    holder.txtTrackOrderTime.setText(strTime);
                                    holder.txtTrackOrderAmount.setText(strAmount);

                                    if (currentOrdersFilter.get(position).getState().equals("P") ||
                                            currentOrdersFilter.get(position).getState().equals("R")) {
                                        holder.btnTrack.setVisibility(View.GONE);
                                        holder.btnCannotTrack.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.btnTrack.setVisibility(View.VISIBLE);
                                        holder.btnCannotTrack.setVisibility(View.GONE);
                                    }
                                    holder.btnViewDetails.setVisibility(View.VISIBLE);

                                    // Cannot Track
                                    holder.btnCannotTrack.setOnClickListener((view) ->
                                            Toast.makeText(TrackDelivery.this,
                                                    "Order is preparing. Please wait for awhile",
                                                    Toast.LENGTH_SHORT).show());

                                    // View Details
                                    holder.btnViewDetails.setOnClickListener((view) -> {
                                        Intent intent = new Intent(TrackDelivery.this, UserOrderDetails.class);
                                        intent.putExtra("orderID", currentOrdersFilter.get(position).getOrderID());
                                        startActivity(intent);
                                    });

                                    // Track GPS Location
                                    holder.btnTrack.setOnClickListener((view) -> {
                                        // check Phone GPS state
                                        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
                                        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                                        if (statusOfGPS) {
                                            //Intent intent = new Intent(TrackDelivery.this, TrackMapsActivity.class);
                                            Intent intent = new Intent(TrackDelivery.this, TrackGPSMapActivity.class);
                                            intent.putExtra("orderID", currentOrdersFilter.get(position).getOrderID());
                                            intent.putExtra("latitude", currentOrdersFilter.get(position).getLatitude());
                                            intent.putExtra("longitude", currentOrdersFilter.get(position).getLongitude());
                                            intent.putExtra("address", currentOrdersFilter.get(position).getAddress());
                                            startActivity(intent);
                                        }
                                        else
                                            Toast.makeText(TrackDelivery.this,
                                                    "GPS is Required, Please Turn On", Toast.LENGTH_SHORT).show();
                                    });

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public int getItemCount() {
                                return currentOrdersFilter.size();
                            }
                        };

                currentRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        // Past Orders, HISTORY
        List<Orders> pastOrdersFilter = new ArrayList<>();

        OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {

                    // for loop, store specific Orders to pastOrdersFilter
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Log.d("OrderID",postSnapshot.getKey());
                        Orders ordersData = postSnapshot.getValue(Orders.class);
                        Log.d("orderstate123",ordersData.getState());

                        // Add to list, if State is D
                        if (ordersData.getState().equals("D"))
                            pastOrdersFilter.add(ordersData);
                    }

                }
                Log.d("OrderID", String.valueOf(pastOrdersFilter.size()));

                // Sorting, LATEST date and time will be FIRST
                Collections.sort(pastOrdersFilter, (o1, o2) -> {
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
                RecyclerView.Adapter<TrackDeliveryViewHolder> pAdapter = new RecyclerView.Adapter<TrackDeliveryViewHolder>() {
                    @NonNull
                    @Override
                    public TrackDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.track_delivery_item_layout, parent, false);
                        return new TrackDeliveryViewHolder(view);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull TrackDeliveryViewHolder holder, int position) {

                        try {
                            // String time only HOUR, MINUTE
                            Date hourMinute = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                                    .parse(pastOrdersFilter.get(position).getTime());
                            SimpleDateFormat formatter_to = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

                            String strOrderID = "Order No : " + pastOrdersFilter.get(position).getOrderID();
                            String strTime = "Order Date : " + pastOrdersFilter.get(position).getDate() +
                                    ", " + formatter_to.format(hourMinute);
                            String strAmount = "Amount : " + pastOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                            // holder from TrackDeliveryViewHolder.java
                            holder.txtTrackOrderID.setText(strOrderID);
                            holder.txtTrackOrderTime.setText(strTime);
                            holder.txtTrackOrderAmount.setText(strAmount);

                            holder.btnTrack.setVisibility(View.GONE);
                            holder.btnCannotTrack.setVisibility(View.GONE);
                            holder.btnViewDetails.setVisibility(View.VISIBLE);

                            holder.btnViewDetails.setOnClickListener((view) -> {
                                Intent intent = new Intent(TrackDelivery.this, UserOrderDetails.class);
                                intent.putExtra("orderID", pastOrdersFilter.get(position).getOrderID());
                                startActivity(intent);
                            });

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public int getItemCount() {
                        return pastOrdersFilter.size();
                    }
                };

                pastRecyclerView.setAdapter(pAdapter);
                pAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}