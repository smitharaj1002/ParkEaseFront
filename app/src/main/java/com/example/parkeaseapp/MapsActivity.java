package com.example.parkeaseapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String BASE_URL = "http://192.168.118.49:8080/api/slots/all";
    private final LatLng DEFAULT_LOCATION = new LatLng(12.9716, 77.5946); // Bengaluru
    private final OkHttpClient client = new OkHttpClient();
    private ClusterManager<MyItem> mClusterManager;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Check if location services are enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Prompt the user to enable location services
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLastLocation();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true); // Enable location layer
            googleMap.getUiSettings().setMyLocationButtonEnabled(true); // Enable location button
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);

        // Remove cluster manager's marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            // Log the marker click event
            Log.d("Marker Click", "Marker clicked: " + marker.getTitle());

            // Start BookingActivity with data
            String title = marker.getTitle();
            if (title != null) {
                Intent intent = new Intent(MapsActivity.this, BookingActivity.class);
                intent.putExtra("parkingName", title);
                intent.putExtra("latitude", marker.getPosition().latitude);
                intent.putExtra("longitude", marker.getPosition().longitude);

                // Move the Intent handling outside of the UI thread
                runOnUiThread(() -> {
                    startActivity(intent);
                });
            }
            return true; // Return true to consume the event
        });

        // Handle Cluster Item Click
        mClusterManager.setOnClusterItemClickListener(item -> {
            Log.d("Marker Click", "Marker clicked: " + item.getTitle());
            Intent intent = new Intent(MapsActivity.this, BookingActivity.class);
            intent.putExtra("parkingName", item.getTitle());
            intent.putExtra("latitude", item.getPosition().latitude);
            intent.putExtra("longitude", item.getPosition().longitude);
            intent.putExtra("price", item.getSnippet());
            startActivity(intent);
            return true; // Return true to indicate the click was handled
        });

        // Move to default location (Bengaluru)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12));

        // Fetch parking slots and show markers
        fetchParkingSlots();

        // Perform search once the map is ready
        String location = getIntent().getStringExtra("location");
        if (location != null) {
            searchLocation(location);
        }
    }






    private void fetchParkingSlots() {
        Request request = new Request.Builder().url(BASE_URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MapsActivity.this, "Error fetching slots: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("JSON Response", jsonData);
                        JSONArray jsonArray = new JSONArray(jsonData);

                        runOnUiThread(() -> {
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject slot = jsonArray.getJSONObject(i);
                                    double latitude = slot.getDouble("latitude");
                                    double longitude = slot.getDouble("longitude");
                                    String name = slot.getString("name");
                                    int availableSlots = slot.getInt("availableSlots"); // Adjust to match JSON
                                    double price = slot.getDouble("price");
                                    Log.d("Marker Check", "Adding marker for: " + name +
                                            " at Latitude: " + latitude + ", Longitude: " + longitude);

                                    // Create marker using MyItem
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    MyItem item = new MyItem(latitude, longitude, name + " - ₹" + price, "Available Slots: " + availableSlots);

                                    mClusterManager.addItem(item);
                                    Log.d("Marker Check", "Marker added: " + name + " at " + latitude + ", " + longitude);

                                    mClusterManager.cluster();
                                    Log.d("JSON Response", jsonData);
                                    Log.d("Marker Check", "Adding marker for: " + name + " at Latitude: " + latitude + ", Longitude: " + longitude);
                                }
                            } catch (JSONException e) {
                                Log.e("JSON Error", "Error parsing JSON", e);
                            }
                        });

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }


    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    } else {
                        Toast.makeText(this, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e ->
                        Toast.makeText(this, "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Toast.makeText(this, "Location Found: " + location, Toast.LENGTH_SHORT).show();

                if (latLng != null) {
                    Log.d("SearchLocation", "Moving to: " + location + " at " + latLng.latitude + ", " + latLng.longitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14)); // Zoom level 14 for closer view
                } else {
                    Log.e("SearchLocation", "Location not found: " + location);
                    Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Location not found. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {            e.printStackTrace();
            Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Call to super method

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now you can access location
                getLastLocation();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied! Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
