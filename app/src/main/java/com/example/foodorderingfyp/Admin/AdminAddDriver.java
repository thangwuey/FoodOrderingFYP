package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
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
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminAddDriver extends AppCompatActivity {
    private EditText etName, etPhone, etPassword, etConfirmPassword, etAddress;
    private String name,phone,password,address;
    private DatabaseReference usersRef, adminsRef, DriversRef;
    private ProgressDialog loadingBar;
    private int countUser=0,countDriver=0;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int deniedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_driver);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        adminsRef = FirebaseDatabase.getInstance().getReference().child("Admins");
        DriversRef = FirebaseDatabase.getInstance().getReference().child("Drivers");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        ImageView ivBack = findViewById(R.id.aad_back);
        etName = findViewById(R.id.add_driver_name);
        etPhone = findViewById(R.id.add_driver_phone);
        etPassword = findViewById(R.id.add_driver_password);
        etConfirmPassword = findViewById(R.id.add_driver_confirm_password);
        etAddress = findViewById(R.id.add_driver_address);
        Button btnAdd = findViewById(R.id.add_driver_button);

        // Select Address
        etAddress.setOnClickListener(v -> checkGPSState());


        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());

        btnAdd.setOnClickListener(v -> ValidateDriverData());
    }

    private void ValidateDriverData() {
        name = etName.getText().toString();
        phone = etPhone.getText().toString();
        password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        address = etAddress.getText().toString();

        String validateName = name.trim();
        String validatePhone = phone.trim();
        String validatePassword = password.trim();
        String validateConfirmPassword = confirmPassword.trim();

        if (TextUtils.isEmpty(validateName) || validateName.length() < 5)
        {
            Toast.makeText(this, "Please enter Full Name with no symbol and more than 5 letters", Toast.LENGTH_SHORT).show();
            showKeyboard(etName, this);
        }
        else if (TextUtils.isEmpty(validatePhone) || validatePhone.length() < 10)
        {
            Toast.makeText(this, "Please enter a valid Phone No", Toast.LENGTH_SHORT).show();
            showKeyboard(etPhone, this);
        }
        else if (TextUtils.isEmpty(validatePassword) || validatePassword.length() < 5)
        {
            Toast.makeText(this, "Please enter Password with more than 5 letters", Toast.LENGTH_SHORT).show();
            showKeyboard(etPassword, this);
        }
        else if (TextUtils.isEmpty(validateConfirmPassword) || validateConfirmPassword.length() < 5)
        {
            Toast.makeText(this, "Please enter Confirm Password with more than 5 letters", Toast.LENGTH_SHORT).show();
            showKeyboard(etConfirmPassword, this);
        }
        else if (TextUtils.isEmpty(address))
        {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString()))
                checkExistedUser();
            else {
                Toast.makeText(this, "Please make sure password and confirm password is same", Toast.LENGTH_SHORT).show();
                showKeyboard(etPassword, this);
            }
        }
    }

    // Check Existed Phone and Name in User
    private void checkExistedUser() {
        countUser=0;
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(phone)) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.hasChild("name") && postSnapshot.child("name").getValue().toString().toLowerCase().equals(name.toLowerCase())) {
                            countUser+=1;
                        }
                    }

                    if (countUser==0)
                        checkExistedDriver();
                    else
                        Toast.makeText(AdminAddDriver.this, "Driver Name already Exist", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AdminAddDriver.this, "Phone Number already Exist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Check Existed Phone and Name in Admin
    private void checkExistedDriver() {
        countDriver=0;
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(phone)) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.hasChild("name") && postSnapshot.child("name").getValue().toString().toLowerCase().equals(name.toLowerCase())) {
                            countDriver+=1;
                        }
                    }

                    if (countDriver==0)
                        confirmBeforeAdded();
                    else
                        Toast.makeText(AdminAddDriver.this, "Driver name already Exist", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(AdminAddDriver.this, "Driver Phone already Exist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void confirmBeforeAdded() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(AdminAddDriver.this);
        alertBuilder.setTitle("Confirm before added");
        alertBuilder.setMessage("Phone number: " + phone + "\n" +
                "The phone will become username that used to login." + "\n" +
                "It cannot be changed after added." + "\n" +
                "Please double check before added.");
        alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                addNewDriver();
            }

        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void addNewDriver() {
        // loading when processing to add food to database
        loadingBar.setTitle("Add New Driver");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new driver.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        HashMap<String, Object> adminMap = new HashMap<>();

        adminMap.put("phone", phone);
        adminMap.put("name", name);
        adminMap.put("password", password);
        adminMap.put("isAdmin", "n");

        adminsRef.child(phone).updateChildren(adminMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Log.d("addDriver", "Driver Added in Admins");
                        Toast.makeText(AdminAddDriver.this, "Driver Added", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(AdminAddDriver.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });

        HashMap<String, Object> driverMap = new HashMap<>();

        driverMap.put("driverPhone", phone);
        driverMap.put("driverName", name);
        driverMap.put("driverAddress", address);

        // add order in done
        DriversRef.child(phone).updateChildren(driverMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Log.d("addDriver", "Driver Added in Drivers");
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(AdminAddDriver.this,
                                "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                    finish();
                });
    }

    private void checkGPSState() {

        // check Phone GPS state
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (statusOfGPS)
        {
            // Request Location Permissions
            requestLocationPermission();
        }
        else
            Toast.makeText(this, "GPS is Required, Please Turn On", Toast.LENGTH_SHORT).show();

    }

    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to your GPS")
                    .setPositiveButton("ok", (dialog, which) ->
                            ActivityCompat.requestPermissions(AdminAddDriver.this,
                                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else { // first time ask permission
            ActivityCompat.requestPermissions(this, new
                    String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    // handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was GRANTED

                Intent i = new Intent(this, AdminSelectAddress.class);
                startActivityForResult(i, LOCATION_PERMISSION_CODE);

            } else if (deniedCount > 1) { // If denied permanently
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                super.onActivityResult(requestCode, resultCode, data);

                String address = data.getStringExtra("address");
                Log.d("Location123addCheck", address);

                etAddress.setText(address);
            }
        }
    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}