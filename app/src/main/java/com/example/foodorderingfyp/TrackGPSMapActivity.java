package com.example.foodorderingfyp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import Prevalent.Prevalent;

//public class TrackGPSMapActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {
public class TrackGPSMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    SupportMapFragment mapFragment;
    private Geocoder geocoder;
    private Marker pickUpMarker, deliveryManMarker;
    Handler handler = new Handler();
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    MarkerOptions moPickUp, moDeliveryMan;

    // Test Animated Smooth car marker
    Location myLocation = null;
    Location myUpdatedLocation = null;
    float Bearing = 0;
    boolean AnimationStatus = false;
    static Marker carMarker;
    Bitmap BitMapMarker;
    // Test Animated Smooth car marker

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

        // Test Animated Smooth car marker
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.motor);
        Bitmap b = bitmapdraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 110, false);
        // Test Animated Smooth car marker

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_gps_map);
        geocoder = new Geocoder(this, Locale.ENGLISH);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;


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

                        pickUpMarker = map.addMarker(markerOptions);
                        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                        map.animateCamera(cameraUpdate);*/

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

                                        // This can run, GOOD
                                        LatLng latLng = new LatLng(latitude, longitude);
                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(latLng)
                                                .icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker))
                                                .title("Delivery Man Location");

                                        deliveryManMarker = map.addMarker(markerOptions);

                                        //final LatLng pickUpLocation = pickUpMarker.getPosition();
                                        //Log.d("Location123pickUp", pickUpLocation.toString());
                                        //Log.d("Location123delman", latLng.toString());
                                        Log.d("Location123delman", deliveryManMarker.getPosition().toString());
                                        Log.d("Location123pickUp", pickUpLocation.toString());
                                        polylineOptions = new PolylineOptions()
                                                .add(pickUpLocation)
                                                .add(deliveryManMarker.getPosition())
                                                .width(10)
                                                .color(Color.BLUE)
                                                .geodesic(true);
                                        polyline = map.addPolyline(polylineOptions);

                                        /*double avg_Lat = (latLng.latitude + pickUpLocation.latitude) / 2;
                                        double avg_Lng = (latLng.longitude + pickUpLocation.longitude) / 2;
                                        LatLng avgLatLng = null;
                                        avgLatLng = new LatLng(avg_Lat, avg_Lng);
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(avgLatLng, 12);
                                        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                                        map.animateCamera(cameraUpdate);*/
                                        // This can run, GOOD


                                        /*moPickUp = new MarkerOptions().position(pickUpLocation);
                                        moDeliveryMan = new MarkerOptions().position(deliveryManMarker.getPosition());

                                        String url = getUrl(moDeliveryMan.getPosition(), moPickUp.getPosition(), "driving");
                                        new FetchURL(TrackGPSMapActivity.this).execute(url, "driving");*/

                                        // Test Animated Smooth car marker
                                        /*if (AnimationStatus) {
                                            myUpdatedLocation.setLatitude(latitude);
                                            myUpdatedLocation.setLongitude(longitude);
                                        } else {
                                            myLocation.setLatitude(latLng.latitude);
                                            myLocation.setLongitude(longitude);
                                            myUpdatedLocation.setLatitude(latitude);
                                            myUpdatedLocation.setLongitude(longitude);
                                            LatLng latlng = new LatLng(latitude, longitude);
                                            carMarker = map.addMarker(new MarkerOptions().position(latlng).
                                                    flat(true).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));
                                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                                    latlng, 17f);
                                            map.animateCamera(cameraUpdate);
                                        }
                                        Bearing = myUpdatedLocation.getBearing();
                                        LatLng updatedLatLng = new LatLng(myUpdatedLocation.getLatitude(), myUpdatedLocation.getLongitude());
                                        changePositionSmoothly(carMarker, updatedLatLng, Bearing);*/
                                        // Test Animated Smooth car marker

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

    // got bug, back to Track Delivery but will KEEP LOOPING
    // DIRECTLY finish()
    // STOP location updates
    private final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            // If the order is REACHED
            /*new AlertDialog.Builder(TrackGPSMapActivity.this)
                    .setTitle("Order Delivered")
                    .setMessage("The order is already reached to you. This order is no longer to track.")
                    .setCancelable(true)
                    .setPositiveButton("ok", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                        Intent intent = new Intent(TrackGPSMapActivity.this, TrackDelivery.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
                    })
                    .show();
            handler.postDelayed(this, 3000);*/
            finish();
            Intent intent = new Intent(TrackGPSMapActivity.this, TrackDelivery.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back);
        }
    };

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
                            // This can run, GOOD
                            //deliveryManMarker.remove();
                            polyline.remove();
                            double latitude = (double) snapshot.child("latitude").getValue();
                            double longitude = (double) snapshot.child("longitude").getValue();

                            LatLng latLng = new LatLng(latitude, longitude);
                            deliveryManMarker.setPosition(latLng);
                            double latPickUp = getIntent().getDoubleExtra("latitude", 0.00);
                            double longPickUp = getIntent().getDoubleExtra("longitude",0.00);
                            LatLng pickUpLocation = new LatLng(latPickUp, longPickUp);

                            polylineOptions = new PolylineOptions()
                                    .add(pickUpLocation)
                                    .add(deliveryManMarker.getPosition())
                                    .width(10)
                                    .color(Color.BLUE)
                                    .geodesic(true);
                            polyline = map.addPolyline(polylineOptions);

                            /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                            map.animateCamera(cameraUpdate);*/
                            // This can run, GOOD

                            // Animate camera BETWEEN 2 markers
                            double avg_Lat = (latLng.latitude + pickUpLocation.latitude) / 2;
                            double avg_Lng = (latLng.longitude + pickUpLocation.longitude) / 2;
                            LatLng avgLatLng = null;
                            avgLatLng = new LatLng(avg_Lat, avg_Lng);

                            // Get Zoom Size
                            double sub_Lat, sub_Lng;
                            CameraUpdate cu_Zoom;
                            if (latLng.latitude > pickUpLocation.latitude)
                                sub_Lat = latLng.latitude - pickUpLocation.latitude;
                            else
                                sub_Lat = pickUpLocation.latitude - latLng.latitude;

                            if (latLng.longitude > pickUpLocation.longitude)
                                sub_Lng = latLng.longitude - pickUpLocation.longitude;
                            else
                                sub_Lng = pickUpLocation.longitude - latLng.longitude;


                            if (sub_Lat <= 0.0014 && sub_Lng <= 0.003)
                            {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 17);
                                map.animateCamera(cu_Zoom);
                            }
                            else if (sub_Lat <= 0.0013 && sub_Lng <= 0.004)
                            {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 16);
                                map.animateCamera(cu_Zoom);
                            }
                            else if (sub_Lat <= 0.005 && sub_Lng <= 0.005)
                            {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 15);
                                map.animateCamera(cu_Zoom);
                            }
                            else if
                            (sub_Lat <= 0.009 && sub_Lng <= 0.1) {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 14);
                                map.animateCamera(cu_Zoom);
                            }
                            else if (sub_Lat <= 0.05 && sub_Lng <= 0.25)
                            {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 13);
                                map.animateCamera(cu_Zoom);
                            }
                            else {
                                cu_Zoom = CameraUpdateFactory.newLatLngZoom(avgLatLng, 12);
                                map.animateCamera(cu_Zoom);
                            }
                            // Animate camera BETWEEN 2 markers

                            /*MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng);

                            deliveryManMarker = map.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            map.animateCamera(cameraUpdate);*/

                            // Test Animated Smooth car marker
                            /*double latitude = (double) snapshot.child("latitude").getValue();
                            double longitude = (double) snapshot.child("longitude").getValue();
                            if (AnimationStatus) {
                                myUpdatedLocation.setLatitude(latitude);
                                myUpdatedLocation.setLongitude(longitude);
                            } else {
                                myLocation.setLatitude(latitude);
                                myLocation.setLongitude(longitude);
                                myUpdatedLocation.setLatitude(latitude);
                                myUpdatedLocation.setLongitude(longitude);
                                LatLng latlng = new LatLng(latitude, longitude);
                                carMarker = map.addMarker(new MarkerOptions().position(latlng).
                                        flat(true).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                        latlng, 17f);
                                map.animateCamera(cameraUpdate);
                            }
                            Bearing = myUpdatedLocation.getBearing();
                            LatLng updatedLatLng = new LatLng(myUpdatedLocation.getLatitude(), myUpdatedLocation.getLongitude());
                            changePositionSmoothly(carMarker, updatedLatLng, Bearing);*/
                            // Test Animated Smooth car marker
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

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    /*@Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
    }*/

    // Test Animated Smooth car marker
    void changePositionSmoothly(final Marker myMarker, final LatLng newLatLng, final Float bearing) {

        final LatLng startPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        final LatLng finalPosition = newLatLng;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                myMarker.setRotation(bearing);
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                        startPosition.longitude * (1 - t) + finalPosition.longitude * t);

                myMarker.setPosition(currentPosition);

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
                myLocation.setLatitude(newLatLng.latitude);
                myLocation.setLongitude(newLatLng.longitude);
            }
        });
    }
    // Test Animated Smooth car marker
}