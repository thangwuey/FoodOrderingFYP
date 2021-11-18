package com.example.foodorderingfyp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class AdminDeleteDriver extends AppCompatActivity {
    private TextView tvName, tvPhone, tvPassword, tvUsername, tvAddress;
    DatabaseReference driversRef,adminsRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_driver);

        ImageView ivBack = findViewById(R.id.admin_dd_back);
        tvName = findViewById(R.id.admin_dd_name);
        tvPhone = findViewById(R.id.admin_dd_phone);
        tvPassword = findViewById(R.id.admin_dd_password);
        tvUsername = findViewById(R.id.admin_dd_username);
        tvAddress = findViewById(R.id.admin_dd_address);
        Button btnDelete = findViewById(R.id.admin_dd_delete);

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        String driverPhone = getIntent().getStringExtra("driverPhone");
        driversRef = FirebaseDatabase.getInstance().getReference().child("Drivers")
                .child(driverPhone);
        adminsRef = FirebaseDatabase.getInstance().getReference().child("Admins")
                .child(driverPhone);

        displayDriverInfo();

        // Back Button
        ivBack.setOnClickListener(v -> onBackPressed());

        // delete driver
        btnDelete.setOnClickListener(v -> confirmBeforeDelete());
    }

    private void confirmBeforeDelete() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(AdminDeleteDriver.this);
        alertBuilder.setTitle("Confirm before delete");
        alertBuilder.setMessage("Driver name: " + tvName.getText().toString() + "\n" +
                "Driver Phone: " + tvPhone.getText().toString() + "\n" +
                "We are going to delete this driver details and his/her account" + "\n" +
                "Please double check before delete.");
        alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // loading when processing to DELETE food in database
                loadingBar.setTitle("Delete Driver");
                loadingBar.setMessage("Dear Admins, please wait while we are deleting this driver.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                deleteThisDriver();
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

    private void deleteThisDriver() {
        // DELETE from realtime database
        driversRef.removeValue();
        adminsRef.removeValue();
        Toast.makeText(AdminDeleteDriver.this, "Driver deleted", Toast.LENGTH_SHORT).show();

        // CLOSE loading bar
        loadingBar.dismiss();
        finish();
    }

    private void displayDriverInfo() {
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String name = snapshot.child("driverName").getValue().toString();
                    String phone = snapshot.child("driverPhone").getValue().toString();
                    String address = snapshot.child("driverAddress").getValue().toString();

                    tvName.setText(name);
                    tvPhone.setText(phone);
                    tvAddress.setText(address);
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
}