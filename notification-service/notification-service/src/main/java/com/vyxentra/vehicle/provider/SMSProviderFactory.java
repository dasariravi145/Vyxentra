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
public class SMSProviderFactory {

    private final TwilioProvider twilioProvider;

    @Value("${notification.sms.provider:twilio}")
    private String defaultProvider;

    public SMSProvider getProvider() {
        return getProvider(defaultProvider);
    }

    public SMSProvider getProvider(String providerName) {
        switch (providerName.toLowerCase()) {
            case "twilio":
                return twilioProvider;
            default:
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Unsupported SMS provider: " + providerName);
        }
    }
}
