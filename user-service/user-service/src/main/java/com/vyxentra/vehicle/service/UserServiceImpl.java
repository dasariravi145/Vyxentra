package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.UpdateProfileRequest;
import com.vyxentra.vehicle.dto.response.AddressResponse;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;
import com.vyxentra.vehicle.dto.response.VehicleResponse;
import com.vyxentra.vehicle.entity.Address;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.entity.Vehicle;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.UserEventProducer;
import com.vyxentra.vehicle.mapper.AddressMapper;
import com.vyxentra.vehicle.mapper.UserMapper;
import com.vyxentra.vehicle.mapper.VehicleMapper;
import com.vyxentra.vehicle.repository.AddressRepository;
import com.vyxentra.vehicle.repository.UserRepository;
import com.vyxentra.vehicle.repository.VehicleRepository;
import com.vyxentra.vehicle.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final VehicleRepository vehicleRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final VehicleMapper vehicleMapper;
    private final UserValidator userValidator;
    private final UserEventProducer eventProducer;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        log.debug("Fetching profile for user: {}", userId);

        User user = findUserById(userId);

        // Fetch addresses
        List<Address> addresses = addressRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        List<AddressResponse> addressResponses = userMapper.toAddressResponseList(addresses);

        // Fetch vehicles
        List<Vehicle> vehicles = vehicleRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        List<VehicleResponse> vehicleResponses = userMapper.toVehicleResponseList(vehicles);

        // Find default address and vehicle
        AddressResponse defaultAddress = addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .map(addressMapper::toResponse)
                .orElse(null);

        VehicleResponse defaultVehicle = vehicles.stream()
                .filter(Vehicle::isDefault)
                .findFirst()
                .map(vehicleMapper::toResponse)
                .orElse(null);

        return userMapper.toDetailedProfileResponse(
                user, addressResponses, vehicleResponses, defaultAddress, defaultVehicle);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = findUserById(userId);

        // Validate email uniqueness if changed
        if (!user.getEmail().equals(request.getEmail())) {
            userValidator.validateEmailUniqueness(request.getEmail(), userId);
        }

        // Update fields
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setAlternatePhone(request.getAlternatePhone());
        user.setProfilePicture(request.getProfilePicture());

        // Update preferences
        if (request.getSmsNotifications() != null) {
            user.setSmsNotifications(request.getSmsNotifications());
        }
        if (request.getEmailNotifications() != null) {
            user.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            user.setPushNotifications(request.getPushNotifications());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }

        user = userRepository.save(user);

        // Publish event
        eventProducer.publishUserProfileUpdated(userId, user.getEmail(), user.getFullName());

        log.info("Profile updated successfully for user: {}", userId);

        return getUserProfile(userId);
    }

    @Override
    @Transactional
    public void deactivateAccount(String userId) {
        log.info("Deactivating account for user: {}", userId);

        User user = findUserById(userId);

        // Check if user has active bookings (should be checked via booking service)
        // For now, proceed with deactivation

        user.setActive(false);
        userRepository.save(user);

        // Publish event
        eventProducer.publishUserDeactivated(userId);

        log.info("Account deactivated successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void incrementBookingCount(String userId) {
        userRepository.incrementBookingCount(userId);
    }

    @Override
    @Transactional
    public void updateTotalSpent(String userId, Integer amount) {
        userRepository.addToTotalSpent(userId, amount);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}