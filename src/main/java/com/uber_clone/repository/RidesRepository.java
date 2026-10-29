package com.uber_clone.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.uber_clone.model.Ride;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RidesRepository {

    private final JdbcTemplate jdbcTemplate;

    public UUID addNewRide(Ride ride) {

        String sql = """
                    INSERT INTO rides
                    (client_id, status, pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude, price)
                    VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                """;

        return jdbcTemplate.queryForObject(sql, UUID.class, ride.getClientId(), ride.getStatus().name(),
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getDropoffLatitude(), ride.getDropoffLongitude(), ride.getPrice());

    }

    public Optional<Ride> acceptRide(UUID rideId, UUID driverId) {
        String sql = """
                    UPDATE rides
                    SET status = 'ACCEPTED',
                        driver_id = ?
                    WHERE id = ? AND status = 'REQUESTED'
                    RETURNING *
                """;
        try {

            Ride updatedRide = jdbcTemplate.queryForObject(sql, rideRowMapper, driverId, rideId);
            // pakujemy updatedRide w Optional w celu obsługi braku update
            // jdbc zwraca wątek
            return Optional.ofNullable(updatedRide);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    public Optional<Ride> startRide(UUID rideId, UUID driverId) {
        String sql = """
                    UPDATE rides SET status = 'IN_PROGRESS' WHERE id = ? AND driver_id = ? AND status = 'ACCEPTED' RETURNING *
                """;

        try {
            Ride updatedRide = jdbcTemplate.queryForObject(sql, rideRowMapper,
                    rideId, driverId);

            return Optional.ofNullable(updatedRide);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    public Optional<Ride> finishRide(UUID rideId, UUID driverId) {
        String sql = """
                    UPDATE rides
                    SET status = 'COMPLETED'
                    WHERE id = ? AND driver_id = ? AND status = 'IN_PROGRESS'
                    RETURNING *
                """;

        try {
            Ride updatedRide = jdbcTemplate.queryForObject(sql, rideRowMapper,
                    rideId, driverId);

            return Optional.ofNullable(updatedRide);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    public Optional<Ride> cancelRide(UUID rideId, UUID userId) {
        String sql = """
                UPDATE rides
                SET status = 'CANCELLED',
                    cancelled_by = ?
                WHERE id = ?
                  AND (client_id = ? OR driver_id = ?)
                  AND status IN ('REQUESTED', 'ACCEPTED')
                RETURNING *
                """;

        try {
            Ride updatedRide = jdbcTemplate.queryForObject(sql, rideRowMapper, userId, rideId, userId, userId);
            return Optional.ofNullable(updatedRide);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    // W RidesRepository.java
    public boolean existsById(UUID rideId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM rides WHERE id = ?)";
        // queryForObject automatycznie zamieni wynik na Boolean
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, rideId);

        // chroni przed zwracaniem null
        // exists to Boolean, więc przy unboxingu
        // java może go potraktowac jako null
        // teraz pórównuje go do wartosci TRUE
        // wiec w przypadku exists = null zwracam false
        return Boolean.TRUE.equals(exists);
    }

    public List<Ride> getRequestedRidesByClient(UUID clientId) {

        String sql = """
                SELECT id, pickup_latitude, pickup_longitude,
                       dropoff_latitude, dropoff_longitude, status, price, created_at
                FROM rides
                WHERE status = 'REQUESTED' AND client_id = ?
                """;

        // 1. Dodajemy 'return' przed jdbcTemplate
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ride ride = new Ride();

            ride.setId(rs.getObject("id", UUID.class));
            ride.setPickupLatitude(rs.getDouble("pickup_latitude"));
            ride.setPickupLongitude(rs.getDouble("pickup_longitude"));
            ride.setDropoffLatitude(rs.getDouble("dropoff_latitude"));
            ride.setDropoffLongitude(rs.getDouble("dropoff_longitude"));
            ride.setPrice(rs.getBigDecimal("price"));
            ride.setStatus(Ride.Status.valueOf(rs.getString("status")));

            return ride;
        }, clientId);
    }

    // Requested dla driver
    public List<Ride> getRequestedRides() {

        String sql = """
                SELECT id, client_id, pickup_latitude, pickup_longitude,
                       dropoff_latitude, dropoff_longitude, status, price, created_at
                FROM rides
                WHERE status = 'REQUESTED'
                """;

        // 1. Dodajemy 'return' przed jdbcTemplate
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ride ride = new Ride();

            ride.setId(rs.getObject("id", UUID.class));
            ride.setClientId(rs.getObject("client_id", UUID.class));
            ride.setPickupLatitude(rs.getDouble("pickup_latitude"));
            ride.setPickupLongitude(rs.getDouble("pickup_longitude"));
            ride.setDropoffLatitude(rs.getDouble("dropoff_latitude"));
            ride.setDropoffLongitude(rs.getDouble("dropoff_longitude"));
            ride.setPrice(rs.getBigDecimal("price"));
            ride.setStatus(Ride.Status.valueOf(rs.getString("status")));

            return ride;
        });
    }

    public List<Ride> findHistoryByClientId(UUID clientId, int limit, int offset) {

        String sql = """
                SELECT * FROM rides WHERE client_id = ? AND (status = 'COMPLETED' OR status = 'CANCELLED')
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        // 1. Dodajemy 'return' przed jdbcTemplate
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ride ride = new Ride();

            ride.setId(rs.getObject("id", UUID.class));
            ride.setClientId(rs.getObject("client_id", UUID.class));
            ride.setPickupLatitude(rs.getDouble("pickup_latitude"));
            ride.setPickupLongitude(rs.getDouble("pickup_longitude"));
            ride.setDropoffLatitude(rs.getDouble("dropoff_latitude"));
            ride.setDropoffLongitude(rs.getDouble("dropoff_longitude"));
            ride.setPrice(rs.getBigDecimal("price"));
            ride.setStatus(Ride.Status.valueOf(rs.getString("status")));

            return ride;
        }, clientId, limit, offset);

    }

    public List<Ride> findHistoryByDriverId(UUID driverId, int limit, int offset) {

        String sql = """
                SELECT * FROM rides WHERE driver_id = ? AND (status = 'COMPLETED' OR status = 'CANCELLED')
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        // 1. Dodajemy 'return' przed jdbcTemplate
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ride ride = new Ride();

            ride.setId(rs.getObject("id", UUID.class));
            ride.setClientId(rs.getObject("client_id", UUID.class));
            ride.setPickupLatitude(rs.getDouble("pickup_latitude"));
            ride.setPickupLongitude(rs.getDouble("pickup_longitude"));
            ride.setDropoffLatitude(rs.getDouble("dropoff_latitude"));
            ride.setDropoffLongitude(rs.getDouble("dropoff_longitude"));
            ride.setPrice(rs.getBigDecimal("price"));
            ride.setStatus(Ride.Status.valueOf(rs.getString("status")));

            return ride;
        }, driverId, limit, offset);

    }

    private final RowMapper<Ride> rideRowMapper = (rs, rowNum) -> {
        Ride ride = new Ride();

        ride.setId(rs.getObject("id", UUID.class));
        ride.setClientId(rs.getObject("client_id", UUID.class));
        ride.setDriverId(rs.getObject("driver_id", UUID.class));
        ride.setPickupLatitude(rs.getDouble("pickup_latitude"));
        ride.setPickupLongitude(rs.getDouble("pickup_longitude"));
        ride.setDropoffLatitude(rs.getDouble("dropoff_latitude"));
        ride.setDropoffLongitude(rs.getDouble("dropoff_longitude"));
        ride.setStatus(Ride.Status.valueOf(rs.getString("status")));
        ride.setPrice(rs.getBigDecimal("price"));
        ride.setCreatedAt(rs.getTimestamp("created_at").toInstant());

        return ride;

    };

}
