package com.vyxentra.vehicle.provider;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TwilioProvider implements SMSProvider {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public Map<String, Object> sendSMS(String from, String to, String message) {
        Map<String, Object> response = new HashMap<>();

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    message
            ).create();

            response.put("success", true);
            response.put("sid", twilioMessage.getSid());
            response.put("status", twilioMessage.getStatus().toString());

            log.info("Twilio SMS sent with SID: {}", twilioMessage.getSid());

        } catch (Exception e) {
            log.error("Twilio SMS failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }
}