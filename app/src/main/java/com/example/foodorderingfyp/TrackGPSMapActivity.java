package com.example.foodorderingfyp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Prevalent.Prevalent;

public class TrackGPSMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    SupportMapFragment mapFragment;
    private Marker pickUpMarker, deliveryManMarker;
    Handler handler = new Handler();
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    Bitmap BitMapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_gps_map);

        TextView tvPickUpAddress = findViewById(R.id.track_gps_address);
        ImageView ivTrackGPSBack = findViewById(R.id.track_gps_back);
        // Back Button
        ivTrackGPSBack.setOnClickListener(v -> onBackPressed());
        String strAddress = getIntent().getStringExtra("address");
        tvPickUpAddress.setText(strAddress);

        // CHANGE delivery man marker
        BitmapDrawable bitmapDraw = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.motor);
        Bitmap b = bitmapDraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 110, false);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_gps_map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);

        String deliveryID = getIntent().getStringExtra("orderID");
        DatabaseReference OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone()).child(deliveryID);

        // Orders -> Phone -> Order ID
        // GET Delivery Location
        OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // If Order State is SENDING
                    if (snapshot.child("state").getValue().toString().equals("S")) {
                        double doubleLat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double doubleLong = Double.parseDouble(snapshot.child("longitude").getValue().toString());

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
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkOrderState(LatLng pickUpLocation) {
        // Order, Delivery REFERENCE
        String deliveryID = getIntent().getStringExtra("orderID");
        DatabaseReference OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone()).child(deliveryID);
        DatabaseReference DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");

        OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if (snapshot.child("state").getValue().toString().equals("S")) {

                        // If Order State is SENDING, keep UPDATING delivery man location
                        DelRef.child(deliveryID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        // ROUTE LINE between markers
                                        polylineOptions = new PolylineOptions()
                                                .add(pickUpLocation)
                                                .add(deliveryManMarker.getPosition())
                                                .width(10)
                                                .color(Color.BLUE)
                                                .geodesic(true);
                                        polyline = map.addPolyline(polylineOptions);

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
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // START location updates
    private final Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            DatabaseReference DelRef = FirebaseDatabase.getInstance().getReference().child("Delivery");
            String deliveryID = getIntent().getStringExtra("orderID");
            DatabaseReference OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                    .child(Prevalent.currentOnlineUser.getPhone()).child(deliveryID);

            // If Order State is DONE
            OrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        if (snapshot.child("state").getValue().toString().equals("D")) {
                            // If the order is DONE
                            //stopRunnable.run(); //this will cause looping bug

                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // If Order State is SENDING, keep UPDATING delivery man location
            DelRef.child(deliveryID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        try {
                            // REMOVE OLD route line
                            polyline.remove();
                            double latitude = (double) snapshot.child("latitude").getValue();
                            double longitude = (double) snapshot.child("longitude").getValue();

                            LatLng latLng = new LatLng(latitude, longitude);
                            deliveryManMarker.setPosition(latLng);
                            double latPickUp = getIntent().getDoubleExtra("latitude", 0.00);
                            double longPickUp = getIntent().getDoubleExtra("longitude",0.00);
                            LatLng pickUpLocation = new LatLng(latPickUp, longPickUp);

                            // ADD NEW route line
                            polylineOptions = new PolylineOptions()
                                    .add(pickUpLocation)
                                    .add(deliveryManMarker.getPosition())
                                    .width(10)
                                    .color(Color.BLUE)
                                    .geodesic(true);
                            polyline = map.addPolyline(polylineOptions);

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
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

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
}