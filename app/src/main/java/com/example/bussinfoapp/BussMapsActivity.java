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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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

import java.util.List;

public class BussMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ActivityBussMapsBinding binding;
    private GoogleMap mMap;
    private Geocoder geocoder;
    public static final int PERMISSIONS_FINE_LOCATION = 99;
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    private DatabaseReference assignedCustomRef, AssignedCustomPositionRef;
    private String driverID, customID = "";

    private ValueEventListener AssignedCustomPositionListener;
    Marker PickUpMarker;



    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }

        binding = ActivityBussMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        AssignedBussDriversPosition();

    }

    private void getAssignedCustomRequest() {
        assignedCustomRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(driverID);

        assignedCustomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    customID = snapshot.getValue().toString();

                    AssignedBussDriversPosition();

                }
                else {
                    customID = "";

                    if(PickUpMarker != null) {
                        PickUpMarker.remove();
                    }
                    if(AssignedCustomPositionListener != null) {
                        AssignedCustomPositionRef.removeEventListener(AssignedCustomPositionListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AssignedBussDriversPosition() {
        AssignedCustomPositionRef = FirebaseDatabase.getInstance().getReference().child("Custom Requests")
                .child(customID).child("l");

        AssignedCustomPositionListener = AssignedCustomPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> customerPositionMap = (List<Object>) snapshot.getValue();
                    double LocationLat = LocationLat = Double.parseDouble(customerPositionMap.get(0).toString());
                    ;
                    double LocationLng = LocationLng = Double.parseDouble(customerPositionMap.get(1).toString());

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);

                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng)
                            .title("Забрать клиента тут").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(DriverLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}