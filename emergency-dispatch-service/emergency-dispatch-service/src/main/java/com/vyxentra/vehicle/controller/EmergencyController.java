package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.EmergencyBookingRequest;
import com.vyxentra.vehicle.dto.request.ProviderAcceptRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.EmergencyBookingResponse;
import com.vyxentra.vehicle.dto.response.ProviderMatchResponse;
import com.vyxentra.vehicle.service.EmergencyDispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyDispatchService emergencyDispatchService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> requestEmergency(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody EmergencyBookingRequest request) {
        log.info("Emergency request from customer: {}, type: {}", customerId, request.getEmergencyType());
        EmergencyBookingResponse response = emergencyDispatchService.requestEmergency(customerId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Emergency request accepted. Searching for providers..."));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> getEmergencyRequest(
            @PathVariable String requestId) {
        log.info("Getting emergency request: {}", requestId);
        EmergencyBookingResponse response = emergencyDispatchService.getEmergencyRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/request/number/{requestNumber}")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> getEmergencyRequestByNumber(
            @PathVariable String requestNumber) {
        log.info("Getting emergency request by number: {}", requestNumber);
        EmergencyBookingResponse response = emergencyDispatchService.getEmergencyRequestByNumber(requestNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/request/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelEmergencyRequest(
            @PathVariable String requestId,
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam String reason) {
        log.info("Cancelling emergency request: {} by customer: {}", requestId, customerId);
        emergencyDispatchService.cancelEmergencyRequest(requestId, customerId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Emergency request cancelled"));
    }

    @GetMapping("/providers/nearby")
    public ResponseEntity<ApiResponse<List<ProviderMatchResponse>>> findNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String emergencyType,
            @RequestParam String vehicleType,
            @RequestParam(required = false, defaultValue = "5") Integer radiusKm) {
        log.info("Finding nearby providers for emergency: {} at ({}, {})", emergencyType, latitude, longitude);
        List<ProviderMatchResponse> providers = emergencyDispatchService.findNearbyProviders(
                latitude, longitude, emergencyType, vehicleType, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @PostMapping("/provider/accept")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> acceptEmergency(
            @RequestHeader("X-User-ID") String providerId,
            @Valid @RequestBody ProviderAcceptRequest request) {

        log.info("Provider {} accepting emergency request: {}", providerId, request.getRequestId());

        EmergencyBookingResponse response = emergencyDispatchService.acceptEmergency(providerId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Emergency request accepted successfully"));
    }

    @PostMapping("/provider/{providerId}/location")
    public ResponseEntity<ApiResponse<Void>> updateProviderLocation(
            @PathVariable String providerId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("Updating location for provider: {} to ({}, {})", providerId, latitude, longitude);
        emergencyDispatchService.updateProviderLocation(providerId, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.success(null, "Location updated successfully"));
    }

    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> getActiveEmergency(
            @PathVariable String customerId) {
        log.info("Getting active emergency for customer: {}", customerId);
        EmergencyBookingResponse response = emergencyDispatchService.getActiveEmergencyForCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/provider/{providerId}/active")
    public ResponseEntity<ApiResponse<EmergencyBookingResponse>> getActiveAssignment(
            @PathVariable String providerId) {
        log.info("Getting active assignment for provider: {}", providerId);
        EmergencyBookingResponse response = emergencyDispatchService.getActiveAssignmentForProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/assignment/{assignmentId}/arrived")
    public ResponseEntity<ApiResponse<Void>> providerArrived(
            @PathVariable String assignmentId,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Provider {} arrived at location for assignment: {}", providerId, assignmentId);
        emergencyDispatchService.providerArrived(assignmentId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Arrival confirmed"));
    }

    @PostMapping("/assignment/{assignmentId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeEmergency(
            @PathVariable String assignmentId,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Provider {} completing emergency assignment: {}", providerId, assignmentId);
        emergencyDispatchService.completeEmergency(assignmentId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Emergency completed"));
    }
}
