package com.vyxentra.vehicle.event;

import com.vyxentra.vehicle.events.BaseEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class OtpSentEvent extends BaseEvent {

    private String mobileNumber;
}