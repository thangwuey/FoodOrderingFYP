package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.foodorderingfyp.ModelClass.Drivers;
import com.example.foodorderingfyp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ViewHolder.DriverListViewHolder;

public class AdminManageDriver extends AppCompatActivity {
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference DriversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_driver);

        DriversRef = FirebaseDatabase.getInstance().getReference().child("Drivers");

        // Food List
        recyclerView = findViewById(R.id.rv_recycleView_driver);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fabAddDriver = findViewById(R.id.fab_add_driver);
        ImageView ivBack = findViewById(R.id.amd_back);

        fabAddDriver.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManageDriver.this, AdminAddDriver.class);
            startActivity(intent);
        });

        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // use LIST instead of Firebase
        List<Drivers> driversFilter = new ArrayList<>();

        // Driver
        DriversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Drivers driversData = postSnapshot.getValue(Drivers.class);
                        driversFilter.add(driversData);
                    }

                    // Driver List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<DriverListViewHolder> driversAdapter = new RecyclerView.Adapter<DriverListViewHolder>() {
                        @NonNull
                        @Override
                        public DriverListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.driver_list_layout, parent, false);
                            return new DriverListViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull DriverListViewHolder holder, int position) {

                            // holder from DriverListViewHolder.java
                            holder.txtDriverName.setText(driversFilter.get(position).getDriverName());
                            holder.txtDriverPhone.setText(driversFilter.get(position).getDriverPhone());

                            // get driver Phone
                            holder.itemView.setOnClickListener((view) -> {
                                Intent intent = new Intent(AdminManageDriver.this,AdminDeleteDriver.class);
                                intent.putExtra("driverPhone", driversFilter.get(position).getDriverPhone());
                                startActivity(intent);
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return driversFilter.size();
                        }
                    };

                    recyclerView.setAdapter(driversAdapter);

                }
            }



            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}