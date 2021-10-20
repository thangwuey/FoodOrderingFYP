package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingfyp.Admin.AdminHome;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import com.example.foodorderingfyp.ModelClass.Users;

import Prevalent.Prevalent;

public class LoginActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText InputPhoneNumber,InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private TextView AdminLink,NotAdminLink;
    private String parentDbName = "Users";
    boolean shouldExit = false; // press back button to exit
    Snackbar snackbar;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        relativeLayout = findViewById(R.id.login_layout); //new

        registerButton = (Button) findViewById(R.id.loginRegisterButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);//slide ui function
            }
        });

        LoginButton = (Button)findViewById(R.id.login);
        InputPhoneNumber = (EditText) findViewById(R.id.loginPhoneNumber);
        InputPassword = (EditText) findViewById(R.id.loginPassword);
        AdminLink = (TextView) findViewById(R.id.loginadmin);
        NotAdminLink = (TextView) findViewById(R.id.notLoginadmin);
        loadingBar = new ProgressDialog(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LoginUser();
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LoginButton.setText("Login As Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });


    }


    private void LoginUser()
    {
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        // Validation
        if(TextUtils.isEmpty(phone.trim()))
        {
            Toast.makeText(this, "Please write your phone number", Toast.LENGTH_SHORT).show();
            showKeyboard(InputPhoneNumber, this);
        }
        else if(TextUtils.isEmpty(password.trim()))
        {
            Toast.makeText(this, "Please write your password!", Toast.LENGTH_SHORT).show();
            showKeyboard(InputPassword, this);
        }
        else
        {
            loadingBar.setTitle("Login Your Account Now");
            loadingBar.setMessage("Please Wait, while we are checking the credential.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAcceddToAccount(phone,password);
        }

    }

    private void AllowAcceddToAccount(String phone, String password)
    {
        final DatabaseReference RootRef; //connect to firebase
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.child(parentDbName).child(phone).exists())//since admin and user user the same activity so it will different from register
                {
                    Users usersData = snapshot.child(parentDbName).child(phone).getValue(Users.class);//video 7 10 min pass the data to the users class and retrive it

                    if(usersData.getPhone().equals(phone)) //check with the database see whether same with the user enter on the phone ornot
                    {
                        if(usersData.getPassword().equals(password)) // if the password is correct
                        {
                            if(parentDbName.equals("Admins"))
                            {
                                Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //HERE MUST CHANGE THE ADMIN TO THE ADD PRODUCT PAGE (MainActivity2 change to add adminHome page)
                                Intent intent = new Intent(LoginActivity.this, AdminHome.class);
                                startActivity(intent);
                            }
                            else if (parentDbName.equals("Users"))
                            {
                                Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //HERE MUST PUT THE HOME ACTIVITY, Intent....
                                Intent intent = new Intent(LoginActivity.this,MainActivity2.class);
                                //This line is video 15 get user data after login because get the user data and store it for further use in cart database and profile name
                                Prevalent.currentOnlineUser = usersData ;
                                startActivity(intent);
                            }

                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Password are incorrect. Please try again", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Account with this " + phone + "number do not exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Please create a new account.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

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