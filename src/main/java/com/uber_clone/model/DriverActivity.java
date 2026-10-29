package com.uber_clone.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class DriverActivity {

    public enum DriverStatus {
        OFFLINE,
        AVAILABLE,
        ON_TRIP
    }

    private UUID driverId;
    private DriverStatus driverStatus;
    private Instant updatedAt;

}
