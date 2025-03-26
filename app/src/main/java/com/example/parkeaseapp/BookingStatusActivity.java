package com.example.parkeaseapp;

import android.os.Bundle;
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

    TextView tvBookingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_status);

        tvBookingStatus = findViewById(R.id.tvBookingStatus);

        // Fetch and display booking status
        fetchBookingStatus();
    }

    private void fetchBookingStatus() {
        new Thread(() -> {
            try {
                String apiUrl = "http://10.250.17.224:8080/api/bookings/status"; // Adjust endpoint if necessary
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
                    runOnUiThread(() -> displayBookingStatus(response.toString()));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to fetch booking status. Status Code: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error fetching booking status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void displayBookingStatus(String response) {
        try {
            JSONArray bookingData = new JSONArray(response);
            if (bookingData.length() == 0) {
                tvBookingStatus.setText("No bookings found.");
                return;
            }
            StringBuilder statusBuilder = new StringBuilder();
            for (int i = 0; i < bookingData.length(); i++) {
                JSONObject booking = bookingData.getJSONObject(i);
                String slotName = booking.optString("slotName", "Unknown Slot");
                String status = booking.optString("status", "Pending");
                String duration = booking.optString("duration", "N/A");
                String paymentMethod = booking.optString("paymentMethod", "N/A");

                statusBuilder.append("Slot: ").append(slotName)
                        .append("\nStatus: ").append(status)
                        .append("\nDuration: ").append(duration)
                        .append("\nPayment Method: ").append(paymentMethod)
                        .append("\n--------------------------\n");
            }
            tvBookingStatus.setText(statusBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing booking status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
