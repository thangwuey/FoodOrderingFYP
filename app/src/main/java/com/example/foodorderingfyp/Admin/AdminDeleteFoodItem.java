package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AdminDeleteFoodItem extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1001;
    private static final int GalleryPick = 1;
    private Button btnDelete;
    private TextView tvName, tvPrice, tvDesc;
    private ImageView ivFoodImage;
    private String foodimagename, downloadImageUrl;
    private String foodID = "";
    private DatabaseReference FoodsRef;
    private FirebaseStorage foodImageStorage;
    private Uri imageUri;
    private ImageView ivDeleteFoodBack; //new
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_food_item);


        btnDelete = (Button) findViewById((R.id.delete_food));
        tvName = (TextView) findViewById(R.id.food_name);
        tvPrice = (TextView) findViewById(R.id.food_price);
        tvDesc = (TextView) findViewById(R.id.food_description);
        ivFoodImage = (ImageView) findViewById(R.id.food_image);
        ivDeleteFoodBack = (ImageView) findViewById(R.id.di_back);

        // Firebase Storage image save location
        //foodImageRef = FirebaseStorage.getInstance().getReference().child("Food Images");
        foodImageStorage = FirebaseStorage.getInstance();

        foodID = getIntent().getStringExtra("foodName");

        FoodsRef = FirebaseDatabase.getInstance().getReference().child("Foods").child(foodID);
// progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // display food info
        displaySpecificFoodInfo();


        //new
        ivDeleteFoodBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Got Bug, it can GO BACK if user click PHONE BACK BUTTON
                /*Intent intent = new Intent(AdminDeleteFoodItem.this, AdminDeleteFoodMenu.class);
                startActivity(intent);*/
                //overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);

                onBackPressed();
            }
        });

        // delete food
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteThisFood();
            }
        });
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

                    downloadImageUrl = snapshot.child("foodImage").getValue().toString(); //new
                    String priceInCompleteSentence = "RM " + fprice + ".00";

                    tvName.setText(fname);
                    tvDesc.setText(fdesc);
                    tvPrice.setText(priceInCompleteSentence);
                    Picasso.get().load(fimage).into(ivFoodImage);

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

        //new
        StorageReference imageRef = foodImageStorage.getReferenceFromUrl(downloadImageUrl);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FoodsRef.removeValue();
                Toast.makeText(AdminDeleteFoodItem.this, "Item deleted", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                Intent intent = new Intent(AdminDeleteFoodItem.this, AdminDeleteFoodMenu.class);
                startActivity(intent);
                finish();
            }
        });

        /*FoodsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent intent = new Intent(AdminDeleteFoodItem.this, AdminDeleteFoodMenu.class);
                startActivity(intent);
                finish();

                Toast.makeText(AdminDeleteFoodItem.this, "The Product Is deleted successfully.", Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}