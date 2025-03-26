package com.example.parkeaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(marker -> {
            String slotDetails = marker.getTitle() + "\n" + marker.getSnippet();
            Intent intent = new Intent(MapsActivity.this, BookingActivity.class);
            intent.putExtra("slotDetails", slotDetails);
            startActivity(intent);
            return false;
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    fetchParkingSpots(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void fetchParkingSpots(double latitude, double longitude) {
        new Thread(() -> {
            try {
                String apiUrl = "http://10.250.17.224:8080/api/slots/search?latitude=" + latitude + "&longitude=" + longitude;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                System.out.println("API URL: " + apiUrl);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    runOnUiThread(() -> displayParkingSpots(response.toString()));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Unable to fetch data. Please check your internet connection or try again later.", Toast.LENGTH_SHORT).show();
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void displayParkingSpots(String response) {
        try {
            JSONArray parkingSpots = new JSONArray(response);
            if (parkingSpots.length() == 0) {
                Toast.makeText(this, "No parking spots available for this location", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < parkingSpots.length(); i++) {
                JSONObject spot = parkingSpots.getJSONObject(i);
                double lat = spot.getDouble("latitude");
                double lng = spot.getDouble("longitude");
                String name = spot.optString("name", "Parking Spot");
                int availableSlots = spot.optInt("availableSlots", 0);
                double price = spot.optDouble("price", 0.0);
                String status = spot.optString("status", "Unknown");

                LatLng position = new LatLng(lat, lng);
                String markerTitle = name + " - ₹" + price + " - " + availableSlots + " Slots";
                mMap.addMarker(new MarkerOptions().position(position).title(markerTitle).snippet("Status: " + status));
            }
            Toast.makeText(this, "Parking spots displayed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
