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

import com.example.foodorderingfyp.ModelClass.Foods;
import com.example.foodorderingfyp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ViewHolder.AdminFoodViewHolder;

public class AdminDeleteFoodMenu extends AppCompatActivity {

    private DatabaseReference FoodsRef;
    private RecyclerView recyclerViewFood, recyclerViewDrink,recyclerViewPopular;
    RecyclerView.LayoutManager layoutManagerFood, layoutManagerDrink, layoutManagerPopular;
    RadioGroup radioGroup;
    RadioButton radioFood;
    RadioButton radioDrink;
    RadioButton radioPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_food_menu);


        // to get Database Foods table Reference
        FoodsRef = FirebaseDatabase.getInstance().getReference().child("Foods");

        // Food List
        recyclerViewFood = findViewById(R.id.rv_recycleView_food);
        recyclerViewFood.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerFood = new LinearLayoutManager(this);
        recyclerViewFood.setLayoutManager(layoutManagerFood);

        // Drink List
        recyclerViewDrink = findViewById(R.id.rv_recycleView_drink);
        recyclerViewDrink.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerDrink = new LinearLayoutManager(this);
        recyclerViewDrink.setLayoutManager(layoutManagerDrink);

        // Popular List
        recyclerViewDrink = findViewById(R.id.rv_recycleView_drink);
        recyclerViewDrink.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManagerPopular = new LinearLayoutManager(this);
        recyclerViewPopular.setLayoutManager(layoutManagerPopular);

        FloatingActionButton fabAddFood = findViewById(R.id.fab_add_food);
        ImageView ivBack = findViewById(R.id.fm_back);
        radioGroup = findViewById(R.id.adfm_radio_group);
        radioFood = findViewById(R.id.adfm_radio_food);
        radioDrink = findViewById(R.id.adfm_radio_drink);
        radioPopular = findViewById(R.id.adfm_radio_recommend);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radioGroup.getCheckedRadioButtonId()==radioFood.getId()) {
                    recyclerViewPopular.setVisibility(View.GONE);
                    recyclerViewFood.setVisibility(View.VISIBLE);
                    recyclerViewDrink.setVisibility(View.GONE);
                } else if (radioGroup.getCheckedRadioButtonId()==radioPopular.getId())
                {
                    recyclerViewFood.setVisibility(View.GONE);
                    recyclerViewDrink.setVisibility(View.GONE);
                    recyclerViewPopular.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerViewFood.setVisibility(View.GONE);
                    recyclerViewDrink.setVisibility(View.VISIBLE);
                    recyclerViewPopular.setVisibility(View.GONE);
                }
            }
        });

        // Add Food Button
        fabAddFood.setOnClickListener(v -> {

            Intent intent = new Intent(AdminDeleteFoodMenu.this, AdminAddFood.class);
            startActivity(intent);
        });

        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());
    }


    @Override
    protected void onStart() {
        super.onStart();

        /*// to configure adapter
        FirebaseRecyclerOptions<Foods> options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(FoodsRef, Foods.class)
                        .build();

        // <T, ViewHolder>
        FirebaseRecyclerAdapter<Foods, AdminFoodViewHolder> adapter =
                new FirebaseRecyclerAdapter<Foods, AdminFoodViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position, @NonNull Foods model)
                    {
                        // holder from FoodViewHolder.java
                        // First letter of each word to UPPERCASE in FOOD NAME
                        String str = model.getFoodName();
                        String[] strArray = str.split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (String s : strArray) {
                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                            builder.append(cap).append(" ");
                        }
                        holder.txtAdminFoodName.setText(builder.toString());

                        // easy way to get Image from database
                        Picasso.get().load(model.getFoodImage()).into(holder.adminImageView);

                        // get Food ID
                        holder.itemView.setOnClickListener((view) -> {
                            Intent intent = new Intent(AdminDeleteFoodMenu.this,AdminDeleteFoodItem.class);
                            intent.putExtra("foodName", model.getFoodName());
                            startActivity(intent);
                        });
                    }

                    @NonNull
                    @Override   // copy and create view into recyclerView from food_items_layout.xml
                    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_food_item_layout, parent, false);
                        return new AdminFoodViewHolder(view);
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();*/

        // use LIST instead of Firebase
        List<Foods> foodsFilter = new ArrayList<>();
        List<Foods> drinksFilter = new ArrayList<>();
        List<Foods> popularFilter = new ArrayList<>();

        // User
        FoodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Foods foodsData = postSnapshot.getValue(Foods.class);

                        // Add to list, if State is P
                        if (foodsData.getFoodCategory().equals("Food"))
                            foodsFilter.add(foodsData);
                        else
                            drinksFilter.add(foodsData);

                        if(foodsData.getFoodPopular().equals("Y"))
                            popularFilter.add(foodsData);
                    }

                    // Popular List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<AdminFoodViewHolder> popularAdapter = new RecyclerView.Adapter<AdminFoodViewHolder>() {
                        @NonNull
                        @Override
                        public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.admin_food_item_layout, parent, false);
                            return new AdminFoodViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {

                            // holder from FoodViewHolder.java
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = popularFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }
                            holder.txtAdminFoodName.setText(builder.toString());

                            // easy way to get Image from database
                            Picasso.get().load(popularFilter.get(position).getFoodImage()).into(holder.adminImageView);

                            // get Food ID
                            holder.itemView.setOnClickListener((view) -> {
                                Intent intent = new Intent(AdminDeleteFoodMenu.this,AdminDeleteFoodItem.class);
                                intent.putExtra("foodName", popularFilter.get(position).getFoodName());
                                startActivity(intent);
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return popularFilter.size();
                        }
                    };


                    // Food List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<AdminFoodViewHolder> foodsAdapter = new RecyclerView.Adapter<AdminFoodViewHolder>() {
                        @NonNull
                        @Override
                        public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.admin_food_item_layout, parent, false);
                            return new AdminFoodViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {

                            // holder from FoodViewHolder.java
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = foodsFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }
                            holder.txtAdminFoodName.setText(builder.toString());

                            // easy way to get Image from database
                            Picasso.get().load(foodsFilter.get(position).getFoodImage()).into(holder.adminImageView);

                            // get Food ID
                            holder.itemView.setOnClickListener((view) -> {
                                Intent intent = new Intent(AdminDeleteFoodMenu.this,AdminDeleteFoodItem.class);
                                intent.putExtra("foodName", foodsFilter.get(position).getFoodName());
                                startActivity(intent);
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return foodsFilter.size();
                        }
                    };

                    // Drink List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<AdminFoodViewHolder> drinksAdapter = new RecyclerView.Adapter<AdminFoodViewHolder>() {
                        @NonNull
                        @Override
                        public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.admin_food_item_layout, parent, false);
                            return new AdminFoodViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {

                            // holder from FoodViewHolder.java
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = drinksFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }
                            holder.txtAdminFoodName.setText(builder.toString());

                            // easy way to get Image from database
                            Picasso.get().load(drinksFilter.get(position).getFoodImage()).into(holder.adminImageView);

                            // get Food ID
                            holder.itemView.setOnClickListener((view) -> {
                                Intent intent = new Intent(AdminDeleteFoodMenu.this,AdminDeleteFoodItem.class);
                                intent.putExtra("foodName", drinksFilter.get(position).getFoodName());
                                startActivity(intent);
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return drinksFilter.size();
                        }
                    };

                    recyclerViewFood.setAdapter(foodsAdapter);
                    recyclerViewDrink.setAdapter(drinksAdapter);
                    recyclerViewPopular.setAdapter(popularAdapter);

                }
            }



            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}