package com.uber_clone.event;

import java.util.UUID;

import com.uber_clone.model.DriverActivity.DriverStatus;

public record DriverStatusChangedEvent(
        UUID driverId,
        DriverStatus newStatus,
        boolean isAvailable) {
}