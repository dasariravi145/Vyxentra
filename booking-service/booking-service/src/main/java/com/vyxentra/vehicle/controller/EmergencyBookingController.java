package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.request.CreateBookingRequest;
import com.vyxentra.vehicle.dto.request.PetrolEmergencyRequest;
import com.vyxentra.vehicle.dto.request.RepairEmergencyRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.BookingResponse;
import com.vyxentra.vehicle.dto.response.NearbyProviderResponse;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings/emergency")
@RequiredArgsConstructor
public class EmergencyBookingController {

    private final BookingService bookingService;

    /**
     * Create emergency booking
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createEmergencyBooking(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody CreateBookingRequest request) {

        log.info("Creating emergency booking for customer: {}, type: {}", customerId, request.getEmergencyType());

        // Ensure emergency flag is set
        request.setIsEmergency(true);

        BookingResponse response = bookingService.createBooking(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Emergency booking created successfully"));
    }

    /**
     * Create petrol emergency booking
     */
    @PostMapping("/petrol")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createPetrolEmergency(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody PetrolEmergencyRequest request) {

        log.info("Creating petrol emergency for customer: {}", customerId);

        CreateBookingRequest bookingRequest = mapToBookingRequest(request);
        bookingRequest.setIsEmergency(true);
        bookingRequest.setEmergencyType(EmergencyType.PETROL_EMERGENCY);

        BookingResponse response = bookingService.createBooking(customerId, bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Petrol emergency created successfully"));
    }

    /**
     * Create repair emergency booking
     */
    @PostMapping("/repair")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createRepairEmergency(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody RepairEmergencyRequest request) {

        log.info("Creating repair emergency for customer: {}", customerId);

        CreateBookingRequest bookingRequest = mapToBookingRequest(request);
        bookingRequest.setIsEmergency(true);
        bookingRequest.setEmergencyType(EmergencyType.REPAIR_EMERGENCY);

        BookingResponse response = bookingService.createBooking(customerId, bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Repair emergency created successfully"));
    }

    /**
     * Get nearby providers for emergency
     */
    @GetMapping("/providers/nearby")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NearbyProviderResponse>>> getNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam EmergencyType emergencyType,
            @RequestParam(required = false, defaultValue = "5") Integer radiusKm) {

        log.info("Finding nearby providers for emergency at ({}, {})", latitude, longitude);

        List<NearbyProviderResponse> providers = List.of(
                NearbyProviderResponse.builder()
                        .providerId("prov_001")
                        .businessName("Quick Service Center")
                        .distance(2.5)
                        .etaMinutes(8)
                        .rating(4.5)
                        .build()
        );

        return ResponseEntity.ok(ApiResponse.success(providers));
    }


    private CreateBookingRequest mapToBookingRequest(PetrolEmergencyRequest request) {
        CreateBookingRequest.CreateBookingRequestBuilder builder = CreateBookingRequest.builder()
                .providerId(request.getProviderId())
                .vehicleType(VehicleType.valueOf(request.getVehicleType()))
                .vehicleDetails(request.getVehicleDetails())
                .location(mapLocation(request.getLocation()))
                .customerNotes(request.getCustomerNotes())
                .isEmergency(true)
                .emergencyType(EmergencyType.PETROL_EMERGENCY);

        if (request.getFuelType() != null && request.getQuantity() != null) {
            builder.petrolDetails(CreateBookingRequest.PetrolDetails.builder()
                    .fuelType(request.getFuelType())
                    .quantity(request.getQuantity())
                    .build());
        }

        return builder.build();
    }

    private CreateBookingRequest mapToBookingRequest(RepairEmergencyRequest request) {
        return CreateBookingRequest.builder()
                .providerId(request.getProviderId())
                .vehicleType(VehicleType.valueOf(request.getVehicleType()))
                .vehicleDetails(request.getVehicleDetails())
                .location(mapLocation(request.getLocation()))
                .customerNotes(request.getCustomerNotes())
                .isEmergency(true)
                .emergencyType(EmergencyType.REPAIR_EMERGENCY)
                .build();
    }

    private CreateBookingRequest.Location mapLocation(Location location) {
        return CreateBookingRequest.Location.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .build();
    }

    private CreateBookingRequest.Location mapLocation(RepairEmergencyRequest.Location location) {
        return CreateBookingRequest.Location.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .build();
    }
}
