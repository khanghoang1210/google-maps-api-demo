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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 11;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentPosition;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private SearchView mSearchView;
    public static LatLng end = null;
    private MaterialButton drawBtn;
    private MaterialButton eraserBtn;
    List<Marker> markerList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        drawBtn = findViewById(R.id.drawBtn);
        eraserBtn = findViewById(R.id.eraserBtn);

        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<LatLng> markerPositions = new ArrayList<>();
                for (Marker marker : markerList) {
                    LatLng position = marker.getPosition();
                    markerPositions.add(position);
                }
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(markerPositions)
                        .add(markerPositions.get(0))
                        .width(5)
                        .color(Color.RED);
                Polyline polyline = map.addPolyline(polylineOptions);
            }
        });

        eraserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
                markerList.clear();
            }
        });


        mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = mSearchView.getQuery().toString();
                List<Address> addresses = null;
                if (location != null) {
                    Log.e("Location", location);
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    try {
                        addresses = geocoder.getFromLocationName(location, 1);
                        if (addresses != null) {
                            Address address = addresses.get(0);
                            Log.e("address", String.valueOf(address.getLatitude()));
                            end = new LatLng(address.getLatitude(), address.getLongitude());
                            MarkerOptions markerEnd = new MarkerOptions().position(end).title(location);
                            Marker marker = map.addMarker(markerEnd);
                            markerList.add(marker);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(end, 19));
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Không tìm ra địa điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập gì đó", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng);
                    Marker marker = map.addMarker(markerOptions);
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (!addresses.isEmpty()) {
                            String address = addresses.get(0).getAddressLine(0);
                            marker.setTitle(address);
                        }
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    marker.showInfoWindow();
                    markerList.add(marker);
                }
            });
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
