package com.uber_clone.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.uber_clone.dto.PointDto;
import com.uber_clone.model.Ride;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CreateRideResponse {

    @NotNull
    private UUID id;

    @NotNull
    private Ride.Status status;

    @NotNull
    private BigDecimal price;

    @NotNull
    private List<PointDto> coordinates;

}
