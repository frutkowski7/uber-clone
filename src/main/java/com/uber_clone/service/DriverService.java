package com.uber_clone.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.uber_clone.exceptions.DriverAlreadyExistsException;
import com.uber_clone.exceptions.UserNotFoundException;
import com.uber_clone.model.DriverActivity.DriverStatus;
import com.uber_clone.repository.DriverRepository;
import com.uber_clone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {

    // sprawdź najpierw czy użytkownik jest faktycznie tego uuid
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public String addDriver(String email) {

        UUID userId = userRepository.getIdByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (driverRepository.existsById(userId)) {
            throw new DriverAlreadyExistsException("Driver already exists!");
        }
        driverRepository.addDriver(userId, DriverStatus.OFFLINE);

        return "New driver created!";
    }

}
