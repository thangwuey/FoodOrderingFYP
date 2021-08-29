package com.example.foodorderingfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderingfyp.ModelClass.Foods;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import ViewHolder.ProductViewHolder;


public class HomeFragment extends Fragment {

    private View MenuView;
    private RecyclerView myMenuList;

    private DatabaseReference FoodRef;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MenuView = inflater.inflate(R.layout.fragment_home, container, false);
        myMenuList = (RecyclerView) MenuView.findViewById(R.id.recycler_menu1);
        myMenuList.setLayoutManager(new LinearLayoutManager(getContext()));

        FoodRef = FirebaseDatabase.getInstance().getReference().child("Foods");//here not same

        return MenuView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Foods>().setQuery(FoodRef,Foods.class).build();
        FirebaseRecyclerAdapter<Foods, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Foods, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ProductViewHolder productViewHolder, int i, @NonNull @org.jetbrains.annotations.NotNull Foods foods)
            {
                productViewHolder.txtProductName.setText(foods.getFoodName());
                productViewHolder.txtProductDescription.setText(foods.getFoodDescription());
                productViewHolder.txtProductPrice.setText(foods.getFoodPrice());
                Picasso.get().load(foods.getFoodImage()).into(productViewHolder.imageView);
            }
            
            @NonNull
            @org.jetbrains.annotations.NotNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                //Display the design layout for the recycler view
                //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_menu_layout,viewGroup,false);
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_menu_layout,parent,false);
                ProductViewHolder viewHolder = new ProductViewHolder(view);
                return viewHolder;
            }
        };

        myMenuList.setAdapter(adapter);
        adapter.startListening();
    }
}