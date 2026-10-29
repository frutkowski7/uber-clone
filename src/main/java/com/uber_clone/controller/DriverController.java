
package com.uber_clone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uber_clone.service.DriverService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/add")
    public ResponseEntity<String> addDriver(
            Authentication authentication) {

        String userEmail = authentication.getName();

        driverService.addDriver(userEmail);

        return ResponseEntity.ok("Driver created");
    }
}
