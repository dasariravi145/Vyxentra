package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.dto.Attachment;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @NotBlank(message = "To email is required")
    @Email(message = "Invalid email format")
    private String to;

    @Email(message = "Invalid email format")
    private String cc;

    @Email(message = "Invalid email format")
    private String bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String content; // HTML content

    private String textContent; // Plain text fallback

    private String templateName;

    private Map<String, Object> templateData;

    private List<Attachment> attachments;

    private String referenceId;

    private String referenceType;


}
