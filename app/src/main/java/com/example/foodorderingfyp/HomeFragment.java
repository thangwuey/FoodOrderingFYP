package com.example.foodorderingfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderingfyp.ModelClass.Food;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ViewHolder.ProductViewHolder;


public class HomeFragment extends Fragment {

private DatabaseReference FoodRef; //create for firebase reference

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
        //FoodRef = FirebaseDatabase.getInstance().getReference().child("Food");
    }

    @Override
    public void onStart() {
        super.onStart();
        //add Class to retrive the food information
        //FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(FoodRef,Food.class).build();


    //CONTINUE VIDEO 16 19.40
    }
}