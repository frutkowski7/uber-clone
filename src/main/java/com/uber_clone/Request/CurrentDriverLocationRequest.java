package com.uber_clone.Request;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CurrentDriverLocationRequest {

    @NotNull
    UUID driverId;

    @NotNull
    double lat;
    @NotNull
    double lng;

    String h3Address;

    Instant updatedAt;

}
