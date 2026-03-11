package com.vyxentra.vehicle.exception;


import lombok.Getter;

@Getter
public enum ErrorCode {
    // Provider Errors (3000-3999)
    PROVIDER_NOT_FOUND("VEH-3000", "Provider not found"),
    PROVIDER_SUSPENDED("VEH-3001", "Provider is suspended"),
    PROVIDER_NOT_APPROVED("VEH-3002", "Provider not approved by admin"),
    PROVIDER_ALREADY_EXISTS("VEH-3003", "Provider already exists"),
    PROVIDER_INVALID_TYPE("VEH-3004", "Invalid provider type"),
    PROVIDER_INVALID_VEHICLE_SUPPORT("VEH-3005", "Provider must support at least one vehicle type"),
    PROVIDER_INVALID_STATUS("VEH-3006", "Invalid provider status for this operation"),
    PROVIDER_ALREADY_APPROVED("VEH-3007", "Provider already approved"),
    PROVIDER_ALREADY_REJECTED("VEH-3008", "Provider already rejected"),
    PROVIDER_DOCUMENTS_MISSING("VEH-3009", "Required documents are missing"),
    PROVIDER_DOCUMENTS_INVALID("VEH-3010", "Documents are invalid or expired"),

    // Pricing Errors (3100-3199)
    PRICING_NOT_FOUND("VEH-3100", "Pricing not found for this service"),
    PRICING_ALREADY_EXISTS("VEH-3101", "Pricing already exists for this service"),
    PRICING_INVALID_AMOUNT("VEH-3102", "Invalid pricing amount"),
    PRICING_NOT_ACTIVE("VEH-3103", "Pricing is not active"),

    // Service Errors (3200-3299)
    SERVICE_NOT_FOUND("VEH-3200", "Service not found"),
    SERVICE_ALREADY_EXISTS("VEH-3201", "Service already exists"),
    SERVICE_NOT_ACTIVE("VEH-3202", "Service is not active"),
    SERVICE_NOT_SUPPORTED("VEH-3203", "Service not supported by provider"),

    // Document Errors (3300-3399)
    DOCUMENT_NOT_FOUND("VEH-3300", "Document not found"),
    DOCUMENT_EXPIRED("VEH-3301", "Document has expired"),
    DOCUMENT_INVALID("VEH-3302", "Invalid document"),
    DOCUMENT_VERIFICATION_FAILED("VEH-3303", "Document verification failed"),

    // General Errors (1000-1999)
    VALIDATION_ERROR("VEH-1001", "Validation failed"),
    UNAUTHORIZED("VEH-1002", "Unauthorized access"),
    FORBIDDEN("VEH-1003", "Access forbidden"),
    RESOURCE_NOT_FOUND("VEH-1004", "Resource not found"),
    BAD_REQUEST("VEH-1005", "Bad request"),
    INTERNAL_SERVER_ERROR("VEH-1000", "Internal server error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
