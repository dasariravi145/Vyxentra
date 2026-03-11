package com.vyxentra.vehicle.provider;

import com.vyxentra.vehicle.dto.Attachment;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SMTPProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public Map<String, Object> sendEmail(String from, String to, String subject, String htmlContent,
                                         String textContent, List<Attachment> attachments) {
        Map<String, Object> response = new HashMap<>();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent != null ? textContent : htmlContent, htmlContent != null);

            // Add attachments
            if (attachments != null) {
                for (Attachment att : attachments) {
                    helper.addAttachment(att.getFilename(),
                            new ByteArrayDataSource(att.getContent(), att.getContentType()));
                }
            }

            mailSender.send(message);

            response.put("success", true);
            response.put("messageId", message.getMessageID());

            log.info("SMTP email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("SMTP email failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    // Simple DataSource implementation for attachments
    private static class ByteArrayDataSource implements jakarta.activation.DataSource {
        private final byte[] data;
        private final String contentType;

        public ByteArrayDataSource(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return new java.io.ByteArrayInputStream(data);
        }

        @Override
        public java.io.OutputStream getOutputStream() throws java.io.IOException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return "attachment";
        }
    }
}