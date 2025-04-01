package com.example.parkeaseapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BookingService {

    @POST("/api/bookings/create")
    Call<Void> createBooking(@Body BookingSlot bookingSlot);
}
