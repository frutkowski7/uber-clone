package com.uber_clone.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CreateRideRequest {

    // Te adnotacje pozwalają
    // na walidacje JSON
    @NotNull
    private Double pickupLatitude;
    @NotNull
    private Double pickupLongitude;

    @NotNull
    private Double dropoffLatitude;
    @NotNull
    private Double dropoffLongitude;

    // Gettery, settery
}