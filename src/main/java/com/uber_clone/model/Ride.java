package com.uber_clone.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.uber_clone.dto.PointDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    public enum Status {
        REQUESTED,
        ACCEPTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    private UUID id;
    private UUID driverId;
    private UUID clientId;
    private Status status;

    private double pickupLatitude;
    private double pickupLongitude;
    private double dropoffLatitude;
    private double dropoffLongitude;
    // Kod komórki H3 miejsca odbioru
    private BigDecimal price;

    private List<PointDto> coordinates;

    private Instant createdAt;
    private Instant updatedAt;

}
