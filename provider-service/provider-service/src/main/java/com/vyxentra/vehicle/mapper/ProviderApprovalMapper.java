package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.entity.ProviderApproval;
import com.vyxentra.vehicle.entity.ProviderDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProviderApprovalMapper {

    @Mapping(target = "approvalId", source = "id")
    @Mapping(target = "providerId", source = "provider.id")
    @Mapping(target = "businessName", source = "provider.businessName")
    @Mapping(target = "providerType", source = "provider.providerType")
    @Mapping(target = "ownerName", source = "provider.ownerName")
    @Mapping(target = "email", source = "provider.email")
    @Mapping(target = "phone", source = "provider.phone")
    @Mapping(target = "status", source = "provider.status")
    @Mapping(target = "gstNumber", source = "provider.gstNumber")
    @Mapping(target = "panNumber", source = "provider.panNumber")
    @Mapping(target = "city", source = "provider.city")
    @Mapping(target = "state", source = "provider.state")
    @Mapping(target = "documents", expression = "java(mapDocuments(provider))")
    @Mapping(target = "allDocumentsUploaded", expression = "java(checkAllDocumentsUploaded(provider))")
    @Mapping(target = "allDocumentsVerified", expression = "java(checkAllDocumentsVerified(provider))")
    @Mapping(target = "pendingDocuments", expression = "java(getPendingDocumentTypes(provider))")
    @Mapping(target = "verifiedDocuments", expression = "java(getVerifiedDocumentTypes(provider))")
    ProviderApprovalResponse toResponse(ProviderApproval approval);

    List<ProviderApprovalResponse> toResponseList(List<ProviderApproval> approvals);

    default List<ProviderApprovalResponse.DocumentVerification> mapDocuments(Provider provider) {
        if (provider.getDocuments() == null) return List.of();

        return provider.getDocuments().stream()
                .map(doc -> ProviderApprovalResponse.DocumentVerification.builder()
                        .documentId(doc.getId())
                        .documentType(doc.getDocumentType())
                        .documentName(doc.getDocumentType())
                        .documentUrl(doc.getDocumentUrl())
                        .documentNumber(doc.getDocumentNumber())
                        .verified(doc.isVerified())
                        .verifiedAt(doc.getVerifiedAt())
                        .verifiedBy(doc.getVerifiedBy())
                        .remarks(doc.getRemarks())
                        .issues(doc.getRemarks() != null ? List.of(doc.getRemarks()) : List.of())
                        .build())
                .collect(Collectors.toList());
    }

    default boolean checkAllDocumentsUploaded(Provider provider) {
        return provider.getDocuments() != null && !provider.getDocuments().isEmpty();
    }

    default boolean checkAllDocumentsVerified(Provider provider) {
        if (provider.getDocuments() == null || provider.getDocuments().isEmpty()) {
            return false;
        }
        return provider.getDocuments().stream().allMatch(ProviderDocument::isVerified);
    }

    default List<String> getPendingDocumentTypes(Provider provider) {
        if (provider.getDocuments() == null) return List.of();

        return provider.getDocuments().stream()
                .filter(doc -> !doc.isVerified())
                .map(ProviderDocument::getDocumentType)
                .collect(Collectors.toList());
    }

    default List<String> getVerifiedDocumentTypes(Provider provider) {
        if (provider.getDocuments() == null) return List.of();

        return provider.getDocuments().stream()
                .filter(ProviderDocument::isVerified)
                .map(ProviderDocument::getDocumentType)
                .collect(Collectors.toList());
    }
}
