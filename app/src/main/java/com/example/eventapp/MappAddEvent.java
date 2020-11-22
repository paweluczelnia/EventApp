package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MappAddEvent extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    SupportMapFragment mapFragment;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(51.2300277, 22.559258);
    private static final int DEFAULT_ZOOM = 14;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private LatLng actualLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String coords = getIntent().getStringExtra("coordinates");
        if (coords != null) {
            String[] separatedCoords = coords.split(";");
            actualLocation = new LatLng(Double.parseDouble(separatedCoords[0]),
                    Double.parseDouble(separatedCoords[1]));
        }

        setContentView(R.layout.activity_mapp_add_event);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Initialize fused location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (!locationPermissionGranted) {
            getLocationPermission();
        }

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                updateMarker(latLng);
            }
        });

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    private void getDeviceLocation() {
        if (actualLocation != null) {
            updateMarker(actualLocation);
        } else {
            try {
                if (locationPermissionGranted) {
                    Task<Location> task = fusedLocationProviderClient.getLastLocation();
                    task.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                updateMarker(latLng);
                            } else {
                                updateMarker(defaultLocation);
                            }
                        }
                    });
                } else {
                    updateMarker(defaultLocation);
                }
            } catch (SecurityException e)  {
                Log.e("Exception: %s", e.getMessage(), e);
            }
        }
    }

    private void updateMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position(latLng);
        gMap.clear();
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        gMap.addMarker(options);

        actualLocation = latLng;
    }

    public void confirmMarker(View view) {
        Intent i = new Intent(this, AddEvent.class);
        i.putExtra("coordinates", actualLocation.latitude + ";" + actualLocation.longitude);
        startActivity(i);
    }
}