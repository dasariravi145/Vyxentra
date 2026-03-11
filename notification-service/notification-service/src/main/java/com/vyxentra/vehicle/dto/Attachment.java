package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class Attachment {
    private String filename;
    private String contentType;
    private byte[] content;
    private String type;
    private  String disposition;
}
