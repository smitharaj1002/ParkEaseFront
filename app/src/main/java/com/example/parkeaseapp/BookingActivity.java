package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookingActivity extends AppCompatActivity {

    TextView tvParkingSlotDetails, tvSelectDuration, tvCalculatedPrice;
    RadioGroup radioGroupDuration, radioGroupPayment;
    Button btnConfirmBooking;
    View upiAppLayout;
    ImageView imgGPay, imgPhonePe, imgPaytm, imgAmazonPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvParkingSlotDetails = findViewById(R.id.tvParkingSlotDetails);
        tvSelectDuration = findViewById(R.id.tvSelectDuration);
        tvCalculatedPrice = findViewById(R.id.tvCalculatedPrice);
        radioGroupDuration = findViewById(R.id.radioGroupDuration);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        upiAppLayout = findViewById(R.id.upiAppLayout);
        imgGPay = findViewById(R.id.imgGPay);
        imgPhonePe = findViewById(R.id.imgPhonePe);
        imgPaytm = findViewById(R.id.imgPaytm);
        imgAmazonPay = findViewById(R.id.imgAmazonPay);

        // Display Parking Slot Details
        Intent intent = getIntent();
        String slotDetails = intent.getStringExtra("slotDetails");
        if (slotDetails != null) {
            Toast.makeText(this, slotDetails, Toast.LENGTH_LONG).show();
            tvParkingSlotDetails.setText(slotDetails);
        } else {
            tvParkingSlotDetails.setText("Parking Slot Details not available");
        }

        // Show UPI Icons when UPI is selected
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbUPI) {
                upiAppLayout.setVisibility(View.VISIBLE);
            } else {
                upiAppLayout.setVisibility(View.GONE);
            }
        });

        // Update Price when Duration is Selected
        radioGroupDuration.setOnCheckedChangeListener((group, checkedId) -> updatePrice());

        // Confirm Booking with Price Calculation
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

                // Extract price from slot details
                double basePrice = extractPrice(slotDetails);

                // Calculate Final Price
                double finalPrice = calculatePrice(basePrice, selectedDuration);

                // Show confirmation
                Toast.makeText(this, "Booking confirmed! Final Price: ₹" + finalPrice, Toast.LENGTH_SHORT).show();

                // Send booking data to backend
                sendBookingToBackend(slotDetails, "Confirmed", selectedDuration, selectedPaymentMethod);

                // Proceed to Payment Gateway
                Intent paymentIntent = new Intent(BookingActivity.this, PaymentGatewayActivity.class);
                paymentIntent.putExtra("duration", selectedDuration);
                paymentIntent.putExtra("paymentMethod", selectedPaymentMethod);
                paymentIntent.putExtra("finalPrice", finalPrice);
                startActivity(paymentIntent);
            }
        });

        // Set UPI App Click Listeners
        imgGPay.setOnClickListener(v -> openUPIApp("com.google.android.apps.nbu.paisa.user"));
        imgPhonePe.setOnClickListener(v -> openUPIApp("com.phonepe.app"));
        imgPaytm.setOnClickListener(v -> openUPIApp("net.one97.paytm"));
        imgAmazonPay.setOnClickListener(v -> openUPIApp("in.amazon.mShop.android.shopping"));
    }

    private void sendBookingToBackend(String slotName, String status, String duration, String paymentMethod) {
        BookingSlot bookingSlot = new BookingSlot(slotName, status, duration, paymentMethod, "Niri");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.118.49:8080") // Backend base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BookingService bookingService = retrofit.create(BookingService.class);

        Call<Void> call = bookingService.createBooking(bookingSlot);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookingActivity.this, "Booking successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookingActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Extract the base price using regex
    private double extractPrice(String slotDetails) {
        try {
            String regex = "₹\\s*(\\d+(\\.\\d+)?)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(slotDetails);

            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            } else {
                throw new Exception("Price not found");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error extracting price. Defaulting to ₹35.", Toast.LENGTH_SHORT).show();
            return 35.0;
        }
    }

    // Calculate final price based on selected duration
    private double calculatePrice(double basePrice, String selectedDuration) {
        switch (selectedDuration) {
            case "30 Minutes":
                return basePrice;
            case "1 Hour":
                return basePrice * 2;
            case "2 Hours":
                return basePrice * 4;
            default:
                return basePrice;
        }
    }

    // Update price on UI
    private void updatePrice() {
        int selectedDurationId = radioGroupDuration.getCheckedRadioButtonId();
        if (selectedDurationId != -1) {
            RadioButton selectedDurationButton = findViewById(selectedDurationId);
            String selectedDuration = selectedDurationButton.getText().toString();
            double basePrice = extractPrice(tvParkingSlotDetails.getText().toString());
            double finalPrice = calculatePrice(basePrice, selectedDuration);
            tvCalculatedPrice.setText("Estimated Price: ₹" + finalPrice);
        } else {
            tvCalculatedPrice.setText("Estimated Price: ₹0");
        }
    }

    // Method to Handle UPI App Redirection
    private void openUPIApp(String packageName) {
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "App not installed. Please install the app to proceed.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening the app.", Toast.LENGTH_SHORT).show();
        }
    }
}
