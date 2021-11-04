package com.example.foodorderingfyp.Admin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.ModelClass.Cart;
import com.example.foodorderingfyp.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import ViewHolder.AdminOrderFoodItemViewHolder;

public class AdminSendOrderDetails extends AppCompatActivity {

    private Button btnSendOrder;
    private TextView tvOrderID, tvOrderState, tvOrderAmount, tvOrderNo, tvOrderTime, tvOrderDate;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvCustomerCity;
    private DatabaseReference OrdersRef, CartsRef, UserRef;
    private String orderID = "";
    private final String strPrepare="Preparing";
    private final String strSend="Sending";
    private final String strDone="Done Order";
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private ProgressDialog loadingBar;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int deniedCount = 0;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_order_details);

        btnSendOrder = findViewById((R.id.send_order_button));
        tvOrderID = findViewById(R.id.send_order_id);
        tvOrderState = findViewById(R.id.send_order_state);
        tvOrderAmount = findViewById(R.id.send_order_food_amount);
        tvOrderNo = findViewById(R.id.send_order_no);
        tvOrderTime = findViewById(R.id.send_order_time);
        tvOrderDate = findViewById(R.id.send_order_date);
        tvCustomerName = findViewById(R.id.send_order_customer_name);
        tvCustomerPhone = findViewById(R.id.send_order_customer_phone);
        tvCustomerAddress = findViewById(R.id.send_order_customer_address);
        tvCustomerCity = findViewById(R.id.send_order_customer_city);
        ImageView ivSendOrderDetailsBack = findViewById(R.id.asod_back);
        recyclerView = findViewById(R.id.rv_asod);
        recyclerView.setHasFixedSize(true);

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        orderID = getIntent().getStringExtra("orderID");

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View");

        // progressing bar to let user know it is processing
        loadingBar = new ProgressDialog(this);

        // Initialize fusedLocationProviderClient, for GPS location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize locationRequest, for GPS location UPDATES
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // display food info
        displayOrderInfo();

        ivSendOrderDetailsBack.setOnClickListener(v -> onBackPressed());

        btnSendOrder.setOnClickListener(v -> {
            // Check GPS ON/OFF STATE
            checkGPSState();
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // Reference for Delivery
            DatabaseReference DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");
            HashMap<String, Object> deliveryMap = new HashMap<>();

            if (locationResult == null)
                return;

            for (Location location: locationResult.getLocations()) {
                Log.d("Location123", location.toString());

                // Location UPDATES to Firebase
                deliveryMap.put("deliveryID", getIntent().getStringExtra("orderID"));
                deliveryMap.put("latitude", location.getLatitude());
                deliveryMap.put("longitude", location.getLongitude());

                DelRef.child(getIntent().getStringExtra("orderID")).updateChildren(deliveryMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Log.d("Location123", "Update to Firebase. " + location.toString());
                    }
                });
            }
        }
    };

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
                            ActivityCompat.requestPermissions(AdminSendOrderDetails.this,
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
                // get LAST LOCATION
                //getLastLocation();

                // Location KEEP UPDATES
                checkSettingAndStartLocationUpdates();

                // change order state
                changeState();
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

    // Check whether the setting has fulfilled
    private void checkSettingAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            // Settings of device are satisfied and we can START LOCATION UPDATES
            startLocationUpdates();
        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                try {
                    apiException.startResolutionForResult(AdminSendOrderDetails.this, 1001);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // START UPDATING
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    // STOP UPDATING
    private void stopLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Log.d("Location123", "Stop Updating Location");
        }
    }

    // TESTING to get location
    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

            locationTask.addOnSuccessListener(location -> {
                // we have a location
                if (location != null) {
                    Log.d("Location123", location.toString());
                    Log.d("Location123", String.valueOf(location.getLatitude()));
                    Log.d("Location123", String.valueOf(location.getLongitude()));

                    // change order state
                    changeState();
                } else // no location
                    Log.d("Location123", "Location was null...");
            }).addOnFailureListener(e -> Log.d("Location123", e.getLocalizedMessage()));

        }

    }

    // Change Order State
    private void changeState() {
        // loading bar declaration
        if (tvOrderState.getText().equals("Preparing")) {
            // loading when processing to SEND ORDER
            loadingBar.setTitle("Send Order");
            loadingBar.setMessage("Dear Admin, please wait while we are sending order.");
        } else {
            // loading when processing to DONE ORDER
            loadingBar.setTitle("Done Order");
            loadingBar.setMessage("Dear Admin, please wait while we have delivered the order.");
        }
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // User
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    String userPhone;

                    // for loop, get user phone
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        userPhone = postSnapshot.getKey(); // get PHONE from TABLE/CHILD name

                        // get Order Info, Orders -> Phone
                        OrdersRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    // Order ID
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(postSnapshot.getKey())) {

                                            if (postSnapshot.child("state").getValue().equals("P")) {

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("S");
                                                Toast.makeText(AdminSendOrderDetails.this, "Order Sent", Toast.LENGTH_SHORT).show();
                                                btnSendOrder.setText(strDone);

                                                tvOrderState.setText(strSend);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#007c33"));
                                                // CLOSE loading bar
                                                loadingBar.dismiss();
                                            } else {

                                                stopLocationUpdates();

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("D");
                                                Toast.makeText(AdminSendOrderDetails.this, "Order Done", Toast.LENGTH_SHORT).show();
                                                // CLOSE loading bar
                                                loadingBar.dismiss();

                                                Intent intent = new Intent(AdminSendOrderDetails.this, AdminSendOrder.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
                                                finish();
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayOrderInfo() {

        // User
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    String userPhone;

                    // for loop, get user phone
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        userPhone = postSnapshot.getKey(); // get PHONE from TABLE/CHILD name

                        // get Order Info
                        OrdersRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(postSnapshot.getKey())) {
                                            String tempOrderID = postSnapshot.child("orderID").getValue().toString();
                                            String tempOrderTime = postSnapshot.child("time").getValue().toString();
                                            String tempOrderDate = postSnapshot.child("date").getValue().toString();
                                            String tempOrderState = postSnapshot.child("state").getValue().toString();
                                            String tempOrderAmount = "RM " + postSnapshot.child("totalAmount").getValue().toString() + ".00";
                                            String tempCustomerName = postSnapshot.child("name").getValue().toString();
                                            String tempCustomerPhone = postSnapshot.child("phone").getValue().toString();
                                            String tempCustomerCity = postSnapshot.child("city").getValue().toString();

                                            // Address, LINE BREAK when more than 20 characters
                                            String str = postSnapshot.child("address").getValue().toString();
                                            String[] strArray = str.split(" ");
                                            StringBuilder builder = new StringBuilder();
                                            String tmpString = "";

                                            for (String s : strArray) {
                                                if (tmpString.length() > 20) {
                                                    builder.append(tmpString).append("\n");
                                                    tmpString = s + " ";
                                                } else if (s.equals(strArray[strArray.length - 1])) {
                                                    tmpString += s;
                                                    builder.append(tmpString);
                                                } else {
                                                    tmpString += s + " ";
                                                }
                                            }

                                            tvOrderID.setText(tempOrderID);
                                            tvOrderNo.setText(tempOrderID);
                                            tvOrderTime.setText(tempOrderTime);
                                            tvOrderDate.setText(tempOrderDate);
                                            tvOrderAmount.setText(tempOrderAmount);
                                            tvCustomerName.setText(tempCustomerName);
                                            tvCustomerPhone.setText(tempCustomerPhone);
                                            tvCustomerAddress.setText(builder.toString());
                                            tvCustomerCity.setText(tempCustomerCity);
                                            if (tempOrderState.equals("P")) // Preparing State
                                                tvOrderState.setText(strPrepare);
                                            else if (tempOrderState.equals("S")) { // Sending State
                                                tvOrderState.setText(strSend);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#007c33"));
                                                btnSendOrder.setText(strDone);
                                            }

                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // Cart List -> Admin View -> Phone -> Order ID -> GET Food Details
                        ArrayList<Cart> orderFoodList = new ArrayList<>();

                        CartsRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        Log.d("userPhone", postSnapshot.getKey());
                                        Log.d("userPhone123", orderID);
                                        if (orderID.equals(postSnapshot.getKey())) {
                                            for (DataSnapshot cartSnapshot : postSnapshot.getChildren()) {
                                                Cart cartData = cartSnapshot.getValue(Cart.class);
                                                Log.d("name", cartData.getFoodName());
                                                orderFoodList.add(cartData);
                                            }

                                            // Adapter instead of Firebase Adapter (different declaration)
                                            RecyclerView.Adapter<AdminOrderFoodItemViewHolder> mAdapter = new RecyclerView.Adapter<AdminOrderFoodItemViewHolder>() {
                                                @NonNull
                                                @Override
                                                public AdminOrderFoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                                    View view = LayoutInflater.from(parent.getContext())
                                                            .inflate(R.layout.admin_order_food_item, parent, false);
                                                    return new AdminOrderFoodItemViewHolder(view);
                                                }

                                                @Override
                                                public void onBindViewHolder(@NonNull AdminOrderFoodItemViewHolder holder, int position) {
                                                    String strQty = orderFoodList.get(position).getQuantity() + "x";
                                                    int subTotal = ((Integer.parseInt(orderFoodList.get(position).getFoodPrice()))) * Integer.parseInt(orderFoodList.get(position).getQuantity());
                                                    String strSubTotal = "RM " + subTotal + ".00";

                                                    // First letter of each word to UPPERCASE in FOOD NAME
                                                    String str = orderFoodList.get(position).getFoodName();
                                                    String[] strArray = str.split(" ");
                                                    StringBuilder builder = new StringBuilder();
                                                    for (String s : strArray) {
                                                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                                        builder.append(cap).append(" ");
                                                    }

                                                    // holder from AdminOrderFoodItemViewHolder.java
                                                    holder.txtAdminOrderFoodQuantity.setText(strQty);
                                                    holder.txtAdminOrderFoodName.setText(builder.toString());
                                                    holder.txtAdminOrderFoodSubtotal.setText(strSubTotal);

                                                }

                                                @Override
                                                public int getItemCount() {
                                                    return orderFoodList.size();
                                                }
                                            };

                                            recyclerView.setAdapter(mAdapter);
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}