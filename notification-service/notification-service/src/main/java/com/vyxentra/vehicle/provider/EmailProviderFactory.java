package com.vyxentra.vehicle.provider;

import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailProviderFactory {

    private final SendGridProvider sendGridProvider;
    private final SMTPProvider smtpProvider;

    @Value("${notification.email.provider:sendgrid}")
    private String defaultProvider;

    public EmailProvider getProvider() {
        return getProvider(defaultProvider);
    }

    public EmailProvider getProvider(String providerName) {
        switch (providerName.toLowerCase()) {
            case "sendgrid":
                return sendGridProvider;
            case "smtp":
                return (EmailProvider) smtpProvider;
            default:
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Unsupported email provider: " + providerName);
        }
    }
}
