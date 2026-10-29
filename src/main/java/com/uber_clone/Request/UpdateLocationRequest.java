package com.uber_clone.Request;

public record UpdateLocationRequest(
        double latitude,
        double longitude,
        boolean isAvailable) {
}
