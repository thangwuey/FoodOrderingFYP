package com.example.foodorderingfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderingfyp.ModelClass.Food;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import ViewHolder.ProductViewHolder;


public class HomeFragment extends Fragment {

private DatabaseReference FoodRef; //create for firebase reference
private RecyclerView recyclerView;
RecyclerView.LayoutManager layoutManager;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);// THIS STATEMENT OF CODE MAYBE NO NEED
        //FoodRef = FirebaseDatabase.getInstance().getReference().child("Food");//firebase header table name

        /*recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);*/

    }

    //@Override
    /*public void onStart() {
        super.onStart();
        //add Class to retrive the food information
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(FoodRef,Food.class).build();

        //adapter to connect to the firebase
        FirebaseRecyclerAdapter<Food,ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Food, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ProductViewHolder holder, int position, @NonNull @NotNull Food model)
            {
                //display data on the text field
                holder.txtProductName.setText(model.getFoodName());
                holder.txtProductDescription.setText(model.getFoodName());
                holder.txtProductPrice.setText("Price = " + model.getFoodName() + "RM");
                Picasso.get().load(model.getImage().into(holder.imageView));//import dependenct to retireve image from firebase
            }

            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_menu_layout,parent,false);
                ProductViewHolder holder = new ProductViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }*/
}