package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    RadioGroup paymentOptions;
    Button btnConfirmPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentOptions = findViewById(R.id.paymentOptions);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // Confirm Payment Button Click
        btnConfirmPayment.setOnClickListener(view -> {
            int selectedId = paymentOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            } else {
                RadioButton selectedButton = findViewById(selectedId);
                String paymentMethod = selectedButton.getText().toString();

                // Proceed with Payment Logic
                Toast.makeText(this, "Selected: " + paymentMethod, Toast.LENGTH_SHORT).show();

                // Redirect based on payment selection
                if (paymentMethod.equalsIgnoreCase("UPI") || paymentMethod.equalsIgnoreCase("Debit/Credit Card")) {
                    Intent intent = new Intent(PaymentActivity.this, PaymentGatewayActivity.class);
                    intent.putExtra("paymentMethod", paymentMethod);
                    startActivity(intent);
                } else {
                    // Handle Cash on Spot
                    Intent intent = new Intent(PaymentActivity.this, ConfirmationActivity.class);
                    intent.putExtra("status", "Success - Cash on Spot Selected");
                    startActivity(intent);
                }
            }
        });
    }
}
