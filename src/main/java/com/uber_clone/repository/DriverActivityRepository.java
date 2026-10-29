package com.uber_clone.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uber_clone.model.DriverActivity;
import com.uber_clone.model.DriverActivity.DriverStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DriverActivityRepository {

    private final JdbcTemplate jdbcTemplate;

    public DriverStatus getStatus(UUID driverId) {
        String sql = """
                    SELECT driver_status FROM driver_activity WHERE driver_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, DriverStatus.class, driverId);

    }

    public DriverActivity updateStatus(UUID driverId, DriverActivity.DriverStatus status) {

        String sql = """
                    UPDATE driver_activity
                    SET driver_status = ?, updated_at = NOW()
                    WHERE driver_id = ?
                    RETURNING driver_id, driver_status, updated_at
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            DriverActivity driverActivity = new DriverActivity();

            driverActivity.setDriverId(rs.getObject("driver_id", UUID.class));
            driverActivity.setDriverStatus(DriverActivity.DriverStatus.valueOf((rs.getString("driver_status"))));
            driverActivity.setUpdatedAt((rs.getObject("updated_at", OffsetDateTime.class)).toInstant());

            return driverActivity;

        }, status.name(), driverId);

    }

}