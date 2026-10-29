package com.uber_clone.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class GetNearbyDriversRequest {

    @NotNull
    double lat;
    @NotNull
    double lng;
    @NotNull
    int rng;

}
