package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.foodorderingfyp.ModelClass.Foods;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import ViewHolder.ProductViewHolder;

public class MainActivity2 extends AppCompatActivity {

    private DatabaseReference FoodRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    boolean shouldExit = false; // press back button to exit
    Snackbar snackbar;
    RelativeLayout relativeLayout; // Snack Bar purpose


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

        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Foods>().setQuery(FoodRef,Foods.class).build();
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
        recyclerView.setAdapter(adapter);
        adapter.startListening();
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