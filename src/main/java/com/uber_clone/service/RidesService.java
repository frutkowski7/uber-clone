package com.uber_clone.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uber_clone.Request.CreateRideRequest;
import com.uber_clone.Response.AcceptRideResponse;
import com.uber_clone.Response.CreateRideResponse;
import com.uber_clone.Response.RideResponse;
import com.uber_clone.dto.PointDto;
import com.uber_clone.event.DriverStatusChangedEvent;
import com.uber_clone.exceptions.RideNotFoundException;
import com.uber_clone.exceptions.UserNotFoundException;
import com.uber_clone.model.DriverActivity.DriverStatus;
import com.uber_clone.model.Ride;
import com.uber_clone.repository.DriverRepository;
import com.uber_clone.repository.RidesRepository;
import com.uber_clone.repository.UserRepository;
import com.uber_clone.util.FareCalculator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RidesService {

        private final RidesRepository ridesRepository;
        private final DriverRepository driverRepository;
        private final OsmrRoutingService osmrRoutingService;
        private final UserRepository userRepository;
        private final FareCalculator fareCalculator;

        // Zamiast wstrzykiwać DriverLocationService i DriverActivityService:
        private final ApplicationEventPublisher eventPublisher;

        public CreateRideResponse addNewRide(CreateRideRequest request, String userEmail) {

                UUID clientId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UserNotFoundException("Client not found!"));

                com.uber_clone.dto.OsmrResponse route = osmrRoutingService.getRoute(
                                request.getPickupLongitude(),
                                request.getPickupLatitude(),
                                request.getDropoffLongitude(), request.getDropoffLatitude());

                List<PointDto> coordinates = route.routes().get(0).geometry().coordinates().stream()
                                .map(c -> new PointDto(c.get(1), c.get(0))).toList();

                Ride ride = new Ride();
                ride.setClientId(clientId);
                ride.setPickupLatitude(request.getPickupLatitude());
                ride.setPickupLongitude(request.getPickupLongitude());
                ride.setDropoffLatitude(request.getDropoffLatitude());
                ride.setDropoffLongitude(request.getDropoffLongitude());
                ride.setCoordinates(coordinates);
                ride.setStatus(Ride.Status.REQUESTED);

                ride.setPrice(fareCalculator.calculatePrice(
                                request.getPickupLatitude(),
                                request.getPickupLongitude(),
                                request.getDropoffLatitude(),
                                request.getDropoffLongitude()));

                UUID generatedId = ridesRepository.addNewRide(ride);
                ride.setId(generatedId);

                return new CreateRideResponse(ride.getId(), ride.getStatus(), ride.getPrice(), ride.getCoordinates());

        }

        @Transactional
        public AcceptRideResponse acceptRide(UUID rideId, String userEmail) {
                UUID driverId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UserNotFoundException("Driver not found"));

                /*
                 * if (driverActivity.getDriverStatus().equals(DriverStatus.ON_TRIP)) {
                 * throw new RideNotFoundException("Driver already on trip!");
                 * 
                 * }
                 */

                Ride updatedRide = ridesRepository.acceptRide(rideId, driverId)
                                .orElseThrow(() -> new RideNotFoundException("Ride not found or already accepted!"));

                eventPublisher.publishEvent(new DriverStatusChangedEvent(driverId, DriverStatus.ON_TRIP, false));

                return mapToAcceptRideResponse(updatedRide);
        }

        @Transactional
        public RideResponse startRide(UUID rideId, String userEmail) {
                UUID userId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UserNotFoundException("User not found!"));

                Ride updatedRide = ridesRepository.startRide(rideId, userId)
                                .orElseThrow(() -> new RideNotFoundException("Ride not found!"));

                return mapToRideResponse(updatedRide);
        }

        @Transactional
        public RideResponse finishRide(UUID rideId, String userEmail) {
                UUID driverId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UserNotFoundException("User not found!"));

                Ride updatedRide = ridesRepository.finishRide(rideId, driverId)
                                .orElseThrow(() -> new RideNotFoundException("Ride not found!"));

                eventPublisher.publishEvent(new DriverStatusChangedEvent(driverId, DriverStatus.AVAILABLE, true));

                return mapToRideResponse(updatedRide);
        }

        @Transactional
        public RideResponse cancelRide(UUID rideId, String userEmail) {
                UUID userId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

                Ride updatedRide = ridesRepository.cancelRide(rideId, userId)
                                .orElseThrow(() -> new RideNotFoundException("Ride not found or cannot be cancelled!"));

                UUID driverId = updatedRide.getDriverId();

                // Jeśli kierowca był przypisany, odblokowujemy go zdarzeniem
                if (driverId != null) {

                        eventPublisher.publishEvent(
                                        new DriverStatusChangedEvent(driverId, DriverStatus.AVAILABLE, true));
                }

                return mapToRideResponse(updatedRide);
        }

        @Transactional(readOnly = true)
        public List<RideResponse> getUserRideHistory(String userEmail, int page, int size) {
                UUID userId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

                boolean isDriver = driverRepository.isDriver(userId);
                int offset = page * size;

                List<Ride> rides = isDriver
                                ? ridesRepository.findHistoryByDriverId(userId, size, offset)
                                : ridesRepository.findHistoryByClientId(userId, size, offset);

                return rides.stream()
                                .map(this::mapToRideResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<RideResponse> getRequestedRidesByClient(String userEmail) {
                UUID clientId = userRepository.getIdByEmail(userEmail)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                return ridesRepository.getRequestedRidesByClient(clientId)
                                .stream()
                                .map(this::mapToRideResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<RideResponse> getRequestedRides() {
                return ridesRepository.getRequestedRides()
                                .stream()
                                .map(this::mapToRideResponse)
                                .toList();
        }

        private RideResponse mapToRideResponse(Ride ride) {
                return new RideResponse(
                                ride.getId(),
                                ride.getClientId(),
                                ride.getPickupLatitude(),
                                ride.getPickupLongitude(),
                                ride.getDropoffLatitude(),
                                ride.getDropoffLongitude(),
                                ride.getStatus(),
                                ride.getPrice(),
                                ride.getCreatedAt());
        }

        private AcceptRideResponse mapToAcceptRideResponse(Ride ride) {
                return new AcceptRideResponse(
                                ride.getId(),
                                ride.getClientId(),
                                ride.getPickupLatitude(),
                                ride.getPickupLongitude(),
                                ride.getDropoffLatitude(),
                                ride.getDropoffLongitude(),
                                ride.getStatus(),
                                ride.getPrice(),
                                ride.getCreatedAt(),
                                ride.getCoordinates());
        }
}