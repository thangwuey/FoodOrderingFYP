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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AdminAddFood extends AppCompatActivity {

    private ImageView inputFoodImage;
    private EditText inputFoodName, inputFoodDescription, inputFoodPrice;
    private Uri imageUri;
    private String fname,fdesc,fprice, foodkey;
    private String downloadImageUrl;
    private int deniedCount = 0;
    private StorageReference foodImageRef;
    private DatabaseReference foodRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private String foodCat;
    private String foodPopularYesNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_food);


        // Views
        inputFoodImage = findViewById(R.id.food_image);
        Button btnChooseFoodImage = findViewById(R.id.choose_food_image);
        inputFoodName = findViewById(R.id.food_name);
        inputFoodDescription = findViewById(R.id.food_description);
        inputFoodPrice = findViewById(R.id.food_price);
        Button btnAddFood = findViewById(R.id.add_new_food);
        ImageView ivAddFoodBack = findViewById(R.id.af_back);
        RadioGroup radioGroup = findViewById(R.id.aaf_radio_group);
        RadioButton radioFood = findViewById(R.id.aaf_radio_food);
        RadioButton radioDrink = findViewById(R.id.aaf_radio_drink);
        RadioGroup radioGroup1 = findViewById(R.id.aaf_radio_group1);
        RadioButton radioYes = findViewById(R.id.aaf_radio_yes);
        RadioButton radioNo = findViewById(R.id.aaf_radio_no);

        // Firebase Storage image save location
        foodImageRef = FirebaseStorage.getInstance().getReference().child("Food Images");

        // Firebase Realtime Database save location
        foodRef = FirebaseDatabase.getInstance().getReference().child("Foods");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // Back Button
        ivAddFoodBack.setOnClickListener(v -> onBackPressed());

        // Choose Image button
        btnChooseFoodImage.setOnClickListener(v -> requestStoragePermission());

        // Add Food Button
        btnAddFood.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId()==radioFood.getId())
                foodCat = radioFood.getText().toString();
            else
                foodCat = radioDrink.getText().toString();

            if (radioGroup1.getCheckedRadioButtonId()==radioYes.getId())
                foodPopularYesNo = "Y";
            else
                foodPopularYesNo = "N";

            ValidateFoodData();
        });
    }

    // Tell user why need permission
    private void requestStoragePermission() {

        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This app needs access to your gallery")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(AdminAddFood.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else { // first time ask permission
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

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was GRANTED
                pickImageFromGallery();
            } else if (deniedCount > 1) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app may not work correctly without the requested permissions. " +
                                "Open the app settings screen to modify app permissions.")
                        .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                // permission was DENIED
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                deniedCount++;
            }
        }
    }


    // handle result of pick image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

        String tname = fname.trim();
        String tdesc = fdesc.trim();

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
            int nPrice = Integer.parseInt(fprice);

            if (nPrice < 1) {
                Toast.makeText(this, "Price cannot be 0", Toast.LENGTH_SHORT).show();
                showKeyboard(inputFoodPrice, this);
            }
            else {
                fprice = String.valueOf(nPrice);
                checkExistFood();
            }
        }
    }

    // Validation check existing food name
    private void checkExistFood() {
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(fname.toLowerCase()).exists())
                    Toast.makeText(AdminAddFood.this, "Food Name already existed, Please change food name", Toast.LENGTH_SHORT).show();
                else
                    StorageFoodInformation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void StorageFoodInformation() {
        // loading when processing to add food to database
        loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new product.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        // food primary key, unique id
        foodkey = fname;

        // food name without spacing, newline in database
        String foodimagename = fname.replaceAll("\\s+", "");


        // Firebase Storage food image name
        final StorageReference filepath = foodImageRef.child(foodimagename + ".jpg");

        // UploadTask can control events for success, progress and failure.
        // .putFile() = upload image from local file
        final UploadTask uploadTask = filepath.putFile(imageUri);


        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(AdminAddFood.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            downloadImageUrl = filepath.getDownloadUrl().toString();
            return filepath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadImageUrl = task.getResult().toString();
                SaveFoodInfoToDatabase();
            }
        }));
    }

    private void SaveFoodInfoToDatabase() {
        HashMap<String, Object> foodMap = new HashMap<>();

        foodMap.put("foodName", fname.toLowerCase());
        foodMap.put("foodDescription", fdesc);
        foodMap.put("foodPrice", fprice);
        foodMap.put("foodImage", downloadImageUrl);
        foodMap.put("foodCategory", foodCat);
        foodMap.put("foodPopular", foodPopularYesNo);

        foodRef.child(foodkey.toLowerCase()).updateChildren(foodMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(AdminAddFood.this, "Food Added", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(AdminAddFood.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                    finish();
                });
    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}