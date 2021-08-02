package com.example.foodorderingfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ViewHolder.ProductViewHolder;


public class HomeFragment extends Fragment {

private DatabaseReference FoodRef;

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

        //FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(FoodRef,Food.class).build();
    //CONTINUE VIDEO 16 19.40
    }
}