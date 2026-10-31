package com.uber_clone.Response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.uber_clone.dto.PointDto;
import com.uber_clone.model.Ride;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcceptRideResponse {

    private UUID id;
    private UUID clientId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private Ride.Status status;
    private BigDecimal price;
    private Instant createdAt;
    private List<PointDto> coordinates;

}