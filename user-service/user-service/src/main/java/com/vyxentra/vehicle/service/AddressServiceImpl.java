package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.AddAddressRequest;
import com.vyxentra.vehicle.dto.request.UpdateAddressRequest;
import com.vyxentra.vehicle.dto.response.AddressResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.entity.Address;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.UserEventProducer;
import com.vyxentra.vehicle.mapper.AddressMapper;
import com.vyxentra.vehicle.repository.AddressRepository;
import com.vyxentra.vehicle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final UserEventProducer eventProducer;

    @Override
    @Transactional
    public AddressResponse addAddress(String userId, AddAddressRequest request) {
        log.info("Adding address for user: {}", userId);

        User user = findUserById(userId);

        // Check address limit (max 10 addresses per user)
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount >= 10) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Maximum 10 addresses allowed per user");
        }

        // If this is set as default or this is the first address, reset other defaults
        if (request.isDefault() || addressCount == 0) {
            addressRepository.resetDefaultAddresses(userId);
            request.setDefault(true);
        }

        Address address = addressMapper.toEntity(request, user);
        address = addressRepository.save(address);

        // Publish event
        eventProducer.publishAddressAdded(userId, address.getId());

        log.info("Address added successfully with ID: {}", address.getId());

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AddressResponse> getUserAddresses(String userId, Pageable pageable) {
        log.debug("Fetching addresses for user: {}", userId);

        Page<Address> page = addressRepository.findByUserId(userId, pageable);

        Page<AddressResponse> responsePage = page.map(addressMapper::toResponse);

        return PageResponse.<AddressResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(String userId, String addressId) {
        log.debug("Fetching address {} for user: {}", addressId, userId);

        Address address = findAddressByIdAndUserId(addressId, userId);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, UpdateAddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);

        Address address = findAddressByIdAndUserId(addressId, userId);

        addressMapper.updateEntity(address, request);
        address = addressRepository.save(address);

        // Publish event
        eventProducer.publishAddressUpdated(userId, addressId);

        log.info("Address updated successfully: {}", addressId);

        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, String addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);

        Address address = findAddressByIdAndUserId(addressId, userId);

        // Cannot delete default address unless it's the only address
        if (address.isDefault()) {
            long addressCount = addressRepository.countByUserId(userId);
            if (addressCount > 1) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Cannot delete default address. Set another address as default first.");
            }
        }

        addressRepository.delete(address);

        // If this was the default address and there are other addresses, make another one default
        if (address.isDefault()) {
            addressRepository.findByUserId(userId, Pageable.ofSize(1))
                    .stream()
                    .findFirst()
                    .ifPresent(a -> {
                        a.setDefault(true);
                        addressRepository.save(a);
                    });
        }

        // Publish event
        eventProducer.publishAddressDeleted(userId, addressId);

        log.info("Address deleted successfully: {}", addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String userId, String addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);

        Address address = findAddressByIdAndUserId(addressId, userId);

        if (address.isDefault()) {
            log.debug("Address {} is already default", addressId);
            return;
        }

        // Reset all addresses to non-default
        addressRepository.resetDefaultAddresses(userId);

        // Set this address as default
        address.setDefault(true);
        addressRepository.save(address);

        // Publish event
        eventProducer.publishDefaultAddressChanged(userId, addressId);

        log.info("Default address set successfully: {}", addressId);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Address findAddressByIdAndUserId(String addressId, String userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }
}