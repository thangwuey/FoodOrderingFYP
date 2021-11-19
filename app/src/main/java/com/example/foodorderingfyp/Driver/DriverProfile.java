package com.example.foodorderingfyp.Driver;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.Admin.AdminSelectAddress;
import com.example.foodorderingfyp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Prevalent.PrevalentAdmin;

public class DriverProfile extends AppCompatActivity {
    private RelativeLayout rlDpp, rlDpp2, rlEP, rlEP2, rlRP, rlRP2;
    private EditText etName, etAddress, etCurrentPass, etPass, etConPass;
    private TextView tvName, tvPhone, tvPassword, tvUsername, tvAddress;
    DatabaseReference driversRef,adminsRef,usersRef;
    Button btnEditPro, btnEditPass, btnCancelPro, btnConPro, btnCancelPass, btnConPass;
    private ProgressDialog loadingBar;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int deniedCount = 0;
    private int countUser=0,countDriver=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        ImageView ivBack = findViewById(R.id.driver_profile_back);
        tvName = findViewById(R.id.dpp_name);
        tvPhone = findViewById(R.id.dpp_phone);
        tvPassword = findViewById(R.id.dpp_password);
        tvUsername = findViewById(R.id.dpp_username);
        tvAddress = findViewById(R.id.dpp_address);
        btnEditPro = findViewById(R.id.dpp_edit_profile_btn);
        btnEditPass = findViewById(R.id.dpp_edit_password_btn);
        rlDpp = findViewById(R.id.dpp_rl);
        rlDpp2 = findViewById(R.id.dpp_rl2);
        etName = findViewById(R.id.ep_name);
        etAddress = findViewById(R.id.ep_address);
        btnConPro = findViewById(R.id.ep_confirm_btn);
        btnCancelPro = findViewById(R.id.ep_cancel_btn);
        rlEP = findViewById(R.id.ep_rl);
        rlEP2 = findViewById(R.id.ep_rl2);
        etCurrentPass = findViewById(R.id.rp_current_password);
        etPass = findViewById(R.id.rp_password);
        etConPass = findViewById(R.id.rp_confirm_password);
        btnConPass = findViewById(R.id.rp_reset_btn);
        btnCancelPass = findViewById(R.id.rp_cancel_btn);
        rlRP = findViewById(R.id.rp_rl);
        rlRP2 = findViewById(R.id.rp_rl2);

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        driversRef = FirebaseDatabase.getInstance().getReference().child("Drivers")
                .child(PrevalentAdmin.currentOnlineAdmin.getPhone());
        adminsRef = FirebaseDatabase.getInstance().getReference().child("Admins")
                .child(PrevalentAdmin.currentOnlineAdmin.getPhone());

        displayProfile();

