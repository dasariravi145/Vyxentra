package com.vyxentra.vehicle.provider;


import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.vyxentra.vehicle.dto.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SendGridProvider implements EmailProvider {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Override
    public Map<String, Object> sendEmail(String from, String to, String subject, String htmlContent,
                                         String textContent, List<Attachment> attachments) {
        Map<String, Object> response = new HashMap<>();

        try {
            Email fromEmail = new Email(from);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(fromEmail, subject, toEmail, content);

            // Add plain text alternative
            if (textContent != null) {
                mail.addContent(new Content("text/plain", textContent));
            }

            // Add attachments
            if (attachments != null) {
                for (Attachment att : attachments) {

                    Attachments sgAttachment = new Attachments();

                    String encodedContent = Base64.getEncoder()
                            .encodeToString(att.getContent()); // byte[] → Base64 string

                    sgAttachment.setContent(encodedContent);
                    sgAttachment.setType(att.getContentType());
                    sgAttachment.setFilename(att.getFilename());
                    sgAttachment.setDisposition("attachment");

                    mail.addAttachments(sgAttachment);
                }
            }

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response sgResponse = sg.api(request);

            response.put("success", sgResponse.getStatusCode() >= 200 && sgResponse.getStatusCode() < 300);
            response.put("statusCode", sgResponse.getStatusCode());
            response.put("messageId", sgResponse.getHeaders().get("X-Message-Id"));
            response.put("body", sgResponse.getBody());

            log.info("SendGrid response status: {}", sgResponse.getStatusCode());

        } catch (IOException e) {
            log.error("SendGrid email failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "SENDGRID";
    }
}
