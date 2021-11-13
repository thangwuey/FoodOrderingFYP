package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.example.foodorderingfyp.Admin.AdminDeleteFoodItem;
import com.example.foodorderingfyp.Admin.AdminDeleteFoodMenu;
import com.example.foodorderingfyp.ModelClass.Foods;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
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
import ViewHolder.ProductViewHolder;

public class MainActivity2 extends AppCompatActivity {

    private DatabaseReference FoodRef;
    private RecyclerView recyclerViewFood, recyclerViewDrink,recyclerViewPopular;
    RecyclerView.LayoutManager layoutManagerFood, layoutManagerDrink,layoutManagerPopular;
    boolean shouldExit = false; // press back button to exit
    Snackbar snackbar;
    RelativeLayout relativeLayout; // Snack Bar purpose
    RadioGroup radioGroup;
    RadioButton radioFood;
    RadioButton radioDrink;
    RadioButton radioPopular;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // for Snack Bar
        relativeLayout = findViewById(R.id.user_menu_layout); //new

        //bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //activity default
        bottomNav.setSelectedItemId(R.id.menu);

        FoodRef = FirebaseDatabase.getInstance().getReference().child("Foods");

        //define first page
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new HomeFragment()).commit();

        // Popular List
        recyclerViewPopular = findViewById(R.id.recycler_menu_popular);
        recyclerViewPopular.setHasFixedSize(true);
        layoutManagerPopular = new LinearLayoutManager(this);
        recyclerViewPopular.setLayoutManager(layoutManagerPopular);

        // Food List
        recyclerViewFood = findViewById(R.id.recycler_menu_food);
        recyclerViewFood.setHasFixedSize(true);
        layoutManagerFood = new LinearLayoutManager(this);
        recyclerViewFood.setLayoutManager(layoutManagerFood);


        // Drink List
        recyclerViewDrink = findViewById(R.id.recycler_menu_drink);
        recyclerViewDrink.setHasFixedSize(true);
        layoutManagerDrink = new LinearLayoutManager(this);
        recyclerViewDrink.setLayoutManager(layoutManagerDrink);

        radioGroup = findViewById(R.id.ufm_radio_group);
        radioFood = findViewById(R.id.ufm_radio_food);
        radioDrink = findViewById(R.id.ufm_radio_drink);
        radioPopular = findViewById(R.id.ufm_radio_popular);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radioGroup.getCheckedRadioButtonId()==radioFood.getId()) {
                    recyclerViewPopular.setVisibility(View.GONE);
                    recyclerViewFood.setVisibility(View.VISIBLE);
                    recyclerViewDrink.setVisibility(View.GONE);

                } else if(radioGroup.getCheckedRadioButtonId()==radioPopular.getId()){
                    recyclerViewPopular.setVisibility(View.VISIBLE);
                    recyclerViewFood.setVisibility(View.GONE);
                    recyclerViewDrink.setVisibility(View.GONE);
                }
                else {
                    recyclerViewPopular.setVisibility(View.GONE);
                    recyclerViewFood.setVisibility(View.GONE);
                    recyclerViewDrink.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            //Fragment selectedFragment = null;
            switch (item.getItemId()){

                case R.id.menu:
                    //selectedFragment = new HomeFragment();
                    //break;
                    //startActivity(new Intent(getApplicationContext(),Test1.class));
                    //overridePendingTransition(0,0);
                    return true;
                case R.id.cart:
                    //selectedFragment = new CartFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), CartActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.wallet:
                    //selectedFragment = new WalletFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), WalletActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.profile:
                    //selectedFragment = new ProfileFragment();
                    //break;
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;}
            return false;
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,selectedFragment).commit();
            //return true;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        /*FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Foods>().setQuery(FoodRef,Foods.class).build();
        FirebaseRecyclerAdapter<Foods, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Foods, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ProductViewHolder productViewHolder, int i, @NonNull @NotNull Foods foods) {

                String strPrice = foods.getFoodPrice() + ".00 MYR"; // price FORMAT

                productViewHolder.txtProductName.setText(foods.getFoodName());
                productViewHolder.txtProductDescription.setText(foods.getFoodDescription());
                productViewHolder.txtProductPrice.setText(strPrice);
                Picasso.get().load(foods.getFoodImage()).into(productViewHolder.imageView);

                productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(MainActivity2.this,FoodDetailsActivity.class);
                        intent.putExtra("foodName",foods.getFoodName());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_menu_layout,parent,false);
                ProductViewHolder holder = new ProductViewHolder(view);
                return holder;
            }
        };
        recyclerViewFood.setAdapter(adapter);
        adapter.startListening();*/

        // use LIST instead of Firebase
        List<Foods> foodsFilter = new ArrayList<>();
        List<Foods> drinksFilter = new ArrayList<>();
        List<Foods> popularFilter = new ArrayList<>();

        // User
        FoodRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    RecyclerView.Adapter<ProductViewHolder> popularAdapter = new RecyclerView.Adapter<ProductViewHolder>() {
                        @NonNull
                        @Override
                        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.food_menu_layout, parent, false);
                            return new ProductViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull ProductViewHolder productViewHolder,int position) {
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = popularFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }

                            String strPrice = popularFilter.get(position).getFoodPrice() + ".00 MYR"; // price FORMAT

                            productViewHolder.txtProductName.setText(builder.toString());
                            productViewHolder.txtProductDescription.setText(popularFilter.get(position).getFoodDescription());
                            productViewHolder.txtProductPrice.setText(strPrice);
                            Picasso.get().load(popularFilter.get(position).getFoodImage()).into(productViewHolder.imageView);

                            productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent intent = new Intent(MainActivity2.this,FoodDetailsActivity.class);
                                    intent.putExtra("foodName",popularFilter.get(position).getFoodName());
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return popularFilter.size();
                        }
                    };


                    // Food List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<ProductViewHolder> foodsAdapter = new RecyclerView.Adapter<ProductViewHolder>() {
                        @NonNull
                        @Override
                        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.food_menu_layout, parent, false);
                            return new ProductViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull ProductViewHolder productViewHolder, int position) {
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = foodsFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }

                            String strPrice = foodsFilter.get(position).getFoodPrice() + ".00 MYR"; // price FORMAT

                            productViewHolder.txtProductName.setText(builder.toString());
                            productViewHolder.txtProductDescription.setText(foodsFilter.get(position).getFoodDescription());
                            productViewHolder.txtProductPrice.setText(strPrice);
                            Picasso.get().load(foodsFilter.get(position).getFoodImage()).into(productViewHolder.imageView);

                            if (drinksFilter.get(position).getFoodPopular().equals("N"))
                                productViewHolder.userMenuPopular.setVisibility(View.GONE);

                            productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent intent = new Intent(MainActivity2.this,FoodDetailsActivity.class);
                                    intent.putExtra("foodName",foodsFilter.get(position).getFoodName());
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return foodsFilter.size();
                        }
                    };

                    // Drink List
                    // Adapter instead of Firebase Adapter (different declaration)
                    RecyclerView.Adapter<ProductViewHolder> drinksAdapter = new RecyclerView.Adapter<ProductViewHolder>() {
                        @NonNull
                        @Override
                        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.food_menu_layout, parent, false);
                            return new ProductViewHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull ProductViewHolder productViewHolder, int position) {
                            // First letter of each word to UPPERCASE in FOOD NAME
                            String str = drinksFilter.get(position).getFoodName();
                            String[] strArray = str.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap).append(" ");
                            }

                            String strPrice = drinksFilter.get(position).getFoodPrice() + ".00 MYR"; // price FORMAT

                            productViewHolder.txtProductName.setText(builder.toString());
                            productViewHolder.txtProductDescription.setText(drinksFilter.get(position).getFoodDescription());
                            productViewHolder.txtProductPrice.setText(strPrice);
                            Picasso.get().load(drinksFilter.get(position).getFoodImage()).into(productViewHolder.imageView);

                            if (drinksFilter.get(position).getFoodPopular().equals("N"))
                                productViewHolder.userMenuPopular.setVisibility(View.GONE);

                            productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent intent = new Intent(MainActivity2.this,FoodDetailsActivity.class);
                                    intent.putExtra("foodName",drinksFilter.get(position).getFoodName());
                                    startActivity(intent);
                                }
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

    @Override
    public void onBackPressed() {

        // DOUBLE PRESS back button to exit app
        if (shouldExit){
            snackbar.dismiss();

            // EXIT app, have BUG
            /*moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());*/

            // EXIT app, COMPLETE
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EXIT_TAG", "SINGLETASK");
            startActivity(intent);
        }else{
            snackbar = Snackbar.make(relativeLayout, "Please press BACK again to exit", Snackbar.LENGTH_SHORT);
            snackbar.show();
            shouldExit = true;

            // if NO press again in a PERIOD, will NOT EXIT
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldExit = false;
                }
            }, 1500);
        }
    }
}