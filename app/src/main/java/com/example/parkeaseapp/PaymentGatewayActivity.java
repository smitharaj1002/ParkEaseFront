package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentGatewayActivity extends AppCompatActivity {

    TextView tvProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_gateway);

        tvProcessing = findViewById(R.id.tvProcessing);

        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        tvProcessing.setText("Processing your payment via " + paymentMethod + "...");

        // Simulating Payment Processing using Handler
        new Handler().postDelayed(() -> {
            // Redirect to Confirmation Screen with Success or Failure
            Intent intent = new Intent(PaymentGatewayActivity.this, ConfirmationActivity.class);
            intent.putExtra("status", "Payment Successful via " + paymentMethod);
            startActivity(intent);
            finish();
        }, 3000); // 3-second simulation
    }
}
