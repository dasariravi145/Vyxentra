package com.vyxentra.vehicle.client;

import org.springframework.stereotype.Component;

@Component
class ProviderServiceClientFallback implements ProviderServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProviderServiceClientFallback.class);

    @Override
    public Boolean validateProvider(String providerId) {
        log.error("Fallback: validateProvider failed for provider: {}", providerId);
        return false;
    }
}
