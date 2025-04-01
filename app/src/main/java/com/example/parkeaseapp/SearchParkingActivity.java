package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SearchParkingActivity extends AppCompatActivity {

    private EditText etSearchLocation;
    private Button btnSearch, btnViewBookingStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnViewBookingStatus = findViewById(R.id.btnViewBookingStatus);
        progressBar = findViewById(R.id.progressBar);

        // Search Parking Slots
        btnSearch.setOnClickListener(v -> {
            String location = etSearchLocation.getText().toString().trim();

            if (!location.isEmpty()) {
                showLoading(true);

                // Navigate to Maps with location
                Intent intent = new Intent(SearchParkingActivity.this, MapsActivity.class);
                intent.putExtra("location", location);
                startActivity(intent);

                // Simulate loading for 2 seconds
                etSearchLocation.postDelayed(() -> showLoading(false), 2000);

            } else {
                Toast.makeText(SearchParkingActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });

        // View Booking Status
        btnViewBookingStatus.setOnClickListener(v -> {
            Intent statusIntent = new Intent(SearchParkingActivity.this, BookingStatusActivity.class);
            startActivity(statusIntent);
        });
    }

    // Show or hide progress bar
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!isLoading);
    }
}
