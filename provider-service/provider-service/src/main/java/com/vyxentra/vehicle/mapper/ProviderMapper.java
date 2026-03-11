package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.entity.ProviderDocument;
import com.vyxentra.vehicle.entity.ProviderPricing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProviderMapper {

    @Mapping(target = "providerId", source = "id")
    @Mapping(target = "address", expression = "java(buildAddress(provider))")
    ProviderResponse toResponse(Provider provider);

    List<ProviderResponse> toResponseList(List<Provider> providers);

    @Mapping(target = "providerId", source = "id")
    @Mapping(target = "address", expression = "java(buildAddress(provider))")
    @Mapping(target = "services", expression = "java(mapServices(provider.getPricings()))")
    @Mapping(target = "documents", expression = "java(mapDocuments(provider.getDocuments()))")
    ProviderDetailResponse toDetailResponse(Provider provider);

    @Mapping(target = "providerId", source = "id")
    @Mapping(target = "address", expression = "java(buildAddress(provider))")
    @Mapping(target = "distance", source = "distance")
    @Mapping(target = "isOpenNow", expression = "java(checkIfOpenNow(provider))")
    ProviderSearchResponse toSearchResponse(Provider provider, Double distance);

    default String buildAddress(Provider provider) {
        StringBuilder address = new StringBuilder();
        if (provider.getAddressLine1() != null) {
            address.append(provider.getAddressLine1());
        }
        if (provider.getAddressLine2() != null && !provider.getAddressLine2().isEmpty()) {
            address.append(", ").append(provider.getAddressLine2());
        }
        if (provider.getCity() != null) {
            address.append(", ").append(provider.getCity());
        }
        if (provider.getState() != null) {
            address.append(", ").append(provider.getState());
        }
        if (provider.getPostalCode() != null) {
            address.append(" - ").append(provider.getPostalCode());
        }
        return address.toString();
    }

    default List<ServiceOfferingResponse> mapServices(List<ProviderPricing> pricings) {
        if (pricings == null) return List.of();

        return pricings.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(p -> ServiceOfferingResponse.builder()
                        .serviceId(p.getId())
                        .serviceType(p.getServiceType() != null ? p.getServiceType().name() : null)
                        .name(p.getServiceType() != null ? p.getServiceType().getDisplayName() : null)
                        .description(p.getDescription())
                        .pricing(p.getVehiclePricing())
                        .estimatedDuration(p.getEstimatedDurationMinutes())
                        .isActive(p.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    default List<DocumentResponse> mapDocuments(List<ProviderDocument> documents) {
        if (documents == null) return List.of();

        return documents.stream()
                .map(d -> DocumentResponse.builder()
                        .documentId(d.getId())
                        .documentType(d.getDocumentType())
                        .documentUrl(d.getDocumentUrl())
                        .documentNumber(d.getDocumentNumber())
                        .verified(d.isVerified())
                        .verifiedBy(d.getVerifiedBy())
                        .verifiedAt(d.getVerifiedAt())
                        .remarks(d.getRemarks())
                        .expiryDate(d.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
    }

    default boolean checkIfOpenNow(Provider provider) {
        if (Boolean.TRUE.equals(provider.getTwentyFourSeven())) {
            return true;
        }
        if (provider.getOpeningTime() == null || provider.getClosingTime() == null) {
            return true;
        }
        java.time.LocalTime now = java.time.LocalTime.now();
        return !now.isBefore(provider.getOpeningTime()) && !now.isAfter(provider.getClosingTime());
    }
}
