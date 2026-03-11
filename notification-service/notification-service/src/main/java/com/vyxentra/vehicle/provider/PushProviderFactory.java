package com.vyxentra.vehicle.provider;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushProviderFactory {

    private final FirebaseProvider firebaseProvider;

    @Value("${notification.push.provider:firebase}")
    private String defaultProvider;

    public PushProvider getProvider() {
        return getProvider(defaultProvider);
    }

    public PushProvider getProvider(String providerName) {
        switch (providerName.toLowerCase()) {
            case "firebase":
                return firebaseProvider;
            default:
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Unsupported push provider: " + providerName);
        }
    }
}
