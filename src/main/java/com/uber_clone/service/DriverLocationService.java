package com.uber_clone.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import com.uber_clone.dto.DriverLocation;
import com.uber_clone.event.DriverStatusChangedEvent;
import com.uber_clone.exceptions.UserNotFoundException;
import com.uber_clone.model.DriverActivity.DriverStatus;
import com.uber_clone.repository.DriverActivityRepository;
import com.uber_clone.repository.DriverLocationRepository;
import com.uber_clone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final H3Service h3Service;
    private final DriverLocationRepository driverLocationRepository;
    private final UserRepository userRepository;
    private final DriverActivityRepository driverActivityRepository;

    // oddzielam updateDriverLocation i updateDriverStatus
    // w pierwszym sygnale GPS jeżeli kierowca w db jest = available i nie istnieje
    // w pamięci RAM, to wtedy ustawiam isAvailable czyli wywołuje
    // updateDriverStatus

    // in-memory state - state w pamięci operacyjnej

    public List<DriverLocation> getDriversLocations() {

        return driverLocationRepository.getDriversLocations();

    }

    @EventListener
    public void handleDriverStatusChanged(DriverStatusChangedEvent event) {
        // Gdy RidesService wyemituje event (isAvailableInRam = false),
        // ten listener odbiera go i aktualizuje stan w ConcurrentHashMap!
        updateDriverStatus(event.driverId(), event.isAvailable());
    }

    public DriverLocation updateDriverLocation(String email, double lat, double lng) {

        // pozbycie się db, tylko jeżeli nie sitnieje

        UUID driverId = userRepository.getIdByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        // res 8 - ok. 460 metrów
        String h3Address = h3Service.geoToH3Adress(lat, lng, 8);

        // Sprawdzam czy kierowca widnieje w in-memory state
        DriverLocation existingDriverLocation = driverLocationRepository.getDriverLocation(driverId);

        // jeżeli kierowca nie istnieje w in-memory
        if (existingDriverLocation == null) {

            // sprawdzam jego status w database
            DriverStatus driverDatabaseStatus = driverActivityRepository.getStatus(driverId);

            if (driverDatabaseStatus == DriverStatus.ON_TRIP) {
                DriverLocation driverLocation = new DriverLocation(
                        driverId,
                        lat,
                        lng,
                        h3Address,
                        false,
                        Instant.now());
                driverLocationRepository.updateLocation(driverLocation);
                return driverLocation;

            }
            if (driverDatabaseStatus == DriverStatus.OFFLINE) {
                // eventem updatujemy stan w database na available
                driverActivityRepository.updateStatus(driverId, DriverStatus.AVAILABLE);
            }

            DriverLocation location = new DriverLocation(driverId, lat, lng, h3Address, true, Instant.now());
            driverLocationRepository.updateLocation(location);
            return location;

        }

        DriverLocation driverLocation = new DriverLocation(
                driverId,
                lat,
                lng,
                h3Address,
                existingDriverLocation.isAvailable(),
                Instant.now());

        driverLocationRepository.updateLocation(driverLocation);
        return driverLocation;

    }

    // String email jest na teraz - później będzie websocket
    public void updateDriverStatus(UUID driverId, boolean isAvailable) {

        DriverLocation existingLocation = driverLocationRepository.getDriverLocation(driverId);

        if (existingLocation == null) {
            return;

        }

        DriverLocation driverLocation = new DriverLocation(
                existingLocation.driverId(),
                existingLocation.latitude(),
                existingLocation.longitude(),
                existingLocation.h3Address(),
                isAvailable,
                Instant.now());

        driverLocationRepository.updateLocation(driverLocation);

    }

    public DriverLocation getDriverLocation(UUID driverId) {
        return driverLocationRepository.getDriverLocation(driverId);

    }

    public List<DriverLocation> findNearbyAvailableDrivers(double lat, double lng, int k) {
        String h3Address = h3Service.geoToH3Adress(lat, lng, 8);
        List<String> cells = h3Service.getKRingNeighbors(h3Address, k);
        List<DriverLocation> availableDrivers = driverLocationRepository.findAvailableDriversInCells(cells);
        System.out.println(availableDrivers);
        return availableDrivers;
    }

    public long countDriversInCell(double lat, double lng) {
        String h3Address = h3Service.geoToH3Adress(lat, lng, 8);

        return driverLocationRepository.countAvailableDriversInCell(h3Address);

    }

}
