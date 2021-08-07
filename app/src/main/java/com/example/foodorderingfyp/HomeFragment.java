package com.example.foodorderingfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderingfyp.ModelClass.Foods;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

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
        FoodRef = FirebaseDatabase.getInstance().getReference().child("Foods");//firebase header table name

        //recyclerView = findViewById(R.id.recycler_menu); //video 16 24.35
        recyclerView = recyclerView.findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    public void onStart() {
        super.onStart();
        //add Class to retrieve the food information
        FirebaseRecyclerOptions<Foods> options = new FirebaseRecyclerOptions.Builder<Foods>().setQuery(FoodRef,Foods.class).build();

        //adapter to connect to the firebase
        FirebaseRecyclerAdapter<Foods, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Foods, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ProductViewHolder holder, int position, @NonNull @NotNull Foods model)
            {
                //display data on the text field
                holder.txtProductName.setText(model.getFoodImage());
                holder.txtProductDescription.setText(model.getFoodDescription());
                holder.txtProductPrice.setText("Price = " + model.getFoodPrice() + "RM");
                Picasso.get().load(model.getFoodImage()).into(holder.imageView);//import dependency to retrieve image from firebase video 16 23.26
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
    }
}