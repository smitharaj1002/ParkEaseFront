package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmationActivity extends AppCompatActivity {

    TextView tvConfirmationMessage;
    Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        String status = getIntent().getStringExtra("status");
        tvConfirmationMessage.setText(status);

        // Redirect to Map on Button Click
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmationActivity.this, SearchParkingActivity.class);
            startActivity(intent);
            finish();

        });
    }
}
