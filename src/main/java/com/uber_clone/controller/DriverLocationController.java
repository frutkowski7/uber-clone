package com.uber_clone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uber_clone.Request.CountDriversRequest;
import com.uber_clone.Request.CurrentDriverLocationRequest;
import com.uber_clone.Request.GetNearbyDriversRequest;
import com.uber_clone.dto.DriverLocation;
import com.uber_clone.service.DriverLocationService;

/* 

3 Kluczowe cechy WebSocketów:
Trwałość (Persistent Connection): Połączenie TCP zostaje otwarte tak długo, 
jak aplikacja kierowcy jest włączona.

Dwukierunkowość (Full-Duplex): Zarówno klient może wysyłać dane do serwera 
(np. pozycję GPS), jak i serwer może sam z siebie wysłać dane do klienta 
(np. powiadomienie "Pasażer zamówił przejazd!").

Niski narzut (Low Overhead): W wysyłanej paczce nie ma potężnych nagłówków HTTP 
(cookie, user-agent, tokenów w każdym zapytaniu). Przesyłany jest czysty, mały JSON lub bajty, 
co oszczędza baterię i transfer w telefonie.
*/

@RestController
@RequestMapping("/api/driver/location")
public class DriverLocationController {

    private final DriverLocationService driverLocationService;

    // Konstruktor
    public DriverLocationController(DriverLocationService driverLocationService) {
        this.driverLocationService = driverLocationService;
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/update")
    public ResponseEntity<DriverLocation> updateDriverLocation(Authentication authentication,
            @RequestBody CurrentDriverLocationRequest currentDriverLocationRequest) {

        return ResponseEntity.ok(driverLocationService.updateDriverLocation(authentication.getName(),
                currentDriverLocationRequest.getLat(), currentDriverLocationRequest.getLng()));

    }

    @GetMapping("/get")
    public ResponseEntity<List<DriverLocation>> getDriversLocations(Authentication authentication) {

        return ResponseEntity.ok(driverLocationService.getDriversLocations());

    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DriverLocation>> getNearbyDrivers(Authentication authentication,
            @RequestBody GetNearbyDriversRequest req) {

        return ResponseEntity
                .ok(driverLocationService.findNearbyAvailableDrivers(req.getLat(), req.getLng(), req.getRng()));

    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDrivers(Authentication authentication,
            @RequestBody CountDriversRequest req) {

        return ResponseEntity.ok(driverLocationService.countDriversInCell(req.getLat(), req.getLng()));

    }

}
