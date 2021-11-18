package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Prevalent.Prevalent;

public class EditProfile extends AppCompatActivity {
    private EditText etName, etPass;
    private TextView tvName, tvPassword;
    DatabaseReference usersRef;
    Button btnEditProfile, btnEditPassword;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ImageView ivBack = findViewById(R.id.edit_profile_back);
        tvName = findViewById(R.id.user_profile_username);
        tvPassword = findViewById(R.id.user_profile_password);
        btnEditProfile = findViewById(R.id.edit_profile_btn);
        btnEditPassword = findViewById(R.id.edit_password_btn);
        etName = findViewById(R.id.edit_profile_name);
        etPass = findViewById(R.id.edit_profile_password);

        loadingBar = new ProgressDialog(this);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());

        displayProfile();

        ivBack.setOnClickListener(v -> onBackPressed());

        btnEditProfile.setOnClickListener(v -> {
            validateProfile();
        });

        btnEditPassword.setOnClickListener(v -> {
            validatePassword();
        });
    }

    private void validateProfile() {
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name) || name.length() < 5)
        {
            Toast.makeText(this, "Please enter Full Name with no symbol and more than 5 letters",
                    Toast.LENGTH_SHORT).show();
            showKeyboard(etName, this);
        }
        else if (etName.getText().toString().equals(tvName.getText()))
        {
            Toast.makeText(this, "Please enter New Name", Toast.LENGTH_SHORT).show();
            showKeyboard(etName, this);
        }
        else
        {
            confirmBeforeEditProfile();
        }
    }

    private void confirmBeforeEditProfile() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(EditProfile.this);
        alertBuilder.setTitle("Confirm before Edit");
        alertBuilder.setMessage("Current Name: " + tvName.getText().toString() + "\n" +
                "New Name: " + etName.getText().toString() + "\n" +
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

                usersRef.child("name").setValue(etName.getText().toString());
                Toast.makeText(EditProfile.this, "Profile Edited", Toast.LENGTH_SHORT).show();
                // CLOSE loading bar
                loadingBar.dismiss();
                etName.setText("");
                etName.clearFocus();
                displayProfile();
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

    private void validatePassword() {
        String newPass = etPass.getText().toString();

        if (TextUtils.isEmpty(newPass) || newPass.length() < 5)
        {
            Toast.makeText(this, "Please enter Password with more than 5 letters", Toast.LENGTH_SHORT).show();
            showKeyboard(etPass, this);
        } else if (etPass.getText().toString().equals(tvPassword.getText()))
        {
            Toast.makeText(this, "Please enter New Password", Toast.LENGTH_SHORT).show();
            showKeyboard(etPass, this);
        }
        else
        {
            confirmBeforeResetPassword();
        }
    }

    private void confirmBeforeResetPassword() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(EditProfile.this);
        alertBuilder.setTitle("Confirm before Reset");
        alertBuilder.setMessage("Old Password: " + tvPassword.getText().toString() + "\n" +
                "New Password: " + etPass.getText().toString() + "\n" +
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

                usersRef.child("password").setValue(etPass.getText().toString());
                Toast.makeText(EditProfile.this, "Password Reset", Toast.LENGTH_SHORT).show();
                // CLOSE loading bar
                loadingBar.dismiss();
                etPass.setText("");
                etPass.clearFocus();
                displayProfile();
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

    private void displayProfile() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String name = snapshot.child("name").getValue().toString();
                    String password = snapshot.child("password").getValue().toString();

                    // Show Profile
                    tvName.setText(name);
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