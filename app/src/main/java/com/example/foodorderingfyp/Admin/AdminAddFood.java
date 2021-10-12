package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private String tname, tdesc; //new, for validation
    private int nPrice; //new
    private ImageView ivAddFoodBack; //new


    private StorageReference foodImageRef;
    private DatabaseReference foodRef;
    private ProgressDialog loadingBar;

    private static final int PERMISSION_CODE = 1001;

    private static final int GalleryPick = 1;

    private static final int STORAGE_PERMISSION_CODE = 1;

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
        ivAddFoodBack = (ImageView) findViewById(R.id.af_back); //new

        // Firebase Storage image save location
        foodImageRef = FirebaseStorage.getInstance().getReference().child("Food Images");

        // Firebase Realtime Database save location
        foodRef = FirebaseDatabase.getInstance().getReference().child("Foods");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        //new
        ivAddFoodBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdminAddFood.this, AdminDeleteFoodMenu.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
            }
        });

        // Choose Image button
        btnChooseFoodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
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

    // new, tell user why need permission
    private void requestStoragePermission() {

        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to access your phone photo gallery")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AdminAddFood.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                    STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new
                    String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
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

        /*switch (requestCode){
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
        }*/

        //new
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was GRANTED
                pickImageFromGallery();
            } else {

                // permission was DENIED
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
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

        //new, for validation
        tname = fname.trim();
        tdesc = fdesc.trim();


        if (imageUri == null)
        {
            Toast.makeText(this, "Please choose Food Image", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(tname))
        {
            Toast.makeText(this, "Please enter Name", Toast.LENGTH_SHORT).show();
            showKeyboard(inputFoodName, this);
        }
        else if (TextUtils.isEmpty(tdesc))
        {
            Toast.makeText(this, "Please enter Description", Toast.LENGTH_SHORT).show();
            showKeyboard(inputFoodDescription, this);
        }
        else if (TextUtils.isEmpty(fprice))
        {
            Toast.makeText(this, "Please enter Price", Toast.LENGTH_SHORT).show();
            showKeyboard(inputFoodPrice, this);
        }
        else
        {
            nPrice = Integer.parseInt(fprice);

            if (nPrice == 0 || nPrice < 1) {
                Toast.makeText(this, "Price cannot be 0", Toast.LENGTH_SHORT).show();
                showKeyboard(inputFoodPrice, this);
            }
            else {
                fprice = String.valueOf(nPrice);
                StorageFoodInformation();
            }
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

    //new
    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void hideSoftKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);

    }
}