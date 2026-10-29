
package com.uber_clone.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FareCalculator {

    @Value("${uber.fare.base:10.00}")
    private BigDecimal baseFare;

    @Value("${uber.fare.per-km:3.50}")
    private BigDecimal perKmRate;

    @Value("${uber.fare.minimum:15.00}")
    private BigDecimal minimumPrice;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public BigDecimal calculatePrice(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = calculateDistanceInKm(lat1, lon1, lat2, lon2);

        BigDecimal distanceCost = perKmRate.multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal totalPrice = baseFare.add(distanceCost).setScale(2, RoundingMode.HALF_UP);

        return totalPrice.max(minimumPrice);
    }

    // Haversine liczy dystans między dwoma punktami GPS
    public double calculateDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        // różnica destynacja - start
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // 2. Zamiana samych szerokości geograficznych na radiany dla funkcji cos()
        double radLat1 = Math.toRadians(lat1); // <-- WAŻNE
        double radLat2 = Math.toRadians(lat2); // <-- WAŻNE

        // 3. Obliczenie 'a'
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // 4. Obliczenie kąta środkowego 'c'
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 5. Powrót do kilometrów
        return EARTH_RADIUS_KM * c;
    }
}