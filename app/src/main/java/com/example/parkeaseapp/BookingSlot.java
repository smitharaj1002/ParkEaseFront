package com.example.parkeaseapp;

public class BookingSlot {
    private String slotName;
    private String status;
    private String duration;
    private String paymentMethod;
    private String userId;

    // ✅ Parameterized Constructor
    public BookingSlot(String slotName, String status, String duration, String paymentMethod, String userId) {
        this.slotName = slotName;
        this.status = status;
        this.duration = duration;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
    }

    // Getters and Setters
    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
