package com.example.googlemapsapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.maps.SupportMapFragment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 11;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentPosition;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private SearchView searchView;
    private MaterialButton drawBtn;
    private MaterialButton eraserBtn;
    private List<Marker> markers = new ArrayList<>();
    public static LatLng end = null;
    private ArrayList<LatLng> latLngArrayList;
    private ArrayList<String> locationNameArraylist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        latLngArrayList = new ArrayList<>();
        locationNameArraylist = new ArrayList<>();
        final PolylineOptions[] polylineOptions = {new PolylineOptions()
                .color(Color.RED)
                .width(5f)};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.search_view);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // draw
        drawBtn = findViewById(R.id.drawBtn);
        eraserBtn = findViewById(R.id.eraserBtn);

        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < latLngArrayList.size(); i++) {
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(latLngArrayList.get(i))
                            .title(locationNameArraylist.get(i)));
                    markers.add(marker);
                }

                for (Marker marker : markers) {
                    polylineOptions[0].add(marker.getPosition());
                }

                map.addPolyline(polylineOptions[0]);
            }
        });

        // eraser
        eraserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear all markers
                for (Marker marker : markers) {
                    marker.remove();
                }
                markers.clear();
                map.clear();
                latLngArrayList.clear();
                locationNameArraylist.clear();
                polylineOptions[0] = null;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                // checking if the entered location is null or not.
                if (location != null || location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    latLngArrayList.add(latLng);
                    locationNameArraylist.add(location);
                    map.addMarker(new MarkerOptions().position(latLng).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionNormal:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.optionSatellite:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.optionHybrid:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.optionTerrain:
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.optionNone:
                map.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            if (isPermissionGranted()) {
                getCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    };

    boolean isPermissionGranted(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        if (isPermissionGranted()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15));
                        map.getUiSettings().setZoomControlsEnabled(true);
                        map.getUiSettings().setMapToolbarEnabled(true);
                        map.getUiSettings().setMyLocationButtonEnabled(true);
                        map.setMyLocationEnabled(true);
                    }
                }
            });
        }
    }

}

