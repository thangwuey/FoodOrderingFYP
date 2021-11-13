package com.example.foodorderingfyp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class TrackDelivery extends AppCompatActivity {

    private ImageView ivTrackDeliveryBack;
    private DatabaseReference OrdersRef;
    private RecyclerView currentRecyclerView, pastRecyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_delivery);

        // Database Reference Orders
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());

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


        ivTrackDeliveryBack = findViewById(R.id.td_back);

        ivTrackDeliveryBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Got Bug, it can GO BACK if user click PHONE BACK BUTTON
                /*Intent intent = new Intent(TrackDelivery.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);*/

                onBackPressed();
            }
        });
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

        Date result = calendarA.getTime();
        return result;
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

                        // Add to list, if State is P or S
                        if (ordersData.getState().equals("P") || ordersData.getState().equals("S"))
                            currentOrdersFilter.add(ordersData);

                    }

                }
                Log.d("OrderID", String.valueOf(currentOrdersFilter.size()));

                // Sorting, LATEST date and time will be FIRST
                Collections.sort(currentOrdersFilter, (o1, o2) -> {
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
                RecyclerView.Adapter<TrackDeliveryViewHolder> mAdapter = new RecyclerView.Adapter<TrackDeliveryViewHolder>() {
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
                            Date hourMinute = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(currentOrdersFilter.get(position).getTime());
                            SimpleDateFormat formatter_to = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

                            String strOrderID = "Order No : " + currentOrdersFilter.get(position).getOrderID();
                            String strTime = "Time : " + currentOrdersFilter.get(position).getDate() + ", " + formatter_to.format(hourMinute);
                            String strAmount = "Amount : " + currentOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                            // holder from TrackDeliveryViewHolder.java
                            holder.txtTrackOrderID.setText(strOrderID);
                            holder.txtTrackOrderTime.setText(strTime);
                            holder.txtTrackOrderAmount.setText(strAmount);

                            if (currentOrdersFilter.get(position).getState().equals("P")) {
                                /*holder.btnTrack.setText("Cannot Track");
                                holder.btnTrack.setBackgroundColor(Color.parseColor("#B6B7B5"));
                                //ContextCompat.getColor(TrackDelivery.this, R.color.gray);
                                //holder.btnTrack.setBackgroundColor(ContextCompat.getColor(TrackDelivery.this, R.color.gray));
                                //holder.btnTrack.setBackgroundResource(R.drawable.bg_order_state_prepare);
                                holder.btnTrack.setTextColor(Color.parseColor("#FFFFFF"));
                                holder.btnTrack.setEnabled(false);*/

                                holder.btnTrack.setVisibility(View.GONE);
                                holder.btnCannotTrack.setVisibility(View.VISIBLE);
                            } else {
                                //holder.btnTrack.setBackgroundColor(Color.parseColor("#00AD6B"));
                                holder.btnTrack.setVisibility(View.VISIBLE);
                                holder.btnCannotTrack.setVisibility(View.GONE);
                            }

                            // Track GPS Location
                            holder.btnCannotTrack.setOnClickListener((view) -> {
                                Toast.makeText(TrackDelivery.this, "Order is preparing. Please wait for awhile", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(TrackDelivery.this, "GPS is Required, Please Turn On", Toast.LENGTH_SHORT).show();
                            });

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        /*String strOrderID = "Order No : " + currentOrdersFilter.get(position).getOrderID();
                        String strTime = "Time : " + currentOrdersFilter.get(position).getDate() + ", " + currentOrdersFilter.get(position).getTime();
                        String strAmount = "Amount : " + currentOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                        // holder from TrackDeliveryViewHolder.java
                        holder.txtTrackOrderID.setText(strOrderID);
                        holder.txtTrackOrderTime.setText(strTime);
                        holder.txtTrackOrderAmount.setText(strAmount);

                        if (currentOrdersFilter.get(position).getState().equals("P")) {
                            holder.btnTrack.setText("Cannot Track");
                            holder.btnTrack.setBackgroundColor(Color.parseColor("#B6B7B5"));
                            //ContextCompat.getColor(TrackDelivery.this, R.color.gray);
                            //holder.btnTrack.setBackgroundColor(ContextCompat.getColor(TrackDelivery.this, R.color.gray));
                            //holder.btnTrack.setBackgroundResource(R.drawable.bg_order_state_prepare);
                            holder.btnTrack.setTextColor(Color.parseColor("#FFFFFF"));
                        } else {
                            holder.btnTrack.setBackgroundColor(Color.parseColor("#00AD6B"));
                        }*/
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

                        // Add to list, if State is P
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
                            Date hourMinute = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(pastOrdersFilter.get(position).getTime());
                            SimpleDateFormat formatter_to = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

                            String strOrderID = "Order No : " + pastOrdersFilter.get(position).getOrderID();
                            String strTime = "Order Date : " + pastOrdersFilter.get(position).getDate() + ", " + formatter_to.format(hourMinute);
                            String strAmount = "Amount : " + pastOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                            // holder from TrackDeliveryViewHolder.java
                            holder.txtTrackOrderID.setText(strOrderID);
                            holder.txtTrackOrderTime.setText(strTime);
                            holder.txtTrackOrderAmount.setText(strAmount);

                            holder.btnTrack.setVisibility(View.GONE);


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // NOT GOOD, No need SECOND
                        /*String strOrderID = "Order No : " + pastOrdersFilter.get(position).getOrderID();
                        String strTime = "Order Date : " + pastOrdersFilter.get(position).getDate() + ", " + pastOrdersFilter.get(position).getTime();
                        String strAmount = "Amount : " + pastOrdersFilter.get(position).getTotalAmount() + ".00 MYR";

                        // holder from TrackDeliveryViewHolder.java
                        holder.txtTrackOrderID.setText(strOrderID);
                        holder.txtTrackOrderTime.setText(strTime);
                        holder.txtTrackOrderAmount.setText(strAmount);

                        holder.btnTrack.setVisibility(View.GONE);*/
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

        /*// to configure adapter
        FirebaseRecyclerOptions<Orders> options =
                new FirebaseRecyclerOptions.Builder<Orders>()
                        .setQuery(OrdersRef, Orders.class)
                        .build();
        Log.d("userphone", Prevalent.currentOnlineUser.getPhone());


        // <T, ViewHolder>
        FirebaseRecyclerAdapter<Orders, TrackDeliveryViewHolder> adapter =
                new FirebaseRecyclerAdapter<Orders, TrackDeliveryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull TrackDeliveryViewHolder holder, int position, @NonNull Orders model)
                    {
                        if (model.getState().equals("S")) {
                            String strAmount = model.getTotalAmount() + ".00 MYR";

                            // holder from TrackDeliveryViewHolder.java
                            holder.txtTrackOrderID.setText(model.getOrderID());
                            holder.txtTrackOrderTime.setText(model.getTime());
                            holder.txtTrackOrderAmount.setText(strAmount);
                        }

                        //holder.btnArrow.setBackgroundResource(R.drawable.ic_show_more);

                        // get Order ID
                        holder.cvCardView.setOnClickListener((view) -> {
                            Intent intent = new Intent(AdminSendOrder.this,AdminDeleteFoodItem.class);
                            intent.putExtra("orderID", model.getOrderID());
                            startActivity(intent);
                            if (holder.rlExpandableLayout.getVisibility()==View.GONE) {
                                TransitionManager.beginDelayedTransition(holder.cvCardView, new AutoTransition());
                                holder.rlExpandableLayout.setVisibility(View.VISIBLE);
                                holder.btnArrow.setBackgroundResource(R.drawable.ic_show_less);
                            } else {
                                TransitionManager.beginDelayedTransition(holder.cvCardView, new AutoTransition());
                                holder.rlExpandableLayout.setVisibility(View.GONE);
                                holder.btnArrow.setBackgroundResource(R.drawable.ic_show_more);
                            }

                        });
                    }

                    @NonNull
                    @Override   // copy and create view into recyclerView from track_delivery_item_layout.xml
                    public TrackDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_delivery_item_layout, parent, false);
                        TrackDeliveryViewHolder holder = new TrackDeliveryViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();*/
    }
}