package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.android.gms.tasks.OnCompleteListener;
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
    private EditText editname, editprice, editdesc;
    private ImageView editimageview;
    private String foodimagename, downloadImageUrl;
    private String foodID = "";
    private DatabaseReference FoodsRef;
    private StorageReference foodImageRef;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_food_item);


        btnDelete = (Button) findViewById((R.id.delete_food));
        editname = (EditText) findViewById(R.id.food_name);
        editprice = (EditText) findViewById(R.id.food_price);
        editdesc = (EditText) findViewById(R.id.food_description);
        editimageview = (ImageView) findViewById(R.id.food_image);

        // Firebase Storage image save location
        foodImageRef = FirebaseStorage.getInstance().getReference().child("Food Images");

        foodID = getIntent().getStringExtra("foodName");

        FoodsRef = FirebaseDatabase.getInstance().getReference().child("Foods").child(foodID);


        // display food info
        displaySpecificFoodInfo();


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
                    String fname = snapshot.child("foodName").getValue().toString();
                    String fdesc = snapshot.child("foodDescription").getValue().toString();
                    String fprice = snapshot.child("foodPrice").getValue().toString();
                    String fimage = snapshot.child("foodImage").getValue().toString();


                    editname.setText(fname);
                    editdesc.setText(fdesc);
                    editprice.setText(fprice);
                    Picasso.get().load(fimage).into(editimageview);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void deleteThisFood() {

        FoodsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent intent = new Intent(AdminDeleteFoodItem.this, AdminDeleteFoodMenu.class);
                startActivity(intent);
                finish();

                Toast.makeText(AdminDeleteFoodItem.this, "The Product Is deleted successfully.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}