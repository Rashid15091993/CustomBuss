package com.example.bussinfoapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bussinfoapp.databinding.ActivityBussMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
//Приложение для клиентов
public class BussMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ActivityBussMapsBinding binding;
    private GoogleMap mMap;
    private Geocoder geocoder;

    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private int radius = 1;
    private Boolean driverFound  = false;
    private String driverFoundID;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    Marker userLocationMarker, driverBussMarket;
    Circle userLocationAccuracyCircle;

    private String customerID;
    private DatabaseReference DriversAvailableRef;

    private FirebaseAuth mAuth;
    private Button callBussBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBussMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        DriversAvailableRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");

        callBussBtn = (Button) findViewById(R.id.callBuss);

        geocoder = new Geocoder(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callBussBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToUserLocation();

            }
        });
    }
    //Функция определения местоположения автобуса
    private void getNearbyDriversBuss(LatLng location) {
        GeoFire geoFire = new GeoFire(DriversAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude,location.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!driverFound) {
                    driverFound = true;
                    driverFoundID = key;

                }
                else {
                    if (driverBussMarket != null) {
                        driverBussMarket.remove();
                    }
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon));
                    driverBussMarket = mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius = radius + 1;
                    getNearbyDriversBuss(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        zoomToUserLocation();

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());


            }
        }
    };

    private void setUserLocationMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (userLocationMarker == null) {
            //Create a new market
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
            markerOptions.rotation(location.getBearing());
            userLocationMarker = mMap.addMarker(markerOptions);

        } else {
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());

        }


    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @SuppressLint("MissingPermission")
    private void zoomToUserLocation() {
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                getNearbyDriversBuss(latLng);

            }
        });
    }
}