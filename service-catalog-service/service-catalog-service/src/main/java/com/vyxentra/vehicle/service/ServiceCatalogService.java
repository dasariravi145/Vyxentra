package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.SearchServicesRequest;
import com.vyxentra.vehicle.dto.request.ServiceDefinitionRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;
import com.vyxentra.vehicle.dto.response.ServiceDetailResponse;
import com.vyxentra.vehicle.dto.response.ServiceSearchResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceCatalogService {

    // Public endpoints
    List<ServiceDefinitionResponse> getAllServices(Boolean active);

    PageResponse<ServiceDefinitionResponse> getServicesPaginated(Pageable pageable, String category,
                                                                 String vehicleType, String providerType,
                                                                 Boolean active);

    ServiceDetailResponse getServiceDetails(String serviceId);

    ServiceDetailResponse getServiceByType(String serviceType);

    List<ServiceSearchResponse> searchServices(SearchServicesRequest request);

    List<ServiceDefinitionResponse> getPopularServices(String vehicleType, Integer limit);

    List<ServiceDefinitionResponse> getRecommendedServices(String vehicleType);

    List<ServiceDefinitionResponse> getServicesByVehicleType(String vehicleType, String providerType);

    // Admin endpoints
    ServiceDefinitionResponse createService(String adminId, ServiceDefinitionRequest request);

    ServiceDefinitionResponse updateService(String adminId, String serviceId, ServiceDefinitionRequest request);

    void deleteService(String adminId, String serviceId);

    void toggleServiceStatus(String adminId, String serviceId, boolean active);

    void updateServicePricing(String adminId, String serviceId, String vehicleType, Double basePrice);

    List<ServiceDefinitionResponse> getServicesPendingReview();
}
