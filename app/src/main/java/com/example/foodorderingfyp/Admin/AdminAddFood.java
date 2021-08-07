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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AdminAddFood extends AppCompatActivity {

    private ImageView inputFoodImage;
    private EditText inputFoodName, inputFoodDescription, inputFoodPrice;
    private Button btnChooseFoodImage, btnAddFood;
    private Uri imageUri;
    private String fname, fdesc, fprice, foodimagename, foodkey, downloadImageUrl;

    private StorageReference foodImageRef;
    private DatabaseReference foodRef;
    private ProgressDialog loadingBar;

    private static final int PERMISSION_CODE = 1001;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_food);


        // Views
        inputFoodImage = (ImageView) findViewById(R.id.food_image);
        btnChooseFoodImage = (Button) findViewById(R.id.choose_food_image);
        inputFoodName = (EditText) findViewById(R.id.food_name);
        inputFoodDescription = (EditText) findViewById(R.id.food_description);
        inputFoodPrice = (EditText) findViewById(R.id.food_price);
        btnAddFood = (Button) findViewById(R.id.add_new_food);

        // Firebase Storage image save location
        foodImageRef = FirebaseStorage.getInstance().getReference().child("Food Images");

        // Firebase Realtime Database save location
        foodRef = FirebaseDatabase.getInstance().getReference().child("Foods");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);


        // Choose Image button
        btnChooseFoodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check runtime permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                    {
                        // permission is not granted
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

                        // show popup for runtime permission
                        requestPermissions(permissions, PERMISSION_CODE);

                    }
                    else
                    {
                        // permission already granted
                        pickImageFromGallery();
                    }
                }
                else
                {
                    // system os is less than marshmallow
                    pickImageFromGallery();
                }
            }
        });

        // Add Food Button
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateFoodData();
            }
        });
    }


    private void pickImageFromGallery(){

        // select image from image, camera
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }


    // handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission was granted
                    pickImageFromGallery();
                }
                else{
                    // permission was denied
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    // handle result of pick image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data!=null && data.getData()!=null){
        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null  && data.getData()!=null){
            // get image uri
            imageUri = data.getData();

            // set image to image view
            inputFoodImage.setImageURI(imageUri);


        }
    }


    // Validation check null
    private void ValidateFoodData(){
        fname = inputFoodName.getText().toString();
        fdesc = inputFoodDescription.getText().toString();
        fprice = inputFoodPrice.getText().toString();

        if (imageUri == null)
        {
            Toast.makeText(this, "Food Image is mandatory...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fdesc))
        {
            Toast.makeText(this, "Please write food description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fname))
        {
            Toast.makeText(this, "Please write food name...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fprice))
        {
            Toast.makeText(this, "Please write food price...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            StorageFoodInformation();
        }
    }


    private void StorageFoodInformation() {
        // loading when processing to add food to database
        /*loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new product.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();*/


        // food primary key, unique id
        foodkey = fname;

        // food name without spacing, newline in database
        foodimagename = fname.replaceAll("\\s+", "");


        // Firebase Storage food image name
        final StorageReference filepath = foodImageRef.child(foodimagename + ".jpg");

        // UploadTask can control events for success, progress and failure.
        // .putFile() = upload image from local file
        final UploadTask uploadTask = filepath.putFile(imageUri);


        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String message = e.toString();
                Toast.makeText(AdminAddFood.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                //loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast.makeText(AdminAddFood.this, "Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        downloadImageUrl = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            downloadImageUrl = task.getResult().toString();

                            Toast.makeText(AdminAddFood.this, "got the Food image Url Successfully...", Toast.LENGTH_SHORT).show();

                            SaveFoodInfoToDatabase();
                        }
                    }
                });
            }
        });
    }



    private void SaveFoodInfoToDatabase() {
        HashMap<String, Object> foodMap = new HashMap<>();



        foodMap.put("foodName", fname);
        foodMap.put("foodDescription", fdesc);
        foodMap.put("foodPrice", fprice);
        foodMap.put("foodImage", downloadImageUrl);

        foodRef.child(foodkey).updateChildren(foodMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            //loadingBar.dismiss();
                            Toast.makeText(AdminAddFood.this, "Food is added successfully...", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(AdminAddFood.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}