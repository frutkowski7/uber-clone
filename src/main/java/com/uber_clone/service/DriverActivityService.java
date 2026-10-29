package com.uber_clone.service;

import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.uber_clone.event.DriverStatusChangedEvent;
import com.uber_clone.exceptions.UserIsNotDriverException;
import com.uber_clone.model.DriverActivity;
import com.uber_clone.model.DriverActivity.DriverStatus;
import com.uber_clone.repository.DriverActivityRepository;
import com.uber_clone.repository.DriverRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverActivityService {

    private final DriverActivityRepository driverActivityRepository;
    private final DriverRepository driverRepository;

    @EventListener
    public DriverActivity updateDriverStatus(DriverStatusChangedEvent event) {

        if (!driverRepository.isDriver(event.driverId())) {
            throw new UserIsNotDriverException("USER IS NOT A DRIVER");
        }

        return driverActivityRepository.updateStatus(event.driverId(), event.newStatus());

    }

    /*
     * public DriverActivity setDriverStatusOffline(String email) {
     * UUID driverId = userRepository.getIdByEmail(email)
     * .orElseThrow(() -> new UserNotFoundException("User not found!"));
     * 
     * DriverStatus currentDriverStatus = getDriverStatus(driverId);
     * if (currentDriverStatus.equals(DriverStatus.ON_TRIP)) {
     * throw new RideNotFoundException("Driver is currently driving");
     * }
     * driverLocationService.updateDriverStatus(driverId, false);
     * 
     * return updateDriverStatus(driverId, DriverStatus.OFFLINE);
     * 
     * }
     */

    public DriverStatus getDriverStatus(UUID driverId) {

        return driverActivityRepository.getStatus(driverId);

    }

}
