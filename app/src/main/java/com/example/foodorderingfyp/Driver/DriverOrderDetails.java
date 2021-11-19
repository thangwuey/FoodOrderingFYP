package com.example.foodorderingfyp.Driver;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.foodorderingfyp.ModelClass.Cart;
import com.example.foodorderingfyp.R;
import com.example.foodorderingfyp.TrackDelivery;
import com.example.foodorderingfyp.TrackGPSMapActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Prevalent.PrevalentAdmin;
import ViewHolder.AdminOrderFoodItemViewHolder;

public class DriverOrderDetails extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {
    RelativeLayout relativeLayoutOrder, relativeLayoutMap;
    private Button btnSendOrder,btnDoneOrder,btnView,btnBack,btnDoneMap;
    private TextView tvOrderID, tvOrderState, tvOrderAmount, tvOrderNo, tvOrderTime, tvOrderDate;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvCustomerCity;
    private DatabaseReference OrdersRef, CartsRef, UserRef, DelRef;
    private String orderID = "";
    private final String strStateSend="s";
    private final String strReady ="Ready";
    private final String strSend="Sending";
    private final String strDone ="Done";
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private ProgressDialog loadingBar;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int deniedCount = 0;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    private int countOrder=0;
    // Map
    private GoogleMap map;
    SupportMapFragment mapFragment;
    private Marker pickUpMarker, deliveryManMarker;
    Handler handler = new Handler();
    private Polyline polyline;
    Bitmap BitMapMarker;
    protected LatLng start = null;
    protected LatLng end = null;
    private List<Polyline> polylineList=null;
    TextView tvDeliveryAddress;
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_order_details);

        btnSendOrder = findViewById(R.id.dod_send_order_button);
        btnDoneOrder = findViewById(R.id.dod_done_order_button);
        tvOrderID = findViewById(R.id.dod_send_order_id);
        tvOrderState = findViewById(R.id.dod_send_order_state);
        tvOrderAmount = findViewById(R.id.dod_send_order_food_amount);
        tvOrderNo = findViewById(R.id.dod_send_order_no);
        tvOrderTime = findViewById(R.id.dod_send_order_time);
        tvOrderDate = findViewById(R.id.dod_send_order_date);
        tvCustomerName = findViewById(R.id.dod_send_order_customer_name);
        tvCustomerPhone = findViewById(R.id.dod_send_order_customer_phone);
        tvCustomerAddress = findViewById(R.id.dod_send_order_customer_address);
        tvCustomerCity = findViewById(R.id.dod_send_order_customer_city);
        ImageView ivBack = findViewById(R.id.dod_back);
        recyclerView = findViewById(R.id.rv_dod);
        recyclerView.setHasFixedSize(true);

        // Map
        tvDeliveryAddress = findViewById(R.id.dod_map_address);
        ImageView ivTrackGPSBack = findViewById(R.id.dod_map_back);
        btnBack = findViewById(R.id.dod_back_to_order_button);
        btnDoneMap = findViewById(R.id.dod_map_done_order_button);
        btnView = findViewById(R.id.dod_view_button);
        relativeLayoutOrder = findViewById(R.id.dod_send_rl);
        relativeLayoutMap = findViewById(R.id.dod_map_rl);
        ivTrackGPSBack.setOnClickListener(v -> onBackPressed());
        // Map

        // put all item in same layout in recyclerView
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        orderID = getIntent().getStringExtra("orderID");

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        CartsRef = FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View");
        DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");

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

        ivBack.setOnClickListener(v -> onBackPressed());

        btnSendOrder.setOnClickListener(v -> {
            // SENDING delivery cannot more than 3
            checkSendingOrder();
        });

        btnDoneOrder.setOnClickListener(v -> confirmBeforeDoneOrder());

        // Map
        btnView.setOnClickListener(v -> {
            relativeLayoutMap.setVisibility(View.VISIBLE);
            relativeLayoutOrder.setVisibility(View.GONE);
        });
        btnBack.setOnClickListener(v -> {
            // check Phone GPS state
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!statusOfGPS) {
                new AlertDialog.Builder(DriverOrderDetails.this)
                        .setTitle("Turn On GPS")
                        .setMessage("Please turn on GPS " + "\n" +
                                "The map will not working if GPS state is off.")
                        .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
            relativeLayoutMap.setVisibility(View.GONE);
            relativeLayoutOrder.setVisibility(View.VISIBLE);
        });
        btnDoneMap.setOnClickListener(v -> confirmBeforeDoneOrder());
        // CHANGE delivery man marker
        BitmapDrawable bitmapDraw = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.motor);
        Bitmap b = bitmapDraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 110, false);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dod_gps_map);

        mapFragment.getMapAsync(this);
    }

    private void checkSendingOrder() {
        String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();
        DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.hasChild("state") && postSnapshot.hasChild("driverName")) {
                            String state = postSnapshot.child("state").getValue().toString();
                            String phone = postSnapshot.child("driverPhone").getValue().toString();
                            if (state.equals("s") && phone.equals(prePhone)) {
                                countOrder+=1;
                            }
                        }

                    }
                }
                Log.d("OrderIDCount",String.valueOf(countOrder));
                if (countOrder<3)
                    checkGPSState();
                else
                    Toast.makeText(DriverOrderDetails.this,
                            "Your current delivery order is reached 3 items" + "\n" +
                                    "Please sending the current order before collect new order", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // Reference for Delivery
            //DatabaseReference DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");
            HashMap<String, Object> deliveryMap = new HashMap<>();
            if (locationResult == null)
                return;

            // Only worked for SINGLE order
            /*DelRef.child(getIntent().getStringExtra("orderID")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("state")) {
                        String state = snapshot.child("state").getValue().toString();
                        if (!state.equals("d") && fusedLocationProviderClient != null) {
                            for (Location location: locationResult.getLocations()) {
                                //Log.d("Location123", location.toString());

                                // Location UPDATES to Firebase
                                deliveryMap.put("deliveryID", getIntent().getStringExtra("orderID"));
                                deliveryMap.put("latitude", location.getLatitude());
                                deliveryMap.put("longitude", location.getLongitude());
                                deliveryMap.put("driverPhone", PrevalentAdmin.currentOnlineAdmin.getPhone());
                                deliveryMap.put("driverName", PrevalentAdmin.currentOnlineAdmin.getName());
                                deliveryMap.put("state", strStateSend);

                                DelRef.child(getIntent().getStringExtra("orderID")).updateChildren(deliveryMap).addOnCompleteListener(task -> {
                                    if (task.isSuccessful())
                                    {
                                        Log.d("Location123", "Update to Firebase. \n" + location.toString());
                                        Log.d("Location123", "NOT First Time");
                                    }
                                });
                            }
                        }
                    }

                    if (!snapshot.exists()) {
                        for (Location location: locationResult.getLocations()) {
                            //Log.d("Location123", location.toString());

                            // Location UPDATES to Firebase
                            deliveryMap.put("deliveryID", getIntent().getStringExtra("orderID"));
                            deliveryMap.put("latitude", location.getLatitude());
                            deliveryMap.put("longitude", location.getLongitude());
                            deliveryMap.put("driverPhone", PrevalentAdmin.currentOnlineAdmin.getPhone());
                            deliveryMap.put("driverName", PrevalentAdmin.currentOnlineAdmin.getName());
                            deliveryMap.put("state", strStateSend);

                            DelRef.child(getIntent().getStringExtra("orderID")).updateChildren(deliveryMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful())
                                {
                                    Log.d("Location123", "Update to Firebase. \n" + location.toString());
                                    Log.d("Location123", "First Time Create");
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/

            // MULTIPLE order
            for (Location location: locationResult.getLocations()) {
                DelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Order ID
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                //Log.d("Location123post", String.valueOf(postSnapshot.hasChild("state")));
                                if (postSnapshot.hasChild("state") && postSnapshot.hasChild("driverName")) {
                                    String state = postSnapshot.child("state").getValue().toString();
                                    String phone = postSnapshot.child("driverPhone").getValue().toString();
                                    String prePhone = PrevalentAdmin.currentOnlineAdmin.getPhone();
                                    //Log.d("Location123check", state + ", " + phone);
                                    if (!state.equals("d") && phone.equals(prePhone) && fusedLocationProviderClient != null) {

                                        // Share location to ALL Delivery, state is SENDING, Particular DRIVER
                                        DelRef.child(postSnapshot.getKey()).child("latitude").setValue(location.getLatitude());
                                        DelRef.child(postSnapshot.getKey()).child("longitude").setValue(location.getLongitude());
                                        Log.d("Location123", "Update to Firebase. \n" + location.toString());
                                        Log.d("Location123", "NOT First Time");

                                        // Location UPDATES to Firebase - SINGLE ORDER
                                        /*deliveryMap.put("deliveryID", getIntent().getStringExtra("orderID"));
                                        deliveryMap.put("latitude", location.getLatitude());
                                        deliveryMap.put("longitude", location.getLongitude());
                                        deliveryMap.put("driverPhone", PrevalentAdmin.currentOnlineAdmin.getPhone());
                                        deliveryMap.put("driverName", PrevalentAdmin.currentOnlineAdmin.getName());
                                        deliveryMap.put("state", strStateSend);

                                        DelRef.child(getIntent().getStringExtra("orderID")).updateChildren(deliveryMap).addOnCompleteListener(task -> {
                                            if (task.isSuccessful())
                                            {
                                                Log.d("Location123", "Update to Firebase. \n" + location.toString());
                                                Log.d("Location123", "NOT First Time");
                                            }
                                        });*/
                                    }
                                }


                            }
                        }
                        // First Time Create
                        if (!snapshot.hasChild(getIntent().getStringExtra("orderID"))) {
                            // Location UPDATES to Firebase
                            deliveryMap.put("deliveryID", getIntent().getStringExtra("orderID"));
                            deliveryMap.put("latitude", location.getLatitude());
                            deliveryMap.put("longitude", location.getLongitude());
                            deliveryMap.put("driverPhone", PrevalentAdmin.currentOnlineAdmin.getPhone());
                            deliveryMap.put("driverName", PrevalentAdmin.currentOnlineAdmin.getName());
                            deliveryMap.put("state", strStateSend);

                            DelRef.child(getIntent().getStringExtra("orderID")).updateChildren(deliveryMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful())
                                {
                                    Log.d("Location123", "Update to Firebase. \n" + location.toString());
                                    Log.d("Location123", "First Time Create");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }
    };

    // Phone GPS
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

    // Permission
    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to your GPS")
                    .setPositiveButton("ok", (dialog, which) ->
                            ActivityCompat.requestPermissions(DriverOrderDetails.this,
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
                confirmBeforeSendOrder();
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

    private void confirmBeforeSendOrder() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(DriverOrderDetails.this);
        alertBuilder.setTitle("Confirm before Send");
        alertBuilder.setMessage("Order No: " + tvOrderNo.getText().toString() + "\n" +
                "Address: " + tvCustomerAddress.getText().toString() + "\n" +
                "Please double check before send.");
        alertBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            // Location KEEP UPDATES
            checkSettingAndStartLocationUpdates();

            // change order state
            changeState();
        });
        alertBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
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
                    apiException.startResolutionForResult(DriverOrderDetails.this, 1001);
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

    // Change Order State
    private void changeState() {
        // loading bar declaration
        if (tvOrderState.getText().equals(strReady)) {
            // loading when processing to SEND ORDER
            loadingBar.setTitle("Send Order");
            loadingBar.setMessage("Dear Driver, please wait while we are sending order.");
        } else {
            // loading when processing to DONE ORDER
            loadingBar.setTitle("Done Order");
            loadingBar.setMessage("Dear Driver, please wait while we have delivered the order.");
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

                                            if (postSnapshot.child("state").getValue().equals("R")) {

                                                new AlertDialog.Builder(DriverOrderDetails.this)
                                                        .setTitle("Sending Order")
                                                        .setMessage("The order is sending. " + "\n" +
                                                                "Noted: This app will sharing your location." + "\n" +
                                                                "It will stop sharing your " +
                                                                "when leave this page" + "\n" +
                                                                "This app will re-updating your location " +
                                                                "when back to this page.")
                                                        .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
                                                        .create().show();

                                                OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("S");
                                                //Toast.makeText(AdminSendOrderDetails.this, "Order Sent", Toast.LENGTH_SHORT).show();
                                                btnSendOrder.setVisibility(View.GONE);
                                                btnDoneOrder.setVisibility(View.VISIBLE);
                                                btnView.setVisibility(View.VISIBLE);

                                                tvOrderState.setText(strSend);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#9b870c"));
                                                // CLOSE loading bar
                                                loadingBar.dismiss();


                                                // Get Pick Up Marker, Start Map
                                                getPickUpMarker();
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

    private void confirmBeforeDoneOrder() {
        AlertDialog.Builder alertBuilder;
        alertBuilder = new AlertDialog.Builder(DriverOrderDetails.this);
        alertBuilder.setTitle("Confirm before Marked as Done");
        alertBuilder.setMessage("Order No: " + tvOrderNo.getText().toString() + "\n" +
                "Address: " + tvCustomerAddress.getText().toString() + "\n" +
                "Please double check before Marked as Done.");
        alertBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            doneOrderState();
        });
        alertBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void doneOrderState() {
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

                                            stopLocationUpdates();
                                            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                            stopLocationUpdates();

                                            // change state
                                            OrdersRef.child(snapshot.getKey()).child(orderID).child("state").setValue("D");
                                            Toast.makeText(DriverOrderDetails.this, "Order Done", Toast.LENGTH_SHORT).show();
                                            tvOrderState.setText(strDone);
                                            //DelRef.child(snapshot.getKey()).child("state").setValue("d");

                                            DelRef.child(orderID).child("state").setValue("d");
                                            Log.d("locationDelState", orderID);

                                            // CLOSE loading bar
                                            loadingBar.dismiss();

                                            finish();
                                            //onDestroy();

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

                                            tvDeliveryAddress.setText(str);
                                            tvOrderID.setText(tempOrderID);
                                            tvOrderNo.setText(tempOrderID);
                                            tvOrderTime.setText(tempOrderTime);
                                            tvOrderDate.setText(tempOrderDate);
                                            tvOrderAmount.setText(tempOrderAmount);
                                            tvCustomerName.setText(tempCustomerName);
                                            tvCustomerPhone.setText(tempCustomerPhone);
                                            tvCustomerAddress.setText(builder.toString());
                                            tvCustomerCity.setText(tempCustomerCity);
                                            if (tempOrderState.equals("R")) // Preparing State
                                                tvOrderState.setText(strReady);
                                            else if (tempOrderState.equals("S")) { // Sending State
                                                tvOrderState.setText(strSend);
                                                tvOrderState.setBackgroundResource(R.drawable.bg_order_state_sending);
                                                tvOrderState.setTextColor(Color.parseColor("#9b870c"));
                                                btnSendOrder.setVisibility(View.GONE);
                                                btnDoneOrder.setVisibility(View.VISIBLE);
                                                btnView.setVisibility(View.VISIBLE);
                                            } else { // Done State
                                                tvOrderState.setVisibility(View.INVISIBLE);
                                                btnSendOrder.setVisibility(View.GONE);
                                            }

                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }); // Order Ref

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
                        }); //Cart Ref
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); // User Ref
    }

    @Override
    public void onResume() {
        super.onResume();

        // state is SENDING
        DelRef.child(getIntent().getStringExtra("orderID")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("state")) {
                    String state = snapshot.child("state").getValue().toString();
                    if (state.equals("s") && fusedLocationProviderClient != null) {
                        startLocationUpdates();
                        Log.d("locationResState", state);
                    }
                } else {
                    Log.d("locationResState", "order not yet send");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    // Map
    public void getPickUpMarker() {
        // Orders -> Phone -> Order ID
        // User
        Log.d("stateOrder", "get marker");
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

                                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(orderSnapshot.getKey())) {
                                            if (orderSnapshot.hasChild("state")) {
                                                // If Order State is SENDING
                                                if (orderSnapshot.child("state").getValue().toString().equals("S")) {
                                                    Log.d("stateOrder", "state is S");
                                                    double doubleLat = Double.parseDouble(orderSnapshot
                                                            .child("latitude").getValue().toString());
                                                    double doubleLong = Double.parseDouble(orderSnapshot
                                                            .child("longitude").getValue().toString());

                                                    LatLng latLng = new LatLng(doubleLat, doubleLong);
                                                    MarkerOptions markerOptions = new MarkerOptions()
                                                            .position(latLng)
                                                            .title("Your Pick Up Location");

                                                    // ADD Pick Up Marker
                                                    pickUpMarker = map.addMarker(markerOptions);

                                                    // KEEP CHECKING Order State
                                                    checkOrderState(pickUpMarker.getPosition());
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }); // Order Ref
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); // User Ref
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);

        // Orders -> Phone -> Order ID
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

                                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                        if (orderID.equals(orderSnapshot.getKey())) {
                                            if (orderSnapshot.hasChild("state")) {
                                                // If Order State is SENDING
                                                if (orderSnapshot.child("state").getValue().toString().equals("S")) {
                                                    double doubleLat = Double.parseDouble(orderSnapshot
                                                            .child("latitude").getValue().toString());
                                                    double doubleLong = Double.parseDouble(orderSnapshot
                                                            .child("longitude").getValue().toString());

                                                    LatLng latLng = new LatLng(doubleLat, doubleLong);
                                                    MarkerOptions markerOptions = new MarkerOptions()
                                                            .position(latLng)
                                                            .title("Your Pick Up Location");

                                                    // ADD Pick Up Marker
                                                    //pickUpMarker = map.addMarker(markerOptions);

                                                    getPickUpMarker();
                                                    // KEEP CHECKING Order State
                                                    //checkOrderState(pickUpMarker.getPosition());
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }); // Order Ref
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); // User Ref
    }

    private void checkOrderState(LatLng pickUpLocation) {
        // Orders -> Phone -> Order ID
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
                                            if (postSnapshot.hasChild("state")) {
                                                // If Order State is SENDING
                                                if (postSnapshot.child("state").getValue().toString().equals("S")) {
                                                    // If Order State is SENDING, keep UPDATING delivery man location
                                                    DelRef.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()) {
                                                                try {
                                                                    //deliveryManMarker.remove();
                                                                    double latitude = (double) snapshot.child("latitude").getValue();
                                                                    double longitude = (double) snapshot.child("longitude").getValue();

                                                                    LatLng latLng = new LatLng(latitude, longitude);
                                                                    MarkerOptions markerOptions = new MarkerOptions()
                                                                            .position(latLng)
                                                                            .icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker))
                                                                            .title("Delivery Man Location");

                                                                    // ADD delivery man marker
                                                                    deliveryManMarker = map.addMarker(markerOptions);

                                                                    Log.d("Location123delman", deliveryManMarker.getPosition().toString());
                                                                    Log.d("Location123pickUp", pickUpLocation.toString());

                                                                    //start=pickUpLocation;
                                                                    findRoutes(pickUpLocation,deliveryManMarker.getPosition());


                                                                    // LOOPING with 7s delay
                                                                    startRunnable.run();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    }); // Del Ref
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }); // Order Ref
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); // User Ref
    }

    // START location updates
    private final Runnable startRunnable = new Runnable() {
        @Override
        public void run() {

            // Orders -> Phone -> Order ID
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
                                                if (postSnapshot.hasChild("state")) {
                                                    // If Order State is SENDING
                                                    if (postSnapshot.child("state").getValue().toString().equals("D")) {
                                                        // If the order is DONE
                                                        Toast.makeText(DriverOrderDetails.this,"Order Done",
                                                                Toast.LENGTH_LONG).show();
                                                        Log.d("findRoute", "Runnable Stop");
                                                        finish();
                                                        handler.removeCallbacks(startRunnable);
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            }); // Order Ref
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }); // User Ref



            // If Order State is SENDING, keep UPDATING delivery man location
            DelRef.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        try {
                            Log.d("findRoute", "Runnable continue");
                            // REMOVE OLD route line
                            //polyline.remove();
                            double latitude = (double) snapshot.child("latitude").getValue();
                            double longitude = (double) snapshot.child("longitude").getValue();

                            LatLng latLng = new LatLng(latitude, longitude);
                            deliveryManMarker.setPosition(latLng);
                            double latPickUp = getIntent().getDoubleExtra("latitude", 0.00);
                            double longPickUp = getIntent().getDoubleExtra("longitude",0.00);
                            LatLng pickUpLocation = new LatLng(latPickUp, longPickUp);

                            // Draw route polyline
                            findRoutes(pickUpLocation,deliveryManMarker.getPosition());

                            // ADD NEW route line
                            /*polylineOptions = new PolylineOptions()
                                    .add(pickUpLocation)
                                    .add(deliveryManMarker.getPosition())
                                    .width(10)
                                    .color(Color.BLUE)
                                    .geodesic(true);
                            polyline = map.addPolyline(polylineOptions);*/

                            // ANIMATE camera zoom according to distance between 2 markers
                            // Markers LIST
                            List<Marker> markersList = new ArrayList<>();

                            // ADD to list
                            markersList.add(deliveryManMarker);
                            markersList.add(pickUpMarker);

                            // GET the latLng builder from the marker list
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (Marker m : markersList) {
                                builder.include(m.getPosition());
                            }
                            // Bounds padding
                            int padding = 50;

                            // CREATE bounds
                            LatLngBounds bounds = builder.build();

                            // CREATE camera with bounds
                            //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(deliveryManMarker.getPosition(), 17);
                            // CHECK map is loaded
                            map.setOnMapLoadedCallback(() -> {
                                //animate camera here
                                map.animateCamera(cu);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            handler.postDelayed(this, 7000);
        }
    };

    // Draw route LINE
    public void findRoutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(DriverOrderDetails.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyBu1nHtDBEA1fUu6iKzkSbLG7DBJ0s7uuc")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        //snackbar.show();
        Log.d("findRoute", e.toString());
    }

    @Override
    public void onRoutingStart() {
        Log.d("findRoute", "Finding Route...");
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        Log.d("findRoute", "routeFind");

        if(polylineList !=null) {
            polylineList.clear();
        }
        if (polyline!=null)
            polyline.remove();
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylineList = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if (i == shortestRouteIndex) {
                Log.d("findRoute", "polyline add");
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                polyline = map.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylineList.add(polyline);

            }
        }
    }

    @Override
    public void onRoutingCancelled() {
        findRoutes(start,end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        findRoutes(start,end);

    }
}