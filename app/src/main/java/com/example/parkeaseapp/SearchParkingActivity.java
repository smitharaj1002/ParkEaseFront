package com.example.parkeaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SearchParkingActivity extends AppCompatActivity {

    EditText etSearchLocation;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);

        // Inside the onClickListener for the Search Button
        btnSearch.setOnClickListener(v -> {
            String location = etSearchLocation.getText().toString().trim();
            if (!location.isEmpty()) {
                Intent intent = new Intent(SearchParkingActivity.this, MapsActivity.class);
                intent.putExtra("location", location);
                startActivity(intent);
            } else {
                Toast.makeText(SearchParkingActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
