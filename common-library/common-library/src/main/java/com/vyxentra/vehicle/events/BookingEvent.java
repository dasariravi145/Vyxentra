package com.vyxentra.vehicle.events;




import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingEvent extends BaseEvent {
    private String bookingId;
    private String customerId;
    private String providerId;
    private String employeeId;
    private VehicleType vehicleType;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal approvedAmount;
    private Instant scheduledTime;
    private String location;
    private Double latitude;
    private Double longitude;
    private boolean emergency;
    private String emergencyType;
    private boolean upfrontPayment;
    private String snapshot;
}