        // Select Address
        etAddress.setOnClickListener(v -> checkGPSState());

        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());

        btnCancelPro.setOnClickListener(v -> {
            rlDpp.setVisibility(View.VISIBLE);
            rlDpp2.setVisibility(View.VISIBLE);
            rlEP.setVisibility(View.GONE);
            rlEP2.setVisibility(View.GONE);
            rlRP.setVisibility(View.GONE);
            rlRP2.setVisibility(View.GONE);
        });

        btnCancelPass.setOnClickListener(v -> {
            rlDpp.setVisibility(View.VISIBLE);
            rlDpp2.setVisibility(View.VISIBLE);
            rlEP.setVisibility(View.GONE);
            rlEP2.setVisibility(View.GONE);
            rlRP.setVisibility(View.GONE);
            rlRP2.setVisibility(View.GONE);
        });

        btnEditPro.setOnClickListener(v -> {
            rlDpp.setVisibility(View.GONE);
            rlDpp2.setVisibility(View.GONE);
            rlEP.setVisibility(View.VISIBLE);
            rlEP2.setVisibility(View.VISIBLE);
            rlRP.setVisibility(View.GONE);
            rlRP2.setVisibility(View.GONE);
        });

        btnEditPass.setOnClickListener(v -> {
            rlDpp.setVisibility(View.GONE);
            rlDpp2.setVisibility(View.GONE);
            rlEP.setVisibility(View.GONE);
            rlEP2.setVisibility(View.GONE);
            rlRP.setVisibility(View.VISIBLE);
            rlRP2.setVisibility(View.VISIBLE);
        });

        btnConPro.setOnClickListener(v -> {
            validateProfile();
        });

        btnConPass.setOnClickListener(v -> {
            validatePassword();
        });
    }

    private void validateProfile() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString();

        if (TextUtils.isEmpty(name) || name.length() < 5)
        {
            Toast.makeText(this, "Please enter Full Name with no symbol and more than 5 letters",
                    Toast.LENGTH_SHORT).show();
            showKeyboard(etName, this);
        }
        else if (TextUtils.isEmpty(address))
        {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
        }
        else
        {
            checkExistedUser();
        }
    }

    // Check Existed Phone and Name in User
    private void checkExistedUser() {
        String name = etName.getText().toString().toLowerCase();
        countUser=0;
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.hasChild("name") && postSnapshot.child("name").getValue()
                                .toString().toLowerCase().equals(name)) {
                            countUser+=1;
                        }
                    }

                    if (countUser==0)
                        checkExistedDriver();
                    else
                        Toast.makeText(DriverProfile.this, "Driver Name already Exist",
                                Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Check Existed Phone and Name in Admin
    private void checkExistedDriver() {
        String name = etName.getText().toString().toLowerCase();
        countDriver=0;
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.hasChild("name") && postSnapshot.child("name").getValue()
                                .toString().toLowerCase().equals(name)) {
                            countDriver+=1;
                        }
                    }

                    if (countDriver==0)
                        confirmBeforeEditProfile();
                    else
                        Toast.makeText(DriverProfile.this, "Driver name already Exist",
                                Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void confirmBeforeEditProfile() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(DriverProfile.this);
        alertBuilder.setTitle("Confirm before Edit");
        alertBuilder.setMessage("Name: " + etName.getText().toString() + "\n" +
                "Address: " + etAddress.getText().toString() + "\n" +
                "Please double check before edit.");
        alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // loading when processing to DELETE food in database
                loadingBar.setTitle("Edit Profile");
                loadingBar.setMessage("Dear " + tvName + ", please wait while we are updating profile.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                editProfile();
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

    private void editProfile() {
        driversRef.child("driverName").setValue(etName.getText().toString());
        driversRef.child("driverAddress").setValue(etAddress.getText().toString());
        adminsRef.child("name").setValue(etName.getText().toString());
        Toast.makeText(this, "Profile Edited", Toast.LENGTH_SHORT).show();
        // CLOSE loading bar
        loadingBar.dismiss();

        rlDpp.setVisibility(View.VISIBLE);
        rlDpp2.setVisibility(View.VISIBLE);
        rlEP.setVisibility(View.GONE);
        rlEP2.setVisibility(View.GONE);
        rlRP.setVisibility(View.GONE);
        rlRP2.setVisibility(View.GONE);
        displayProfile();
    }

    private void validatePassword() {
        String currentPass = etCurrentPass.getText().toString().trim();
        String newPass = etPass.getText().toString();
        String confirmPass = etConPass.getText().toString();

        if (TextUtils.isEmpty(currentPass))
        {
            Toast.makeText(this, "Please enter current password", Toast.LENGTH_SHORT).show();
            showKeyboard(etCurrentPass, this);
        }
        else if (TextUtils.isEmpty(newPass) || newPass.length() < 5)
        {
            Toast.makeText(this, "Please enter Password with more than 5 letters", Toast.LENGTH_SHORT).show();
            showKeyboard(etPass, this);
        }
        else if (TextUtils.isEmpty(confirmPass))
        {
            Toast.makeText(this, "Please enter Confirm Password", Toast.LENGTH_SHORT).show();
            showKeyboard(etConPass, this);
        }
        else
        {
            checkPassword();
        }
    }

    private void checkPassword() {
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pass = snapshot.child("password").getValue().toString();
                    if (etCurrentPass.getText().toString().equals(pass)) {
                        if (etPass.getText().toString().equals(etConPass.getText().toString()))
                            confirmBeforeResetPassword();
                        else
                            Toast.makeText(DriverProfile.this,"Please make sure that new password and " +
                                    "confirm password is the same",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DriverProfile.this,"The current password is wrong. Please Enter Again",Toast.LENGTH_SHORT).show();
                        showKeyboard(etCurrentPass,DriverProfile.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void confirmBeforeResetPassword() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(DriverProfile.this);
        alertBuilder.setTitle("Confirm before Reset");
        alertBuilder.setMessage("New Password: " + etPass.getText().toString() + "\n" +
                "Please double check before edit.");
        alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // loading when processing to DELETE food in database
                loadingBar.setTitle("Reset Password");
                loadingBar.setMessage("Dear " + tvName + ", please wait while we are resetting password.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                resetPassword();
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

    private void resetPassword() {
        adminsRef.child("password").setValue(etPass.getText().toString());
        Toast.makeText(this, "Password Reset", Toast.LENGTH_SHORT).show();
        // CLOSE loading bar
        loadingBar.dismiss();

        etCurrentPass.setText("");
        etPass.setText("");
        etConPass.setText("");

        rlDpp.setVisibility(View.VISIBLE);
        rlDpp2.setVisibility(View.VISIBLE);
        rlEP.setVisibility(View.GONE);
        rlEP2.setVisibility(View.GONE);
        rlRP.setVisibility(View.GONE);
        rlRP2.setVisibility(View.GONE);
        displayProfile();
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
                            ActivityCompat.requestPermissions(DriverProfile.this,
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

    private void displayProfile() {
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String name = snapshot.child("driverName").getValue().toString();
                    String phone = snapshot.child("driverPhone").getValue().toString();
                    String address = snapshot.child("driverAddress").getValue().toString();

                    // Show Profile and Account
                    tvName.setText(name);
                    tvPhone.setText(phone);
                    tvAddress.setText(address);

                    // Show Edit Profile
                    etName.setText(name);
                    etAddress.setText(address);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("phone").getValue().toString();
                    String password = snapshot.child("password").getValue().toString();

                    tvUsername.setText(username);
                    tvPassword.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}