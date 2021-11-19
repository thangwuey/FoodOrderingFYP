package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import Prevalent.Prevalent;

public class CreditCardActivity extends AppCompatActivity {

    CardForm cardForm;
    Button confirmButton;
    AlertDialog.Builder alertBuilder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);

        Intent receiveIntent = this.getIntent();
        Integer walletAmount = receiveIntent.getIntExtra("Wallet",0);

        cardForm = findViewById(R.id.card_form);
        confirmButton = findViewById(R.id.btnConfirm);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .setup(CreditCardActivity.this);
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardForm.isValid()) {
                    alertBuilder = new AlertDialog.Builder(CreditCardActivity.this);
                    alertBuilder.setTitle("Confirm before reload");
                    alertBuilder.setMessage("Card number: " + cardForm.getCardNumber() + "\n" +
                            "Card expiry date: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                            "Card CVV: " + cardForm.getCvv() + "\n" +
                            "Postal code: " + cardForm.getPostalCode() + "\n" +
                            "Phone number: " + cardForm.getMobileNumber());
                    alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(CreditCardActivity.this, "Thank you for reload", Toast.LENGTH_LONG).show();
                            finish();
                            //Integer walletAmount = 10;
                            //Integer price = 0;
                            //final DatabaseReference WalletRef = FirebaseDatabase.getInstance().getReference().child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone());
                            final DatabaseReference WalletRef;
                            WalletRef = FirebaseDatabase.getInstance().getReference();

                            WalletRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                    if(!(snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).exists()))
                                    {
                                        HashMap<String,Object> walletdataMap = new HashMap<>();
                                        walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone()); // this maybe cannot run
                                        walletdataMap.put("walletAmount",walletAmount);

                                        WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    finish();
                                                    //Toast.makeText(WalletActivity.this,"Successfully reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                                    //showBalance.setText(walletAmount);
                                                    //displayBalance();
                                                }else{
                                                    Toast.makeText(CreditCardActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }else{
                                        //Reload the amount to the database
                                        String amount = snapshot.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).child("walletAmount").getValue().toString();
                                        Integer price = Integer.parseInt(amount);

                                        price += walletAmount;

                                        HashMap<String,Object> walletdataMap = new HashMap<>();
                                        walletdataMap.put("phone",Prevalent.currentOnlineUser.getPhone());
                                        walletdataMap.put("walletAmount",price);

                                        WalletRef.child("E-Wallet").child(Prevalent.currentOnlineUser.getPhone()).updateChildren(walletdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    //Toast.makeText(WalletActivity.this,"Successfully Reload complete 10 MYR !",Toast.LENGTH_SHORT).show();
                                                    //displayBalance();
                                                }else{
                                                    Toast.makeText(CreditCardActivity.this,"Error,Please try again!",Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });

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
                }else {
                    Toast.makeText(CreditCardActivity.this, "Please complete the form", Toast.LENGTH_LONG).show();
                }
            }

        });

        ImageView ivCreditCardBack = findViewById(R.id.cc_back);
        // Back button
        ivCreditCardBack.setOnClickListener(v -> onBackPressed());
    }
}