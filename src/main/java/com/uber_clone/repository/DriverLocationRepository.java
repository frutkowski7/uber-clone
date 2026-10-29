package com.uber_clone.repository;

import com.uber_clone.dto.DriverLocation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DriverLocationRepository {

    // Tutaj musi być Concurrent zamiast zwykłego HashMap
    private final Map<UUID, DriverLocation> driverLocations = new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> h3Index = new ConcurrentHashMap<>();

    public DriverLocation getDriverLocation(UUID driverId) {
        return driverLocations.get(driverId);

    }

    public List<DriverLocation> getDriversLocations() {

        List<DriverLocation> driversLocationsList = new ArrayList<>();

        driverLocations.forEach((driverId, location) -> {
            driversLocationsList.add(location);
        });

        return driversLocationsList;

    }

    public void updateLocation(DriverLocation location) {

        UUID driverId = location.driverId();
        driverLocations.put(driverId, location);
        h3Index.computeIfAbsent(location.h3Address(), k -> ConcurrentHashMap.newKeySet())
                .add(driverId);

    }

    // recordy uznawane są .equals() true
    // porównując dane wewnątrz rekordu
    // natomiast klasa jak wiadomo, jest
    // porównywana po referencji więc
    // dwie instancje są różne bo odwołują się
    // do innej referencji

    public List<DriverLocation> findAvailableDriversInCells(List<String> h3Cells) {
        return h3Cells.stream().map((cell) -> h3Index.get(cell)).filter(set -> set != null).flatMap(set -> set.stream())
                .map(driverId -> driverLocations.get(driverId)).toList();
    }

    /**
     * Zwraca liczbę wolnych kierowców w pojedynczej komórce H3 (potrzebne do
     * wyliczania Surge Pricing).
     */
    public long countAvailableDriversInCell(String h3Address) {

        // pobieram kierowców w danym h3
        Set<UUID> driverIds = h3Index.get(h3Address);

        if (driverIds == null || driverIds.isEmpty()) {
            return 0;
        }

        // SET i LISTA
        // Map i hashMap - key:value pairs

        // zliczam kierowców dla danego h3
        return driverIds.stream()
                // .get() wyciąga daną wartość z listy czy hashMap
                .map(driverId -> driverLocations.get(driverId))
                .filter(obj -> obj != null).filter(loc -> loc.isAvailable())
                .count();
    }

}
