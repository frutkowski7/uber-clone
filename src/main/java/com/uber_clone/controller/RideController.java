package com.uber_clone.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uber_clone.Request.CreateRideRequest;
import com.uber_clone.Response.CreateRideResponse;
import com.uber_clone.Response.RideResponse;
import com.uber_clone.service.RidesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ride")
@RequiredArgsConstructor
public class RideController {

    private final RidesService rideService;

    @PostMapping("/new")
    public ResponseEntity<CreateRideResponse> createRideRequest(
            @Valid @RequestBody CreateRideRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(rideService.addNewRide(request, userEmail));

    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my-rides")
    public ResponseEntity<List<RideResponse>> getRequestedRidesByClient(
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(rideService.getRequestedRidesByClient(userEmail));

    }

    // Preauthorize korzysta z security config, filtra JWT i CustomUserDetails -- tu
    // zapisuje ROLE
    // Z którego korzystam w tej adnotacji (WSZYSTKO JEST W JWT)
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/requested")
    public ResponseEntity<List<RideResponse>> getRequestedRides() {
        return ResponseEntity.ok(rideService.getRequestedRides());
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable("id") UUID rideId,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, authentication.getName()));

    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/start")
    public ResponseEntity<RideResponse> startRide(
            @PathVariable("id") UUID rideId,
            Authentication authentication) {
        System.out.println(authentication.getName());
        return ResponseEntity.ok(rideService.startRide(rideId, authentication.getName()));

    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponse> finishRide(
            @PathVariable("id") UUID rideId,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.finishRide(rideId, authentication.getName()));

    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable("id") UUID rideId,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.cancelRide(rideId, authentication.getName()));

    }

    @GetMapping("/history")
    public ResponseEntity<List<RideResponse>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        return ResponseEntity.ok(rideService.getUserRideHistory(authentication.getName(), page, size));

    }

}
