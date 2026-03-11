package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.AddAddressRequest;
import com.vyxentra.vehicle.dto.request.UpdateAddressRequest;
import com.vyxentra.vehicle.dto.response.AddressResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AddressService {

    AddressResponse addAddress(String userId, AddAddressRequest request);

    PageResponse<AddressResponse> getUserAddresses(String userId, Pageable pageable);

    AddressResponse getAddress(String userId, String addressId);

    AddressResponse updateAddress(String userId, String addressId, UpdateAddressRequest request);

    void deleteAddress(String userId, String addressId);

    void setDefaultAddress(String userId, String addressId);
}