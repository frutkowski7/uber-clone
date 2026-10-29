package com.uber_clone.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uber_clone.model.Driver;
import com.uber_clone.model.DriverActivity.DriverStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class DriverRepository {

    private final JdbcTemplate jdbcTemplate;

    public int addDriver(UUID driverId, DriverStatus driverStatus) {
        String driverQuery = """
                    INSERT INTO drivers VALUES (?)
                """;
        String driverActivityQuery = """
                    INSERT INTO driver_activity (driver_id, driver_status)
                    VALUES (?, ?)
                """;

        jdbcTemplate.update(driverQuery, driverId);
        return jdbcTemplate.update(driverActivityQuery, driverId, driverStatus.name());

    }

    public boolean isDriver(UUID userId) {
        String sql = """
                    SELECT EXISTS (SELECT 1 FROM drivers WHERE driver_id = ?)
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, userId);

        return Boolean.TRUE.equals(exists);

    }

    public boolean existsById(UUID driverId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM drivers WHERE driver_id = ?)";

        // .equals (porównuje Boolean.TRUE i wartość w środku)
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, driverId));

    }

    public List<Driver> getDrivers() {
        String sql = """
                    SELECT * FROM drivers;
                """;
        // rs - obecnie przetwarzany wiersz
        // rowNum - pozycja tego danego rekordu

        // jbdcTemplate.query zwraca liste
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Driver driver = new Driver(rs.getObject("driver_id", UUID.class));
            return driver;

        });

    }

}
