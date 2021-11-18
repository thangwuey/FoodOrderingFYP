package com.example.foodorderingfyp.Admin;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.foodorderingfyp.R;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AdminSelectAddress extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private GoogleMap map;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;
    private Marker marker;
    TextView tvSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_address);

        Button btnSelectAddress = findViewById(R.id.admin_select_address_button);
        ImageView ivSelectAddressBack = findViewById(R.id.admin_select_address_back);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.admin_select_address_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.ENGLISH);

        // Back Button
        ivSelectAddressBack.setOnClickListener(v -> onBackPressed());

        // Select Address button
        btnSelectAddress.setOnClickListener(v -> confirmAddress());

        // Searching Place
        Places.initialize(getApplicationContext(), "AIzaSyBu1nHtDBEA1fUu6iKzkSbLG7DBJ0s7uuc");
        tvSearch = findViewById(R.id.asa_tvSearch);

        tvSearch.setOnClickListener(v -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG,
                    Place.Field.NAME);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList)
                    .build(AdminSelectAddress.this);
            startActivityForResult(intent,100);
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
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            tvSearch.setText(address.getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                tvSearch.setText(address.getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 & resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            tvSearch.setText(place.getAddress());

            map.clear(); // CLEAR marker
            String location = tvSearch.getText().toString();
            List<Address> addressList;

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
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);

            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}