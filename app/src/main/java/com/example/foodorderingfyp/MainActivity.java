package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;//for navigation used
    private Button CreateAccountButton; // for user to create account
    private EditText InputName, InputPhoneNumber, InputPassword;// the text view naming
    private ProgressDialog loadingBar;
    boolean shouldExit = false; // press back button to exit
    Snackbar snackbar;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.register_layout); //new

        //Navigation purpose
        loginButton = (Button) findViewById(R.id.registerLoginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        //Creating user account
        CreateAccountButton = (Button)findViewById(R.id.register);
        InputName = (EditText) findViewById(R.id.registerUsername);
        InputPhoneNumber = (EditText) findViewById(R.id.registerPhoneNumber);
        InputPassword = (EditText) findViewById(R.id.registerPassword);
        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CreateAccount();
            }
        });

    }


    //Create Account function
    private void CreateAccount()
    {
        String name = InputName.getText().toString();
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Please write your name!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "Please write your phone number!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
             Toast.makeText(this, "Please write your password!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please Wait, while we are checking the credential.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidatePhoneNumber(name,phone,password); // for validating to check in firebase
        }

    }

    private void ValidatePhoneNumber(String name, String phone, String password)
    {
        final DatabaseReference RootRef; //connect to firebase
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
              if(!(snapshot.child("Users").child(phone).exists()))
              {
                  HashMap<String,Object> userdataMap = new HashMap<>();
                  userdataMap.put("phone",phone);
                  userdataMap.put("password",password);
                  userdataMap.put("name",name);

                  RootRef.child("Users").child(phone).updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull @NotNull Task<Void> task)
                      {
                          if (task.isSuccessful())
                          {
                              Toast.makeText(MainActivity.this, "Account have created successfully.", Toast.LENGTH_SHORT).show();
                              loadingBar.dismiss();

                              Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                              startActivity(intent);
                          }
                          else
                          {
                              loadingBar.dismiss();
                              Toast.makeText(MainActivity.this, "Network Error:Please try again.", Toast.LENGTH_SHORT).show();
                          }
                      }
                  });
              }
              else
              {
                  Toast.makeText(MainActivity.this, "This " + phone + "already exists.", Toast.LENGTH_SHORT).show();
                  loadingBar.dismiss();
              }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        ///////////////////////////HERE GOT PROBLEM, NOT SAME WITH YOUTUBE VIDEO PART 6
        /*RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot)
            {
                if (!(snapshot.child("Users").child(phone).exists()))
                {
                    HashMap<String,Object> userdataMap = new HashMap<>();
                    userdataMap.put("phone",phone);
                    userdataMap.put("password",password);
                    userdataMap.put("name",name);

                    RootRef.child("Users").child(phone).updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "Account have created successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(intent);
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(MainActivity.this, "Network Error:Please try again.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
                else
                {
                    Toast.makeText(MainActivity.this, "This " + phone + "already exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error)
            {

            }
        });*/
    }

    @Override
    public void onBackPressed() {

        // double press back button to exit app
        if (shouldExit){
            snackbar.dismiss();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
        }else{
            snackbar = Snackbar.make(relativeLayout, "Please press BACK again to exit", Snackbar.LENGTH_SHORT);
            snackbar.show();
            shouldExit = true;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldExit = false;
                }
            }, 1500);
        }
    }
}