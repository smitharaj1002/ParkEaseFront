package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {

    TextView tvParkingSlotDetails, tvSelectDuration;
    RadioGroup radioGroupDuration, radioGroupPayment;
    Button btnConfirmBooking, btnViewBookingStatus; // Added View Booking Status Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvParkingSlotDetails = findViewById(R.id.tvParkingSlotDetails);
        tvSelectDuration = findViewById(R.id.tvSelectDuration);
        radioGroupDuration = findViewById(R.id.radioGroupDuration);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnViewBookingStatus = findViewById(R.id.btnViewBookingStatus); // Initialize Button

        // Display Parking Slot Details
        Intent intent = getIntent();
        String slotDetails = intent.getStringExtra("slotDetails");
        tvParkingSlotDetails.setText(slotDetails != null ? slotDetails : "Parking Slot Details not available");

        // Confirm Button Click Listener
        btnConfirmBooking.setOnClickListener(v -> {
            int selectedDurationId = radioGroupDuration.getCheckedRadioButtonId();
            int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();

            if (selectedDurationId == -1) {
                Toast.makeText(this, "Please select a duration.", Toast.LENGTH_SHORT).show();
            } else if (selectedPaymentId == -1) {
                Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
            } else {
                RadioButton selectedDurationButton = findViewById(selectedDurationId);
                RadioButton selectedPaymentButton = findViewById(selectedPaymentId);

                String selectedDuration = selectedDurationButton.getText().toString();
                String selectedPaymentMethod = selectedPaymentButton.getText().toString();

                // Proceed to Payment Gateway
                Intent paymentIntent = new Intent(BookingActivity.this, PaymentGatewayActivity.class);
                paymentIntent.putExtra("duration", selectedDuration);
                paymentIntent.putExtra("paymentMethod", selectedPaymentMethod);
                startActivity(paymentIntent);
            }
        });

        // View Booking Status Button Click Listener
        btnViewBookingStatus.setOnClickListener(v -> {
            Intent statusIntent = new Intent(BookingActivity.this, BookingStatusActivity.class);
            startActivity(statusIntent);
        });
    }
}
