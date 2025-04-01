package com.example.parkeaseapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BookingStatusActivity extends AppCompatActivity {

    LinearLayout bookingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_status);

        bookingLayout = findViewById(R.id.bookingLayout);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        // Display loading message
        TextView loadingTextView = new TextView(this);
        loadingTextView.setText("Fetching booking status...");
        bookingLayout.addView(loadingTextView);

        // Fetch and display booking status
        fetchBookingStatus(loadingTextView);

        // Handle back to home button click
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(BookingStatusActivity.this, SearchParkingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void fetchBookingStatus(TextView loadingTextView) {
        new Thread(() -> {
            try {
                String apiUrl = "http://192.168.118.49:8080/api/bookings/status";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    runOnUiThread(() -> {
                        loadingTextView.setVisibility(TextView.GONE);
                        handleResponse(response.toString());
                    });
                } else {
                    runOnUiThread(() -> {
                        loadingTextView.setText("Failed to fetch booking status. Status Code: " + responseCode);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    loadingTextView.setText("Error fetching booking status: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleResponse(String response) {
        try {
            // Check if response is JSON or plain text
            if (response.trim().equals("Booking status fetched successfully!")) {
                TextView successMessage = new TextView(this);
                successMessage.setText("No bookings available.");
                successMessage.setTextColor(Color.GRAY);
                bookingLayout.addView(successMessage);
                return;
            }

            // Attempt to parse JSON response
            JSONArray bookingData = new JSONArray(response);
            if (bookingData.length() == 0) {
                TextView noBookingTextView = new TextView(this);
                noBookingTextView.setText("No bookings found.");
                bookingLayout.addView(noBookingTextView);
                return;
            }
            for (int i = 0; i < bookingData.length(); i++) {
                JSONObject booking = bookingData.getJSONObject(i);

                // Extract data safely using optString
                String slotName = booking.optString("slotName", "Unknown Slot");
                String status = booking.optString("status", "Pending");
                String duration = booking.optString("duration", "N/A");
                String paymentMethod = booking.optString("paymentMethod", "N/A");
                boolean isPast = booking.optBoolean("isPast", false);

                TextView bookingView = new TextView(this);
                bookingView.setText("Slot: " + slotName +
                        "\nStatus: " + status +
                        "\nDuration: " + duration +
                        "\nPayment Method: " + paymentMethod +
                        "\n--------------------------");

                // Apply colors based on status
                if (isPast) {
                    bookingView.setTextColor(Color.GRAY);
                } else {
                    bookingView.setTextColor(Color.BLACK);
                    bookingView.setTextSize(18);
                }
                bookingLayout.addView(bookingView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing booking status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
