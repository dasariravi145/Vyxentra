package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.SearchServicesRequest;
import com.vyxentra.vehicle.dto.request.ServiceDefinitionRequest;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService catalogService;

    // ==================== Public Endpoints ====================

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getAllServices(
            @RequestParam(required = false) Boolean active) {
        log.info("Getting all services, active: {}", active);
        List<ServiceDefinitionResponse> responses = catalogService.getAllServices(active);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/services/paginated")
    public ResponseEntity<ApiResponse<PageResponse<ServiceDefinitionResponse>>> getServicesPaginated(
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String providerType,
            @RequestParam(required = false) Boolean active) {
        log.info("Getting paginated services with filters");
        PageResponse<ServiceDefinitionResponse> response = catalogService.getServicesPaginated(
                pageable, category, vehicleType, providerType, active);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceDetailResponse>> getServiceDetails(
            @PathVariable String serviceId) {
        log.info("Getting service details: {}", serviceId);
        ServiceDetailResponse response = catalogService.getServiceDetails(serviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/services/type/{serviceType}")
    public ResponseEntity<ApiResponse<ServiceDetailResponse>> getServiceByType(
            @PathVariable String serviceType) {
        log.info("Getting service by type: {}", serviceType);
        ServiceDetailResponse response = catalogService.getServiceByType(serviceType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/services/search")
    public ResponseEntity<ApiResponse<List<ServiceSearchResponse>>> searchServices(
            @Valid SearchServicesRequest request) {
        log.info("Searching services with query: {}", request.getQuery());
        List<ServiceSearchResponse> responses = catalogService.searchServices(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/services/popular")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getPopularServices(
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("Getting popular services for vehicle: {}", vehicleType);
        List<ServiceDefinitionResponse> responses = catalogService.getPopularServices(vehicleType, limit);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/services/recommended")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getRecommendedServices(
            @RequestParam(required = false) String vehicleType) {
        log.info("Getting recommended services for vehicle: {}", vehicleType);
        List<ServiceDefinitionResponse> responses = catalogService.getRecommendedServices(vehicleType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/services/by-vehicle/{vehicleType}")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getServicesByVehicleType(
            @PathVariable String vehicleType,
            @RequestParam(required = false) String providerType) {
        log.info("Getting services for vehicle: {}, provider: {}", vehicleType, providerType);
        List<ServiceDefinitionResponse> responses = catalogService.getServicesByVehicleType(vehicleType, providerType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ==================== Admin Endpoints ====================

    @PostMapping("/admin/services")
    public ResponseEntity<ApiResponse<ServiceDefinitionResponse>> createService(
            @RequestHeader("X-User-ID") String adminId,
            @Valid @RequestBody ServiceDefinitionRequest request) {
        log.info("Creating new service by admin: {}", adminId);
        ServiceDefinitionResponse response = catalogService.createService(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Service created successfully"));
    }

    @PutMapping("/admin/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceDefinitionResponse>> updateService(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId,
            @Valid @RequestBody ServiceDefinitionRequest request) {
        log.info("Updating service: {} by admin: {}", serviceId, adminId);
        ServiceDefinitionResponse response = catalogService.updateService(adminId, serviceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Service updated successfully"));
    }

    @DeleteMapping("/admin/services/{serviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId) {
        log.info("Deleting service: {} by admin: {}", serviceId, adminId);
        catalogService.deleteService(adminId, serviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Service deleted successfully"));
    }

    @PatchMapping("/admin/services/{serviceId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleServiceStatus(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId,
            @RequestParam boolean active) {
        log.info("Toggling service: {} to {} by admin: {}", serviceId, active, adminId);
        catalogService.toggleServiceStatus(adminId, serviceId, active);
        return ResponseEntity.ok(ApiResponse.success(null,
                active ? "Service activated" : "Service deactivated"));
    }

    @PostMapping("/admin/services/{serviceId}/pricing")
    public ResponseEntity<ApiResponse<Void>> updateServicePricing(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId,
            @RequestParam String vehicleType,
            @RequestParam Double basePrice) {
        log.info("Updating pricing for service: {}, vehicle: {}, price: {}", serviceId, vehicleType, basePrice);
        catalogService.updateServicePricing(adminId, serviceId, vehicleType, basePrice);
        return ResponseEntity.ok(ApiResponse.success(null, "Pricing updated successfully"));
    }

    @GetMapping("/admin/services/pending-review")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getServicesPendingReview(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting services pending review for admin: {}", adminId);
        List<ServiceDefinitionResponse> responses = catalogService.getServicesPendingReview();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}