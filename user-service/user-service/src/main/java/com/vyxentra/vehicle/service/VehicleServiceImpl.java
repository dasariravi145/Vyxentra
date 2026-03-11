package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.AddVehicleRequest;
import com.vyxentra.vehicle.dto.request.UpdateVehicleRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.VehicleResponse;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.entity.Vehicle;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.UserEventProducer;
import com.vyxentra.vehicle.mapper.VehicleMapper;
import com.vyxentra.vehicle.repository.UserRepository;
import com.vyxentra.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleMapper vehicleMapper;
    private final UserEventProducer eventProducer;

    private static final Pattern REGISTRATION_PATTERN =
            Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$");
    private static final int MAX_VEHICLES_PER_USER = 5;

    @Override
    @Transactional
    @CacheEvict(value = "userVehicles", key = "#userId")
    public VehicleResponse addVehicle(String userId, AddVehicleRequest request) {
        log.info("Adding vehicle for user: {}", userId);

        // Validate user exists
        User user = findUserById(userId);

        // Validate registration number format
        validateRegistrationNumber(request.getRegistrationNumber());

        // Check if registration number already exists
        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Vehicle with registration number " + request.getRegistrationNumber() + " already exists");
        }

        // Check vehicle limit
        long vehicleCount = vehicleRepository.countByUserId(userId);
        if (vehicleCount >= MAX_VEHICLES_PER_USER) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Maximum " + MAX_VEHICLES_PER_USER + " vehicles allowed per user");
        }

        // Validate vehicle type
        validateVehicleType(request.getVehicleType());

        // If this is set as default or this is the first vehicle, reset other defaults
        if (request.isDefault() || vehicleCount == 0) {
            vehicleRepository.resetDefaultVehicles(userId);
            request.setDefault(true);
        }

        // Create and save vehicle
        Vehicle vehicle = Vehicle.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .registrationNumber(request.getRegistrationNumber().toUpperCase())
                .color(request.getColor())
                .isDefault(request.isDefault())
                .fuelType(request.getFuelType())
                .transmissionType(request.getTransmissionType())
                .engineCapacity(request.getEngineCapacity())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        vehicle = vehicleRepository.save(vehicle);

        // Update user's default vehicle if needed
        if (vehicle.isDefault()) {
            user.setDefaultVehicleId(vehicle.getId());
            userRepository.save(user);
        }

        // Publish Kafka event
        eventProducer.publishVehicleAdded(userId, vehicle.getId());

        log.info("Vehicle added successfully with ID: {}", vehicle.getId());

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userVehicles", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null")
    public PageResponse<VehicleResponse> getUserVehicles(String userId, Pageable pageable) {
        log.debug("Fetching vehicles for user: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Page<Vehicle> page = vehicleRepository.findByUserId(userId, pageable);

        List<VehicleResponse> content = page.getContent().stream()
                .map(vehicleMapper::toResponse)
                .toList();

        return PageResponse.<VehicleResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicle", key = "#vehicleId", unless = "#result == null")
    public VehicleResponse getVehicle(String userId, String vehicleId) {
        log.debug("Fetching vehicle {} for user: {}", vehicleId, userId);

        Vehicle vehicle = findVehicleByIdAndUserId(vehicleId, userId);
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"vehicle", "userVehicles"}, allEntries = true)
    public VehicleResponse updateVehicle(String userId, String vehicleId, UpdateVehicleRequest request) {
        log.info("Updating vehicle {} for user: {}", vehicleId, userId);

        Vehicle vehicle = findVehicleByIdAndUserId(vehicleId, userId);
        User user = vehicle.getUser();

        // Track changes for audit
        boolean defaultChanged = false;

        // Update fields if provided
        if (request.getMake() != null && !request.getMake().isEmpty()) {
            vehicle.setMake(request.getMake());
        }

        if (request.getModel() != null && !request.getModel().isEmpty()) {
            vehicle.setModel(request.getModel());
        }

        if (request.getYear() != null && !request.getYear().isEmpty()) {
            validateYear(request.getYear());
            vehicle.setYear(request.getYear());
        }

        if (request.getColor() != null && !request.getColor().isEmpty()) {
            vehicle.setColor(request.getColor());
        }

        if (request.getFuelType() != null && !request.getFuelType().isEmpty()) {
            vehicle.setFuelType(request.getFuelType());
        }

        if (request.getTransmissionType() != null && !request.getTransmissionType().isEmpty()) {
            vehicle.setTransmissionType(request.getTransmissionType());
        }

        if (request.getEngineCapacity() != null) {
            vehicle.setEngineCapacity(request.getEngineCapacity());
        }

        // Handle default flag if provided
        if (request.getIsDefault() != null) {
            if (request.getIsDefault() && !vehicle.isDefault()) {
                // Setting as default
                vehicleRepository.resetDefaultVehicles(userId);
                vehicle.setDefault(true);
                user.setDefaultVehicleId(vehicle.getId());
                defaultChanged = true;
            } else if (!request.getIsDefault() && vehicle.isDefault()) {
                // Cannot unset default if it's the only vehicle
                long vehicleCount = vehicleRepository.countByUserId(userId);
                if (vehicleCount == 1) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Cannot unset default on the only vehicle");
                }
                vehicle.setDefault(false);
                // Find another vehicle to set as default
                vehicleRepository.findFirstByUserIdAndIdNot(userId, vehicleId)
                        .ifPresent(otherVehicle -> {
                            otherVehicle.setDefault(true);
                            vehicleRepository.save(otherVehicle);
                            user.setDefaultVehicleId(otherVehicle.getId());
                        });
            }
        }

        vehicle.setUpdatedAt(Instant.now());
        vehicle = vehicleRepository.save(vehicle);

        if (defaultChanged) {
            userRepository.save(user);
        }

        // Publish Kafka event
        eventProducer.publishVehicleUpdated(userId, vehicleId);

        log.info("Vehicle updated successfully: {}", vehicleId);

        return vehicleMapper.toResponse(vehicle);
    }


    @Override
    @Transactional
    @CacheEvict(value = {"vehicle", "userVehicles"}, allEntries = true)
    public void deleteVehicle(String userId, String vehicleId) {
        log.info("Deleting vehicle {} for user: {}", vehicleId, userId);

        Vehicle vehicle = findVehicleByIdAndUserId(vehicleId, userId);
        User user = vehicle.getUser();

        // Cannot delete default vehicle unless it's the only vehicle
        if (vehicle.isDefault()) {
            long vehicleCount = vehicleRepository.countByUserId(userId);
            if (vehicleCount > 1) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Cannot delete default vehicle. Set another vehicle as default first.");
            }
        }

        // Delete the vehicle
        vehicleRepository.delete(vehicle);

        // If this was the default vehicle and there are other vehicles, make another one default
        if (vehicle.isDefault()) {
            vehicleRepository.findByUserId(userId, Pageable.ofSize(1))
                    .stream()
                    .findFirst()
                    .ifPresent(v -> {
                        v.setDefault(true);
                        vehicleRepository.save(v);
                        user.setDefaultVehicleId(v.getId());
                        userRepository.save(user);
                    });
        } else if (vehicleId.equals(user.getDefaultVehicleId())) {
            // Clear default vehicle ID if it was pointing to deleted vehicle
            user.setDefaultVehicleId(null);
            userRepository.save(user);
        }

        // Publish Kafka event
        eventProducer.publishVehicleDeleted(userId, vehicleId);

        log.info("Vehicle deleted successfully: {}", vehicleId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"vehicle", "userVehicles"}, allEntries = true)
    public void setDefaultVehicle(String userId, String vehicleId) {
        log.info("Setting default vehicle {} for user: {}", vehicleId, userId);

        Vehicle vehicle = findVehicleByIdAndUserId(vehicleId, userId);
        User user = vehicle.getUser();

        if (vehicle.isDefault()) {
            log.debug("Vehicle {} is already default", vehicleId);
            return;
        }

        // Reset all vehicles to non-default
        vehicleRepository.resetDefaultVehicles(userId);

        // Set this vehicle as default
        vehicle.setDefault(true);
        vehicle.setUpdatedAt(Instant.now());
        vehicleRepository.save(vehicle);

        // Update user's default vehicle ID
        user.setDefaultVehicleId(vehicleId);
        userRepository.save(user);

        // Publish Kafka event
        eventProducer.publishDefaultVehicleChanged(userId, vehicleId);

        log.info("Default vehicle set successfully: {}", vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkRegistrationNumberExists(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            return false;
        }
        return vehicleRepository.existsByRegistrationNumber(registrationNumber.toUpperCase());
    }

    // Private helper methods

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Vehicle findVehicleByIdAndUserId(String vehicleId, String userId) {
        return vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
    }

    private void validateRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Registration number is required");
        }

        if (!REGISTRATION_PATTERN.matcher(registrationNumber.toUpperCase()).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Invalid registration number format. Expected format: KA01AB1234");
        }
    }

    private void validateVehicleType(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vehicle type is required");
        }
    }

    private void validateYear(String year) {
        if (year == null) return;

        if (!year.matches("^[0-9]{4}$")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Year must be a 4-digit number");
        }

        int yearInt = Integer.parseInt(year);
        int currentYear = java.time.Year.now().getValue();

        if (yearInt < 1900 || yearInt > currentYear + 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Year must be between 1900 and " + (currentYear + 1));
        }
    }
}