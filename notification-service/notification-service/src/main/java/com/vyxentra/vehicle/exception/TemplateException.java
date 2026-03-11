package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class TemplateException extends BusinessException {

    private final String templateName;
    private final String channel;

    public TemplateException(String templateName, String channel, ErrorCode errorCode) {
        super(errorCode);
        this.templateName = templateName;
        this.channel = channel;
    }

    public TemplateException(String templateName, String channel, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.templateName = templateName;
        this.channel = channel;
    }

    public TemplateException(String templateName, String channel, String message) {
        super(ErrorCode.valueOf(message));
        this.templateName = templateName;
        this.channel = channel;
    }
}
