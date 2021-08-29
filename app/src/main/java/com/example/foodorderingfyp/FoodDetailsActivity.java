package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.foodorderingfyp.ModelClass.Foods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import Prevalent.Prevalent;

public class FoodDetailsActivity extends AppCompatActivity {

    private Button addToCartButton;
    private ImageView foodImageDetails;
    private ElegantNumberButton numberButton;
    private TextView foodPrice,foodDescription,foodName;
    private  String foodID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        foodID = getIntent().getStringExtra("foodName");

        addToCartButton = (Button) findViewById((R.id.addToCartButton));
        numberButton = (ElegantNumberButton) findViewById(R.id.foodNumber_button);
        foodImageDetails = (ImageView) findViewById(R.id.food_image_details);
        foodName = (TextView) findViewById(R.id.food_name_details);
        foodDescription = (TextView) findViewById(R.id.food_description_details);
        foodPrice = (TextView) findViewById(R.id.food_price_details);

        getFoodDetails(foodID);

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addingToCartList();
            }
        });
    }

    private void addingToCartList()
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        //Video 22
        final HashMap<String,Object> cartMap = new HashMap<>();
        cartMap.put("foodName",foodID);
        cartMap.put("foodName",foodName.getText().toString());
        cartMap.put("foodPrice",foodPrice.getText().toString());
        cartMap.put("date",saveCurrentDate);
        cartMap.put("time",saveCurrentTime);
        cartMap.put("quantity",numberButton.getNumber());

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods").child(foodID).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone()).child("Foods").child(foodID).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(FoodDetailsActivity.this,"Added to Cart List", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(FoodDetailsActivity.this, MainActivity2.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
    }

    private void getFoodDetails(String foodID)
    {
        DatabaseReference foodRef = FirebaseDatabase.getInstance().getReference().child("Foods");
        foodRef.child(foodID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    Foods foods = snapshot.getValue(Foods.class);
                    foodName.setText(foods.getFoodName());
                    foodDescription.setText(foods.getFoodDescription());
                    foodPrice.setText(foods.getFoodPrice());
                    Picasso.get().load(foods.getFoodImage()).into(foodImageDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}