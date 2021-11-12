package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import Prevalent.Prevalent;
import ViewHolder.CartViewHolder;

public class CartActivity extends AppCompat {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button placeOrderButton;
    private TextView txtTotalAmount;
    boolean shouldExit = false; // press back button to EXIT
    Snackbar snackbar;
    RelativeLayout relativeLayout; // Snack Bar purpose

    private int overallTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // for Snack Bar
        relativeLayout = findViewById(R.id.user_cart_layout); //new

        //bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //activity default
        bottomNav.setSelectedItemId(R.id.cart);

        //Video 23
        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        placeOrderButton = (Button) findViewById(R.id.place_order_button);
        txtTotalAmount  = (TextView) findViewById(R.id.total_price);

        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                txtTotalAmount.setText(String.valueOf(overallTotalPrice) + " .00 MYR");

                /*Intent intent = new Intent(CartActivity.this,ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overallTotalPrice));
                startActivity(intent);
                finish();*/

                // Validation, Cart cannot be EMPTY before proceed to CONFIRM ORDER
                if (overallTotalPrice <= 0) {
                    Toast.makeText(CartActivity.this, "Please add food before place order", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(CartActivity.this,ConfirmFinalOrderActivity.class);
                    intent.putExtra("Total Price", String.valueOf(overallTotalPrice));
                    startActivity(intent);
                    finish();
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
                    startActivity(new Intent(getApplicationContext(),MainActivity2.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                case R.id.cart:
                    //selectedFragment = new CartFragment();
                    //break;
                    //startActivity(new Intent(getApplicationContext(),Test1.class));
                    //overridePendingTransition(0,0);
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
        //Video 24
        super.onStart();
        //Connect database
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>().setQuery(cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods"),Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull CartViewHolder cartViewHolder, int i, @NonNull @NotNull Cart cart)
            {
                cartViewHolder.txtFoodName.setText(cart.getFoodName());
                cartViewHolder.txtFoodPrice.setText("Price: " + cart.getFoodPrice() + "MYR");
                cartViewHolder.txtFoodQuantity.setText("Quantity = " + cart.getQuantity());

                //video 26 show to total price of the all food item in the cart
                int oneTypeFoodTotalPrice = ((Integer.valueOf(cart.getFoodPrice()))) * Integer.valueOf(cart.getQuantity());
                overallTotalPrice = overallTotalPrice + oneTypeFoodTotalPrice;

                // new, show TOTAL in TOP
                txtTotalAmount.setText( "Total Price = " + String.valueOf(overallTotalPrice) + " .00 MYR");

                //Click to perform other function VIDEO 25
                cartViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Edit","Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {//Edit cart item
                                    Intent intent = new Intent(CartActivity.this,FoodDetailsActivity.class);
                                    intent.putExtra("foodName", cart.getFoodName());
                                    startActivity(intent);
                                }
                                if (which == 1)
                                { //remove cart item
                                    cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods").child(cart.getFoodName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(CartActivity.this, "Item removed successfully!", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(CartActivity.this,MainActivity2.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });

                                    /*cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods").child(cart.getFoodName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(CartActivity.this, "Item removed successfully!", Toast.LENGTH_SHORT).show();
                                                //Intent intent = new Intent(CartActivity.this,MainActivity2.class);
                                                //startActivity(intent);
                                            }
                                        }
                                    });*/
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent,false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onBackPressed() {

        // double press back button to exit app
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