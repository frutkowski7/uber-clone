package com.uber_clone.dto;

import java.time.Instant;
import java.util.UUID;

/* 

specjalny typ klasy przeznaczony wyłącznie do 
przechowywania niemutowalnych danych (Data Transfer Object)

*/

public record DriverLocation(
                UUID driverId,
                double latitude,
                double longitude,
                String h3Address,
                boolean isAvailable,
                Instant lastUpdated) {
}