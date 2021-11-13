package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AdminDeleteFoodItem extends AppCompatActivity {
    private TextView tvName, tvPrice, tvDesc, tvPopular;
    private ImageView ivFoodImage;
    private String downloadImageUrl;
    private DatabaseReference FoodsRef;
    private FirebaseStorage foodImageStorage;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_food_item);


        Button btnDelete = findViewById((R.id.delete_food));
        tvName = findViewById(R.id.food_name);
        tvPrice = findViewById(R.id.food_price);
        tvDesc = findViewById(R.id.food_description);
        tvPopular = findViewById(R.id.food_popular);
        ivFoodImage = findViewById(R.id.food_image);
        ImageView ivDeleteFoodBack = findViewById(R.id.di_back);

        // Firebase Storage image save location
        foodImageStorage = FirebaseStorage.getInstance();

        String foodID = getIntent().getStringExtra("foodName");

        FoodsRef = FirebaseDatabase.getInstance().getReference().child("Foods").child(foodID);

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // display food info
        displaySpecificFoodInfo();


        // Back Button
        ivDeleteFoodBack.setOnClickListener(v -> onBackPressed());

        // delete food
        btnDelete.setOnClickListener(v -> deleteThisFood());
    }


    private void displaySpecificFoodInfo() {

        FoodsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // First letter of each word to UPPERCASE in FOOD NAME
                    String str = snapshot.child("foodName").getValue().toString();
                    String[] strArray = str.split(" ");
                    StringBuilder builder = new StringBuilder();
                    for (String s : strArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        builder.append(cap).append(" ");
                    }

                    String fname = builder.toString();
                    String fdesc = snapshot.child("foodDescription").getValue().toString();
                    String fprice = snapshot.child("foodPrice").getValue().toString();
                    String fimage = snapshot.child("foodImage").getValue().toString();
                    String fpopular = snapshot.child("foodPopular").getValue().toString();

                    downloadImageUrl = snapshot.child("foodImage").getValue().toString(); //new
                    String priceInCompleteSentence = "RM " + fprice + ".00";

                    tvName.setText(fname);
                    tvDesc.setText(fdesc);
                    tvPrice.setText(priceInCompleteSentence);
                    Picasso.get().load(fimage).into(ivFoodImage);

                    if (fpopular.equals("Y"))
                        tvPopular.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void deleteThisFood() {
        // loading when processing to DELETE food in database
        loadingBar.setTitle("Delete Current Product");
        loadingBar.setMessage("Dear Admin, please wait while we are deleting this product.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        StorageReference imageRef = foodImageStorage.getReferenceFromUrl(downloadImageUrl);

        // DELETE from Storage
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // DELETE from realtime database
            FoodsRef.removeValue();
            Toast.makeText(AdminDeleteFoodItem.this, "Item deleted", Toast.LENGTH_SHORT).show();

            // CLOSE loading bar
            loadingBar.dismiss();

            Intent intent = new Intent(AdminDeleteFoodItem.this, AdminDeleteFoodMenu.class);
            startActivity(intent);
            finish();
        });
    }
}