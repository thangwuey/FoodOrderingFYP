package com.example.foodorderingfyp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectAddressActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap map;
    SupportMapFragment mapFragment;
    private SearchView searchView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);


        Button btnSelectAddress = findViewById(R.id.select_address_button);
        ImageView ivSelectAddressBack = findViewById(R.id.select_address_back);
        searchView = findViewById(R.id.select_address_sv);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.select_address_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.ENGLISH);

        // Back Button
        ivSelectAddressBack.setOnClickListener(v -> onBackPressed());

        // Select Address button
        btnSelectAddress.setOnClickListener(v -> confirmAddress());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                map.clear(); // CLEAR marker
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null || location.equals("")) {
                    try {
                        addressList = geocoder.getFromLocationName(location,1);

                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        marker = map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(location));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                        Log.d("Location123lat", String.valueOf(address.getLatitude()));
                        Log.d("Location123long", String.valueOf(address.getLongitude()));
                        Log.d("Location123country", address.getCountryName());
                        Log.d("Location123locality", address.getLocality());
                        Log.d("Location123addressLine", address.getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
    }

    private void confirmAddress() {
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                String locality = address.getLocality();
                Log.d("Location123latConfirm",String.valueOf(latitude));
                Log.d("Location123longConfirm", String.valueOf(longitude));
                Log.d("Location123addConfirm",  streetAddress);
                Log.d("Location123localConfirm",  locality);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("address", streetAddress);
                returnIntent.putExtra("latitude", latitude);
                returnIntent.putExtra("longitude", longitude);
                returnIntent.putExtra("locality", locality);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(this); // Pick up location by CLICK

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true); // top right LOCATE user location button
            zoomToUserLocation(); // ZOOM user current location
        }
    }

    private void zoomToUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

            locationTask.addOnSuccessListener(location -> {
                // we have a location
                if (location != null) {

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                    marker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Your current location"));
                } else // no location
                    Log.d("Location123user", "Location was null...");
            }).addOnFailureListener(e -> Log.d("Location123user", e.getLocalizedMessage()));
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Log.d("Location123click", latLng.toString());
        map.clear(); // CLEAR marker
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(streetAddress));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}