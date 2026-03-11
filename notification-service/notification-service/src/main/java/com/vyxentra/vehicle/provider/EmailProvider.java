package com.vyxentra.vehicle.provider;



import com.vyxentra.vehicle.dto.Attachment;

import java.util.List;
import java.util.Map;

public interface EmailProvider {

    Map<String, Object> sendEmail(String from, String to, String subject, String htmlContent,
                                  String textContent, List<Attachment> attachments);

    String getProviderName();
}
