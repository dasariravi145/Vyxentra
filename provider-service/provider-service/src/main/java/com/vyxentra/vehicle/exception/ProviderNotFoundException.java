package com.vyxentra.vehicle.exception;



public class ProviderNotFoundException extends ResourceNotFoundException {

    public ProviderNotFoundException(String providerId) {
        super("Provider", providerId);
    }

    public ProviderNotFoundException(String field, String value) {
        super("Provider", field, value);
    }
}
